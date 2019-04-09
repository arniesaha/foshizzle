/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.foshizzle.ui;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.assist.AssistContent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
//import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.foshizzle.R;
import io.foshizzle.data.api.deviantart.model.Comment;
import io.foshizzle.data.api.deviantart.model.CommentResponse;
import io.foshizzle.data.api.deviantart.model.Deviation;
import io.foshizzle.data.prefs.DeviantartPrefs;
import io.foshizzle.ui.recyclerview.InsetDividerDecoration;
import io.foshizzle.ui.recyclerview.SlideInItemAnimator;
import io.foshizzle.ui.transitions.FabTransform;
import io.foshizzle.ui.widget.AuthorTextView;
import io.foshizzle.ui.widget.CheckableImageButton;
import io.foshizzle.ui.widget.ElasticDragDismissFrameLayout;
import io.foshizzle.ui.widget.FabOverlapTextView;
import io.foshizzle.ui.widget.ForegroundImageView;
import io.foshizzle.ui.widget.ParallaxScrimageView;
import io.foshizzle.util.ColorUtils;
import io.foshizzle.util.HtmlUtils;
import io.foshizzle.util.ImeUtils;
import io.foshizzle.util.TransitionUtils;
import io.foshizzle.util.ViewUtils;
import io.foshizzle.util.customtabs.CustomTabActivityHelper;
//import io.plaidapp.util.glide.CircleTransform;
//import io.plaidapp.util.glide.GlideUtils;
import io.foshizzle.util.glide.GlideApp;
import io.foshizzle.util.glide.GlideUtils;
import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static io.foshizzle.util.AnimUtils.getFastOutSlowInInterpolator;

public class DeviationActivity extends Activity {

    public final static String DEVIATION = "DEVIATION";
    public final static String RESULT_EXTRA_SHOT_ID = "RESULT_EXTRA_DEVIATION_ID";
    private static final int RC_LOGIN_LIKE = 0;
    private static final int RC_LOGIN_COMMENT = 1;
    private static final float SCRIM_ADJUSTMENT = 0.075f;

    @BindView(R.id.draggable_frame) ElasticDragDismissFrameLayout draggableFrame;
    @BindView(R.id.back) ImageButton back;
    @BindView(R.id.deviation) ParallaxScrimageView imageView;
    @BindView(R.id.deviation_comments) RecyclerView commentsList;
//    @BindView(R.id.fab_heart) FABToggle fab;
    View deviationDescription;
    View deviationSpacer;
    Button likeCount;
    Button viewCount;
    Button share;
    ImageView playerAvatar;
    EditText enterComment;
    ImageButton postComment;
    private View title;
    private View description;
    private TextView playerName;
    private TextView shotTimeAgo;
    private View commentFooter;
    private ImageView userAvatar;
    private ElasticDragDismissFrameLayout.SystemChromeFader chromeFader;

    Deviation deviation;
//    int fabOffset;
    DeviantartPrefs deviantartPrefs;
//    boolean performingLike;
    boolean allowComment;
//    CircleTransform circleTransform;
    CommentsAdapter adapter;
    CommentAnimator commentAnimator;
    @BindDimen(R.dimen.large_avatar_size) int largeAvatarSize;
    @BindDimen(R.dimen.z_card) int cardElevation;

    long id;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deviation);
        deviantartPrefs = DeviantartPrefs.get(this);
//        circleTransform = new CircleTransform(this);
        ButterKnife.bind(this);
        deviationDescription = getLayoutInflater().inflate(R.layout.deviation_description,
                commentsList, false);
        deviationSpacer = deviationDescription.findViewById(R.id.deviation_spacer);
        title = deviationDescription.findViewById(R.id.deviation_title);
        description = deviationDescription.findViewById(R.id.deviation_description);
        likeCount = (Button) deviationDescription.findViewById(R.id.shot_like_count);
        viewCount = (Button) deviationDescription.findViewById(R.id.shot_view_count);
        share = (Button) deviationDescription.findViewById(R.id.shot_share_action);
        playerName = (TextView) deviationDescription.findViewById(R.id.player_name);
        playerAvatar = (ImageView) deviationDescription.findViewById(R.id.player_avatar);
        shotTimeAgo = (TextView) deviationDescription.findViewById(R.id.shot_time_ago);

//        setupCommenting();
        commentsList.addOnScrollListener(scrollListener);
        commentsList.setOnFlingListener(flingListener);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResultAndFinish();
            }
        });
