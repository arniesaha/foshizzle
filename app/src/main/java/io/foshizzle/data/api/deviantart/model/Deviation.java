package io.foshizzle.data.api.deviantart.model;

import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.text.Spanned;
import android.text.TextUtils;


import java.util.List;
import java.util.UUID;

import io.foshizzle.data.PlaidItem;
import io.foshizzle.util.DribbbleUtils;

/**
 * Created by macbook on 02/04/17.
 */


public class Deviation extends PlaidItem implements Parcelable {


    public final UUID deviationid;
    public final String description;
    public final Content content;
    public final int published_time;
    public final Stats stats;
    public final List<Thumbs> thumbs;

    public final boolean is_favourited;
    public final User author;

    // todo move this into a decorator
    public boolean hasFadedIn = false;
    public Spanned parsedDescription;


    public Deviation(long id, String title, UUID deviationid, String description, String html_url, Content content, int published_time, Stats stats, List<Thumbs> thumbs, boolean is_favourited, User user){
        super(id, title, html_url);
        this.deviationid = deviationid;
        this.description = description;
        this.content = content;
        this.published_time = published_time;
        this.stats = stats;
        this.thumbs = thumbs;
        this.is_favourited = is_favourited;
        this.author = user;
    }

    public Deviation(Parcel in) {
        super(in.readLong(), in.readString(), in.readString());
        deviationid = (UUID) in.readValue(UUID.class.getClassLoader());
        description = in.readString();
        content = (Content) in.readValue(Content.class.getClassLoader());
        published_time = in.readInt();
        stats = (Stats) in.readValue(Stats.class.getClassLoader());
        thumbs = (List<Thumbs>) in.readValue(Thumbs.class.getClassLoader());
        is_favourited =  Boolean.valueOf(in.readString());
        author = (User) in.readValue(User.class.getClassLoader());
        hasFadedIn = in.readByte() != 0x00;
    }

    public static class Builder {
        private long id;
        private String title;
        private String html_url;
        private UUID deviationid;
        private String description;
        private Content content;
        private int published_time;
        private Stats stats;
        private List<Thumbs> thumbs;
        private boolean is_favourited;
        private User user;

        public Deviation.Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Deviation.Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Deviation.Builder setHtmlUrl(String html_url) {
            this.html_url = html_url;
            return this;
        }

        public Deviation.Builder setDeviationId(UUID deviationid) {
            this.deviationid = deviationid;
            return this;
        }

        public Deviation.Builder setDescription(String description) {
            this.description = description;
            return this;
        }


        public Deviation.Builder setContent(Content content) {
            this.content = content;
            return this;
        }

        public Deviation.Builder setPublishedTime(int published_time){
            this.published_time = published_time;
            return this;
        }

        public Deviation.Builder setStats(Stats stats){
            this.stats = stats;
            return this;
        }


        public Deviation.Builder setThumbs(List<Thumbs> thumbs){
            this.thumbs = thumbs;
            return this;
        }


        public Deviation.Builder setIsFavourited(boolean is_favourited){
            this.is_favourited = is_favourited;
            return this;
        }

        public Deviation.Builder setUser(User user){
            this.user = user;
            return this;
        }


        public Deviation build() {
            return new Deviation(id, title, deviationid, description, html_url, content, published_time, stats, thumbs, is_favourited, user);
        }

    }

     /* Parcelable stuff */

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Deviation> CREATOR = new Parcelable.Creator<Deviation>() {
        @Override
        public Deviation createFromParcel(Parcel in) {
            return new Deviation(in);
        }

        @Override
        public Deviation[] newArray(int size) {
            return new Deviation[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(url);
        dest.writeValue(deviationid);
        dest.writeString(description);
        dest.writeValue(content);
        dest.writeInt(published_time);
        dest.writeValue(stats);
        dest.writeValue(thumbs);
        dest.writeString(String.valueOf(is_favourited));
        dest.writeValue(author);
        dest.writeByte((byte) (hasFadedIn ? 0x01 : 0x00));

    }

    public Spanned getParsedDescription(ColorStateList linkTextColor,
                                        @ColorInt int linkHighlightColor) {
        if (parsedDescription == null && !TextUtils.isEmpty(description)) {
            parsedDescription = DribbbleUtils.parseDribbbleHtml(description, linkTextColor,
                    linkHighlightColor);
        }
        return parsedDescription;
    }


    public UUID getDeviationid(){
        return this.deviationid;
    }
}