//        fab.setOnClickListener(fabClick);
        chromeFader = new ElasticDragDismissFrameLayout.SystemChromeFader(this) {
            @Override
            public void onDragDismissed() {
                setResultAndFinish();
            }
        };

        final Intent intent = getIntent();
        if (intent.hasExtra(DEVIATION)) {
            deviation = intent.getParcelableExtra(DEVIATION);
            bindDeviation(true);
        } else if (intent.getData() != null) {
            final HttpUrl url = HttpUrl.parse(intent.getDataString());
            if (url.pathSize() == 2 && url.pathSegments().get(0).equals("shots")) {
                try {
                    final String shotPath = url.pathSegments().get(1);
                    id = Long.parseLong(shotPath.substring(0, shotPath.indexOf("-")));

                    final Call<Deviation> deviationCall = deviantartPrefs.getApi().getDeviation(id);
                    deviationCall.enqueue(new Callback<Deviation>() {
                        @Override
                        public void onResponse(Call<Deviation> call, Response<Deviation> response) {
                            deviation = response.body();
                            bindDeviation(false);
                        }

                        @Override
                        public void onFailure(Call<Deviation> call, Throwable t) {
                            reportUrlError();
                        }
                    });
                } catch (NumberFormatException|StringIndexOutOfBoundsException ex) {
                    reportUrlError();
                }
            } else {
                reportUrlError();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (!performingLike) {
//            checkLiked();
//        }
        draggableFrame.addListener(chromeFader);
    }

    @Override
    protected void onPause() {
        draggableFrame.removeListener(chromeFader);
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_LOGIN_LIKE:
                if (resultCode == RESULT_OK) {
                    // TODO when we add more authenticated actions will need to keep track of what
                    // the user was trying to do when forced to login
//                    fab.setChecked(true);
//                    doLike();
//                    setupCommenting();
                }
                break;
            case RC_LOGIN_COMMENT:
//                if (resultCode == RESULT_OK) {
//                    setupCommenting();
//                }
        }
    }

    @Override
    public void onBackPressed() {
        setResultAndFinish();
    }

    @Override
    public boolean onNavigateUp() {
        setResultAndFinish();
        return true;
    }

    @Override @TargetApi(Build.VERSION_CODES.M)
    public void onProvideAssistContent(AssistContent outContent) {
        outContent.setWebUri(Uri.parse(deviation.url));
    }

    public void postComment(View view) {
        if (deviantartPrefs.isLoggedIn()) {
            if (TextUtils.isEmpty(enterComment.getText())) return;
            enterComment.setEnabled(false);
//            final Call<Comment> postCommentCall = deviantartPrefs.getApi().postComment(
//                    shot.id, enterComment.getText().toString().trim());
//            postCommentCall.enqueue(new Callback<Comment>() {
//                @Override
//                public void onResponse(Call<Comment> call, Response<Comment> response) {
//                    loadComments();
//                    enterComment.getText().clear();
//                    enterComment.setEnabled(true);
//                }
//
//                @Override
//                public void onFailure(Call<Comment> call, Throwable t) {
//                    enterComment.setEnabled(true);
//                }
//            });
        } else {
            Intent login = new Intent(DeviationActivity.this, DeviantartLogin.class);
            FabTransform.addExtras(login, ContextCompat.getColor(
                    DeviationActivity.this, R.color.background_light), R.drawable.ic_comment_add);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                    DeviationActivity.this, postComment, getString(R.string.transition_dribbble_login));
            startActivityForResult(login, RC_LOGIN_COMMENT, options.toBundle());
        }
    }

    void bindDeviation(final boolean postponeEnterTransition) {
        final Resources res = getResources();

        // load the main image
//        final int[] imageSize = deviation.images.bestSize();
//        final int[] imageSize = new int[] { 400, 300 };

        if(deviation.content != null) {

            GlideApp.with(this)
                    .load(deviation.content.getSource())
                    .listener(deviationListener)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .priority(Priority.IMMEDIATE)
//                .override(imageSize[0], imageSize[1])
                    .transition(withCrossFade())
                    .into(imageView);

            imageView.setOnClickListener(shotClick);
            deviationSpacer.setOnClickListener(shotClick);

            if (postponeEnterTransition) postponeEnterTransition();
            imageView.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            imageView.getViewTreeObserver().removeOnPreDrawListener(this);
//                        calculateFabPosition();
                            if (postponeEnterTransition) startPostponedEnterTransition();
                            return true;
                        }
                    });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ((FabOverlapTextView) title).setText(deviation.title);
            } else {
                ((TextView) title).setText(deviation.title);
            }
            if (!TextUtils.isEmpty(deviation.description)) {
                final Spanned descText = deviation.getParsedDescription(
                        ContextCompat.getColorStateList(this, R.color.dribbble_links),
                        ContextCompat.getColor(this, R.color.dribbble_link_highlight));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ((FabOverlapTextView) description).setText(descText);
                } else {
                    HtmlUtils.setTextWithNiceLinks((TextView) description, descText);
                }
            } else {
                description.setVisibility(View.GONE);
            }
            NumberFormat nf = NumberFormat.getInstance();
            likeCount.setText(
                    res.getQuantityString(R.plurals.likes,
                            (int) deviation.stats.getLikes(),
                            nf.format(deviation.stats.getLikes())));
            likeCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AnimatedVectorDrawable) likeCount.getCompoundDrawables()[1]).start();
//                if (deviation.stats.getLikes() > 0) {
//                    PlayerSheet.start(DeviationActivity.this, deviation);
//                }
                }
            });
            if (deviation.stats.getLikes() == 0) {
                likeCount.setBackground(null); // clear touch ripple if doesn't do anything
            }
            viewCount.setText(
                    res.getQuantityString(R.plurals.views,
                            (int) deviation.stats.getComments(),
                            nf.format(deviation.stats.getComments())));
            viewCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AnimatedVectorDrawable) viewCount.getCompoundDrawables()[1]).start();
                }
            });
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AnimatedVectorDrawable) share.getCompoundDrawables()[1]).start();
                    new ShareDeviationImageTask(DeviationActivity.this, deviation).execute();
                }
            });
            if (deviation.author != null) {
                playerName.setText(deviation.author.username.toLowerCase());
                GlideApp.with(this)
                        .load(deviation.author.usericon)
                        .circleCrop()
                        .placeholder(R.drawable.avatar_placeholder)
                        .override(largeAvatarSize, largeAvatarSize)
                        .transition(withCrossFade())
                        .into(playerAvatar);
                View.OnClickListener playerClick = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                    Intent player = new Intent(DeviationActivity.this, PlayerActivity.class);
//                    if (deviation.stats.getLikes() > 0) { // legit user object
//                        player.putExtra(PlayerActivity.EXTRA_PLAYER, deviation.author);
//                    } else {
//                        // search doesn't fully populate the user object,
//                        // in this case send the ID not the full user
//                        player.putExtra(PlayerActivity.EXTRA_PLAYER_NAME, deviation.author.username);
//                        player.putExtra(PlayerActivity.EXTRA_PLAYER_ID, deviation.author.userid);
//                    }
//                    ActivityOptions options =
//                            ActivityOptions.makeSceneTransitionAnimation(DeviationActivity.this,
//                                    playerAvatar, getString(R.string.transition_player_avatar));
//                    startActivity(player, options.toBundle());
                    }
                };
                playerAvatar.setOnClickListener(playerClick);
                playerName.setOnClickListener(playerClick);
                if (deviation.author != null) {
                    shotTimeAgo.setText(DateUtils.getRelativeTimeSpanString(deviation.published_time * 1000L, System.currentTimeMillis(),
                            DateUtils.SECOND_IN_MILLIS).toString().toLowerCase());
                }
            } else {
                playerName.setVisibility(View.GONE);
                playerAvatar.setVisibility(View.GONE);
                shotTimeAgo.setVisibility(View.GONE);
            }

            commentAnimator = new CommentAnimator();
            commentsList.setItemAnimator(commentAnimator);
            adapter = new CommentsAdapter(deviationDescription, commentFooter, deviation.stats.getComments(),
                    getResources().getInteger(R.integer.comment_expand_collapse_duration));
            commentsList.setAdapter(adapter);
            commentsList.addItemDecoration(new InsetDividerDecoration(
                    res.getDimensionPixelSize(R.dimen.divider_height),
                    res.getDimensionPixelSize(R.dimen.keyline_1),
                    ContextCompat.getColor(this, R.color.divider)));
            if (deviation.stats.getComments() != 0) {
                loadComments();
            }
//        checkLiked();
        }
    }

    void reportUrlError() {
        Snackbar.make(draggableFrame, R.string.bad_dribbble_shot_url, Snackbar.LENGTH_SHORT).show();
        draggableFrame.postDelayed(new Runnable() {
            @Override
            public void run() {
                finishAfterTransition();
            }
        }, 3000L);
    }

    private View.OnClickListener shotClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            openLink(deviation.url);
        }
    };

    /**
     * We run a transition to expand/collapse comments. Scrolling the RecyclerView while this is
     * running causes issues, so we consume touch events while the transition runs.
     */
    View.OnTouchListener touchEater = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return true;
        }
    };

    void openLink(String url) {
        CustomTabActivityHelper.openCustomTab(
                DeviationActivity.this,
                new CustomTabsIntent.Builder()
                        .setToolbarColor(ContextCompat.getColor(DeviationActivity.this, R.color.deviantart))
                        .addDefaultShareMenuItem()
                        .build(),
                Uri.parse(url));
    }

    private RequestListener deviationListener = new RequestListener<Drawable>() {
        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                       DataSource dataSource, boolean isFirstResource) {
            final Bitmap bitmap = GlideUtils.getBitmap(resource);
            final int twentyFourDip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    24, DeviationActivity.this.getResources().getDisplayMetrics());
            Palette.from(bitmap)
                    .maximumColorCount(3)
                    .clearFilters() /* by default palette ignore certain hues
                        (e.g. pure black/white) but we don't want this. */
                    .setRegion(0, 0, bitmap.getWidth() - 1, twentyFourDip) /* - 1 to work around
                        https://code.google.com/p/android/issues/detail?id=191013 */
                    .generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            boolean isDark;
                            @ColorUtils.Lightness int lightness = ColorUtils.isDark(palette);
                            if (lightness == ColorUtils.LIGHTNESS_UNKNOWN) {
                                isDark = ColorUtils.isDark(bitmap, bitmap.getWidth() / 2, 0);
                            } else {
                                isDark = lightness == ColorUtils.IS_DARK;
                            }

                            if (!isDark) { // make back icon dark on light images
                                back.setColorFilter(ContextCompat.getColor(
                                        DeviationActivity.this, R.color.deviantart));
                            }

                            // color the status bar. Set a complementary dark color on L,
                            // light or dark color on M (with matching status bar icons)
                            int statusBarColor = getWindow().getStatusBarColor();
                            final Palette.Swatch topColor =
                                    ColorUtils.getMostPopulousSwatch(palette);
                            if (topColor != null &&
                                    (isDark || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                                statusBarColor = ColorUtils.scrimify(topColor.getRgb(),
                                        isDark, SCRIM_ADJUSTMENT);
                                // set a light status bar on M+
                                if (!isDark && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    ViewUtils.setLightStatusBar(imageView);
                                }
                            }

                            if (statusBarColor != getWindow().getStatusBarColor()) {
                                imageView.setScrimColor(statusBarColor);
                                ValueAnimator statusBarColorAnim = ValueAnimator.ofArgb(
                                        getWindow().getStatusBarColor(), statusBarColor);
                                statusBarColorAnim.addUpdateListener(new ValueAnimator
                                        .AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        getWindow().setStatusBarColor(
                                                (int) animation.getAnimatedValue());
                                    }
                                });
                                statusBarColorAnim.setDuration(1000L);
                                statusBarColorAnim.setInterpolator(
                                        getFastOutSlowInInterpolator(DeviationActivity.this));
                                statusBarColorAnim.start();
                            }
                        }
                    });

            Palette.from(bitmap)
                    .clearFilters()
                    .generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            // color the ripple on the image spacer (default is grey)
                            deviationSpacer.setBackground(
                                    ViewUtils.createRipple(palette, 0.25f, 0.5f,
                                            ContextCompat.getColor(DeviationActivity.this, R.color.mid_grey),
                                            true));
                            // slightly more opaque ripple on the pinned image to compensate
                            // for the scrim
                            imageView.setForeground(
                                    ViewUtils.createRipple(palette, 0.3f, 0.6f,
                                            ContextCompat.getColor(DeviationActivity.this, R.color.mid_grey),
                                            true));
                        }
                    });

            // TODO should keep the background if the image contains transparency?!
            imageView.setBackground(null);
            return false;
        }

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                    Target<Drawable> target, boolean isFirstResource) {
            return false;
        }
    };

    private View.OnFocusChangeListener enterCommentFocus = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            // kick off an anim (via animated state list) on the post button. see
            // @drawable/ic_add_comment
            postComment.setActivated(hasFocus);

            // prevent content hovering over image when not pinned.
            if(hasFocus) {
                imageView.bringToFront();
                imageView.setOffset(-imageView.getHeight());
                imageView.setImmediatePin(true);
            }
        }
    };

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            final int scrollY = deviationDescription.getTop();
            imageView.setOffset(scrollY);
//            fab.setOffset(fabOffset + scrollY);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            // as we animate the main image's elevation change when it 'pins' at it's min height
            // a fling can cause the title to go over the image before the animation has a chance to
            // run. In this case we short circuit the animation and just jump to state.
            imageView.setImmediatePin(newState == RecyclerView.SCROLL_STATE_SETTLING);
        }
    };

    private RecyclerView.OnFlingListener flingListener = new RecyclerView.OnFlingListener() {
        @Override
        public boolean onFling(int velocityX, int velocityY) {
            imageView.setImmediatePin(true);
            return false;
        }
    };

//    private View.OnClickListener fabClick = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            if (deviantartPrefs.isLoggedIn()) {
//                fab.toggle();
//                doLike();
//            } else {
//                final Intent login = new Intent(DeviationActivity.this, DeviantartLogin.class);
//                FabTransform.addExtras(login, ContextCompat.getColor(DeviationActivity.this, R
//                        .color.dribbble), R.drawable.ic_heart_empty_56dp);
//                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation
//                        (DeviationActivity.this, fab, getString(R.string.transition_dribbble_login));
//                startActivityForResult(login, RC_LOGIN_LIKE, options.toBundle());
//            }
//        }
//    };

    void loadComments() {
        final Call<CommentResponse> commentsCall = deviantartPrefs.getApi().getComments(deviation.deviationid);

        commentsCall.enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                final CommentResponse commentResponse = response.body();
                if(commentResponse != null) {
                    if (commentResponse.thread != null && !commentResponse.thread.isEmpty()) {
                        adapter.addComments(commentResponse.thread);
                    }
                }
            }

            @Override public void onFailure(Call<CommentResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    void setResultAndFinish() {
        final Intent resultData = new Intent();
        resultData.putExtra(RESULT_EXTRA_SHOT_ID, deviation.id);
        setResult(RESULT_OK, resultData);
        finishAfterTransition();
    }

//    void calculateFabPosition() {
//        // calculate 'natural' position i.e. with full height image. Store it for use when scrolling
//        fabOffset = imageView.getHeight() + title.getHeight() - (fab.getHeight() / 2);
//        fab.setOffset(fabOffset);
//
//        // calculate min position i.e. pinned to the collapsed image when scrolled
//        fab.setMinOffset(imageView.getMinimumHeight() - (fab.getHeight() / 2));
//    }

//    void doLike() {
//        performingLike = true;
//        if (fab.isChecked()) {
//            final Call<Like> likeCall = deviantartPrefs.getApi().like(deviation.deviationid);
//            likeCall.enqueue(new Callback<Like>() {
//                @Override
//                public void onResponse(Call<Like> call, Response<Like> response) {
//                    performingLike = false;
//                    Log.d("response", response.body().toString());
//                }
//
//                @Override
//                public void onFailure(Call<Like> call, Throwable t) {
//                    performingLike = false;
//                }
//            });
//        } else {
//            final Call<Like> unlikeCall = deviantartPrefs.getApi().unlike(deviation.deviationid);
//            unlikeCall.enqueue(new Callback<Like>() {
//                @Override
//                public void onResponse(Call<Like> call, Response<Like> response) {
//                    performingLike = false;
//                }
//
//                @Override
//                public void onFailure(Call<Like> call, Throwable t) {
//                    performingLike = false;
//                }
//            });
//        }
//    }

    boolean isOP(UUID playerId) {
        return deviation.content != null && deviation.getDeviationid() == playerId;
    }

//    private void checkLiked() {
//        if (deviation != null && deviantartPrefs.isLoggedIn()) {
//
//            if(deviation.is_favourited){
//                fab.setChecked(true);
//            }else{
//                fab.setChecked(false);
//                fab.jumpDrawablesToCurrentState();
//            }
//        }
//    }

//    private void setupCommenting() {
//        allowComment = !deviantartPrefs.isLoggedIn()
//                || (deviantartPrefs.isLoggedIn());
//        if (allowComment && commentFooter == null) {
//            commentFooter = getLayoutInflater().inflate(R.layout.deviantart_enter_comment,
//                    commentsList, false);
//            userAvatar = (ForegroundImageView) commentFooter.findViewById(R.id.avatar);
//            enterComment = (EditText) commentFooter.findViewById(R.id.comment);
//            postComment = (ImageButton) commentFooter.findViewById(R.id.post_comment);
//            enterComment.setOnFocusChangeListener(enterCommentFocus);
//        } else if (!allowComment && commentFooter != null) {
//            adapter.removeCommentingFooter();
//            commentFooter = null;
//            Toast.makeText(getApplicationContext(),
//                    R.string.prospects_cant_post, Toast.LENGTH_SHORT).show();
//        }
//
//        if (allowComment
//                && deviantartPrefs.isLoggedIn()
//                && !TextUtils.isEmpty(deviantartPrefs.getUserAvatar())) {
//            GlideApp.with(this)
//                    .load(deviantartPrefs.getUserAvatar())
//                    .circleCrop()
//                    .placeholder(R.drawable.ic_player)
//                    .transition(withCrossFade())
//                    .into(userAvatar);
//        }
//    }

    class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int EXPAND = 0x1;
        private static final int COLLAPSE = 0x2;
        private static final int COMMENT_LIKE = 0x3;
        private static final int REPLY = 0x4;

        private final List<Comment> comments = new ArrayList<>();
        final Transition expandCollapse;
        private final View description;
        private View footer;

        private boolean loading;
        private boolean noComments;
        int expandedCommentPosition = RecyclerView.NO_POSITION;

        CommentsAdapter(
                @NonNull View description,
                @Nullable View footer,
                long commentCount,
                long expandDuration) {
            this.description = description;
            this.footer = footer;
            noComments = commentCount == 0L;
            loading = !noComments;
            expandCollapse = new AutoTransition();
            expandCollapse.setDuration(expandDuration);
            expandCollapse.setInterpolator(getFastOutSlowInInterpolator(DeviationActivity.this));
            expandCollapse.addListener(new TransitionUtils.TransitionListenerAdapter() {
                @Override
                public void onTransitionStart(Transition transition) {
                    commentsList.setOnTouchListener(touchEater);
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    commentAnimator.setAnimateMoves(true);
                    commentsList.setOnTouchListener(null);
                }
            });
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0)  return R.layout.deviation_description;
            if (position == 1) {
                if (loading)  return R.layout.loading;
                if (noComments) return R.layout.deviantart_no_comments;
            }
            if (footer != null) {
                int footerPos = (loading || noComments) ? 2 : deviation.stats.getComments() + 1;
                if (position == footerPos) return R.layout.deviantart_enter_comment;
            }
            return R.layout.deviantart_comment;
        }

        @Override
        public int getItemCount() {
            int count = 1; // description
            if (!comments.isEmpty()) {
                count += comments.size();
            } else {
                count++; // either loading or no comments
            }
            if (footer != null) count++;
            return count;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case R.layout.deviation_description:
                    return new SimpleViewHolder(description);
                case R.layout.deviantart_comment:
                    return createCommentHolder(parent, viewType);
                case R.layout.loading:
                case R.layout.deviantart_no_comments:
                    return new SimpleViewHolder(
                            getLayoutInflater().inflate(viewType, parent, false));
                case R.layout.deviantart_enter_comment:
                    return new SimpleViewHolder(footer);
            }
            throw new IllegalArgumentException();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (getItemViewType(position)) {
                case R.layout.deviantart_comment:
                    bindComment((CommentViewHolder) holder, getComment(position));
                    break;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position,
                                     List<Object> partialChangePayloads) {
            if (holder instanceof CommentViewHolder) {
                bindPartialCommentChange(
                        (CommentViewHolder) holder, position, partialChangePayloads);
            } else {
                onBindViewHolder(holder, position);
            }
        }

        Comment getComment(int adapterPosition) {
            int returnValue = 0;
            if(adapterPosition > comments.size()){
                returnValue  = comments.size() - 1;
            }else{
                returnValue = adapterPosition - 1;
            }
            return comments.get(returnValue); // description
        }

        void addComments(List<Comment> newComments) {
            hideLoadingIndicator();
            noComments = false;
            comments.addAll(newComments);
            notifyItemRangeInserted(1, newComments.size());
        }

        void removeCommentingFooter() {
            if (footer == null) return;
            int footerPos = getItemCount() - 1;
            footer = null;
            notifyItemRemoved(footerPos);
        }

        private CommentViewHolder createCommentHolder(ViewGroup parent, int viewType) {
            final CommentViewHolder holder = new CommentViewHolder(
                    getLayoutInflater().inflate(viewType, parent, false));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = holder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;

                    final Comment comment = getComment(position);
                    TransitionManager.beginDelayedTransition(commentsList, expandCollapse);
                    commentAnimator.setAnimateMoves(false);

                    // collapse any currently expanded items
                    if (RecyclerView.NO_POSITION != expandedCommentPosition) {
                        notifyItemChanged(expandedCommentPosition, COLLAPSE);
                    }

                    // expand this item (if it wasn't already)
                    if (expandedCommentPosition != position) {
                        expandedCommentPosition = position;
                        notifyItemChanged(position, EXPAND);
//                        if (comment.liked == null) {
//                            final Call<Like> liked = deviantartPrefs.getApi()
//                                    .likedComment(deviation.id, comment.id);
//                            liked.enqueue(new Callback<Like>() {
//                                @Override
//                                public void onResponse(Call<Like> call, Response<Like> response) {
//                                    comment.liked = response.isSuccessful();
//                                    holder.likeHeart.setChecked(comment.liked);
//                                    holder.likeHeart.jumpDrawablesToCurrentState();
//                                }
//
//                                @Override public void onFailure(Call<Like> call, Throwable t) { }
//                            });
//                        }
                        if (enterComment != null && enterComment.hasFocus()) {
                            enterComment.clearFocus();
                            ImeUtils.hideIme(enterComment);
                        }
                        holder.itemView.requestFocus();
                    } else {
                        expandedCommentPosition = RecyclerView.NO_POSITION;
                    }
                }
            });

            holder.avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = holder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;

//                    final Comment comment = getComment(position);
//                    final Intent player = new Intent(DeviationActivity.this, PlayerActivity.class);
//                    player.putExtra(PlayerActivity.EXTRA_PLAYER, comment.user);
//                    ActivityOptions options =
//                            ActivityOptions.makeSceneTransitionAnimation(DeviationActivity.this,
//                                    Pair.create(holder.itemView,
//                                            getString(R.string.transition_player_background)),
//                                    Pair.create((View) holder.avatar,
//                                            getString(R.string.transition_player_avatar)));
//                    startActivity(player, options.toBundle());
                }
            });

            holder.reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final int position = holder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;

                    final Comment comment = getComment(position);
                    enterComment.setText("@" + comment.user.username + " ");
                    enterComment.setSelection(enterComment.getText().length());

                    // collapse the comment and scroll the reply box (in the footer) into view
                    expandedCommentPosition = RecyclerView.NO_POSITION;
                    notifyItemChanged(position, REPLY);
                    holder.reply.jumpDrawablesToCurrentState();
                    enterComment.requestFocus();
                    commentsList.smoothScrollToPosition(getItemCount() - 1);
                }
            });

            holder.likeHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (deviantartPrefs.isLoggedIn()) {
                        final int position = holder.getAdapterPosition();
                        if (position == RecyclerView.NO_POSITION) return;

                        final Comment comment = getComment(position);
//                        if (comment.liked == null || !comment.liked) {
//                            comment.liked = true;
//                            comment.likes_count++;
//                            holder.likesCount.setText(String.valueOf(comment.likes_count));
//                            notifyItemChanged(position, COMMENT_LIKE);
//                            final Call<Like> likeCommentCall =
//                                    deviantartPrefs.getApi().likeComment(shot.id, comment.id);
//                            likeCommentCall.enqueue(new Callback<Like>() {
//                                @Override
//                                public void onResponse(Call<Like> call, Response<Like> response) { }
//
//                                @Override
//                                public void onFailure(Call<Like> call, Throwable t) { }
//                            });
//                        } else {
//                            comment.liked = false;
//                            comment.likes_count--;
//                            holder.likesCount.setText(String.valueOf(comment.likes_count));
//                            notifyItemChanged(position, COMMENT_LIKE);
//                            final Call<Void> unlikeCommentCall =
//                                    deviantartPrefs.getApi().unlikeComment(shot.id, comment.id);
//                            unlikeCommentCall.enqueue(new Callback<Void>() {
//                                @Override
//                                public void onResponse(Call<Void> call, Response<Void> response) { }
//
//                                @Override
//                                public void onFailure(Call<Void> call, Throwable t) { }
//                            });
//                        }
//                    } else {
                        holder.likeHeart.setChecked(false);
                        startActivityForResult(new Intent(DeviationActivity.this,
                                DeviantartLogin.class), RC_LOGIN_LIKE);
                    }
                }
            });

            holder.likesCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = holder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;

//                    final Comment comment = getComment(position);
////                    final Call<List<Like>> commentLikesCall =
////                            deviantartPrefs.getApi().getCommentLikes(shot.id, comment.id);
////                    commentLikesCall.enqueue(new Callback<List<Like>>() {
////                        @Override
////                        public void onResponse(Call<List<Like>> call,
////                                               Response<List<Like>> response) {
////                            // TODO something better than this.
////                            StringBuilder sb = new StringBuilder("Liked by:\n\n");
////                            for (Like like : response.body()) {
////                                if (like.user != null) {
////                                    sb.append("@");
////                                    sb.append(like.user.username);
////                                    sb.append("\n");
////                                }
////                            }
////                            Toast.makeText(getApplicationContext(), sb.toString(), Toast
////                                    .LENGTH_SHORT).show();
////                        }
//
//                        @Override
//                        public void onFailure(Call<List<Like>> call, Throwable t) { }
//                    });
                }
            });

            return holder;
        }

        private void bindComment(CommentViewHolder holder, Comment comment) {
            final int position = holder.getAdapterPosition();
            final boolean isExpanded = position == expandedCommentPosition;

            GlideApp.with(DeviationActivity.this)
                    .load(comment.user.usericon)
                    .circleCrop()
                    .placeholder(R.drawable.avatar_placeholder)
                    .override(largeAvatarSize, largeAvatarSize)
                    .transition(withCrossFade())
                    .into(holder.avatar);

            holder.author.setText(comment.user.username.toLowerCase());
            holder.author.setOriginalPoster(isOP(comment.user.userid));
//            holder.timeAgo.setText(comment.created_at == null ? "" :
//                    DateUtils.getRelativeTimeSpanString(comment.created_at.getTime(),
//                            System.currentTimeMillis(),
//                            DateUtils.SECOND_IN_MILLIS)
//                            .toString().toLowerCase());
            HtmlUtils.setTextWithNiceLinks(holder.commentBody,
                    fromHtml(comment.body));
//            holder.likeHeart.setChecked(comment.liked != null && comment.liked);
            holder.likeHeart.setEnabled(comment.user.userid != deviantartPrefs.getUserId());
//            holder.likesCount.setText(String.valueOf(comment.likes_count));
            setExpanded(holder, isExpanded);
        }

        @SuppressWarnings("deprecation")
        public Spanned fromHtml(String html){
            Spanned result;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
            } else {
                result = Html.fromHtml(html);
            }
            return result;
        }


        private void setExpanded(CommentViewHolder holder, boolean isExpanded) {
            holder.itemView.setActivated(isExpanded);
            holder.reply.setVisibility((isExpanded && allowComment) ? View.VISIBLE : View.GONE);
            holder.likeHeart.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            holder.likesCount.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        }

        private void bindPartialCommentChange(
                CommentViewHolder holder, int position, List<Object> partialChangePayloads) {
            // for certain changes we don't need to rebind data, just update some view state
            if ((partialChangePayloads.contains(EXPAND)
                    || partialChangePayloads.contains(COLLAPSE))
                    || partialChangePayloads.contains(REPLY)) {
                setExpanded(holder, position == expandedCommentPosition);
            } else if (partialChangePayloads.contains(COMMENT_LIKE)) {
                return; // nothing to do
            } else {
                onBindViewHolder(holder, position);
            }
        }

        private void hideLoadingIndicator() {
            if (!loading) return;
            loading = false;
            notifyItemRemoved(1);
        }
    }

    static class SimpleViewHolder extends RecyclerView.ViewHolder {

        SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.player_avatar) ImageView avatar;
        @BindView(R.id.comment_author) AuthorTextView author;
        @BindView(R.id.comment_time_ago) TextView timeAgo;
        @BindView(R.id.comment_text) TextView commentBody;
        @BindView(R.id.comment_reply) ImageButton reply;
        @BindView(R.id.comment_like) CheckableImageButton likeHeart;
        @BindView(R.id.comment_likes_count) TextView likesCount;

        CommentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    /**
     * A {@link RecyclerView.ItemAnimator} which allows disabling move animations. RecyclerView
     * does not like animating item height changes. {@link android.transition.ChangeBounds} allows
     * this but in order to simultaneously collapse one item and expand another, we need to run the
     * Transition on the entire RecyclerView. As such it attempts to move views around. This
     * custom item animator allows us to stop RecyclerView from trying to handle this for us while
     * the transition is running.
     */
    static class CommentAnimator extends SlideInItemAnimator {

        private boolean animateMoves = false;

        CommentAnimator() {
            super();
        }

        void setAnimateMoves(boolean animateMoves) {
            this.animateMoves = animateMoves;
        }

        @Override
        public boolean animateMove(
                RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
            if (!animateMoves) {
                dispatchMoveFinished(holder);
                return false;
            }
            return super.animateMove(holder, fromX, fromY, toX, toY);
        }
    }

}
