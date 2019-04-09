
package io.foshizzle.data;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.foshizzle.BuildConfig;
import io.foshizzle.data.api.AuthInterceptor;
import io.foshizzle.data.api.DenvelopingConverter;
import io.foshizzle.data.api.deviantart.DeviantartService;
import io.foshizzle.data.api.dribbble.DribbbleSearchConverter;
import io.foshizzle.data.api.dribbble.DribbbleSearchService;
import io.foshizzle.data.api.dribbble.DribbbleService;
import io.foshizzle.data.api.producthunt.ProductHuntService;
import io.foshizzle.data.prefs.DeviantartPrefs;
import io.foshizzle.data.prefs.DribbblePrefs;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Base class for loading data; extending types are responsible for providing implementations of
 * {@link #onDataLoaded(Object)} to do something with the data and {@link #cancelLoading()} to
 * cancel any activity.
 */
public abstract class BaseDataManager<T> implements DataLoadingSubject {

    private final AtomicInteger loadingCount;
    private final DribbblePrefs dribbblePrefs;
    private final DeviantartPrefs deviantartPrefs;
    private DribbbleSearchService dribbbleSearchApi;
    private ProductHuntService productHuntApi;
    private List<DataLoadingSubject.DataLoadingCallbacks> loadingCallbacks;

    public BaseDataManager(@NonNull Context context) {
        loadingCount = new AtomicInteger(0);
        dribbblePrefs = DribbblePrefs.get(context);
        deviantartPrefs = DeviantartPrefs.get(context);
    }

    public abstract void onDataLoaded(T data);

    public abstract void cancelLoading();

    @Override
    public boolean isDataLoading() {
        return loadingCount.get() > 0;
    }

    public DribbblePrefs getDribbblePrefs() {
        return dribbblePrefs;
    }

    public DribbbleService getDribbbleApi() {
        return dribbblePrefs.getApi();
    }

    public ProductHuntService getProductHuntApi() {
        if (productHuntApi == null) createProductHuntApi();
        return productHuntApi;
    }

    public DribbbleSearchService getDribbbleSearchApi() {
        if (dribbbleSearchApi == null) createDribbbleSearchApi();
        return dribbbleSearchApi;
    }

    public DeviantartPrefs getDeviantartPrefs() {
        return  deviantartPrefs;
    }

    public DeviantartService getDeviantartApi() {
        return deviantartPrefs.getApi();
    }


    @Override
    public void registerCallback(DataLoadingSubject.DataLoadingCallbacks callback) {
        if (loadingCallbacks == null) {
            loadingCallbacks = new ArrayList<>(1);
        }
        loadingCallbacks.add(callback);
    }

    @Override
    public void unregisterCallback(DataLoadingSubject.DataLoadingCallbacks callback) {
        if (loadingCallbacks != null && loadingCallbacks.contains(callback)) {
            loadingCallbacks.remove(callback);
        }
    }

    protected void loadStarted() {
        if (0 == loadingCount.getAndIncrement()) {
            dispatchLoadingStartedCallbacks();
        }
    }

    protected void loadFinished() {
        if (0 == loadingCount.decrementAndGet()) {
            dispatchLoadingFinishedCallbacks();
        }
    }

    protected void resetLoadingCount() {
        loadingCount.set(0);
    }

    protected static void setPage(List<? extends PlaidItem> items, int page) {
        for (PlaidItem item : items) {
            item.page = page;
        }
    }

    protected static void setDataSource(List<? extends PlaidItem> items, String dataSource) {
        for (PlaidItem item : items) {
            item.dataSource = dataSource;
        }
    }

    private void dispatchLoadingStartedCallbacks() {
        if (loadingCallbacks == null || loadingCallbacks.isEmpty()) return;
        for (DataLoadingCallbacks loadingCallback : loadingCallbacks) {
            loadingCallback.dataStartedLoading();
        }
    }

    private void dispatchLoadingFinishedCallbacks() {
        if (loadingCallbacks == null || loadingCallbacks.isEmpty()) return;
        for (DataLoadingCallbacks loadingCallback : loadingCallbacks) {
            loadingCallback.dataFinishedLoading();
        }
    }

    private void createDribbbleSearchApi() {
        dribbbleSearchApi = new Retrofit.Builder()
                .baseUrl(DribbbleSearchService.ENDPOINT)
                .addConverterFactory(new DribbbleSearchConverter.Factory())
                .build()
                .create((DribbbleSearchService.class));
    }



    private void createProductHuntApi() {
        final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(BuildConfig.PROCUCT_HUNT_DEVELOPER_TOKEN))
                .build();
        final Gson gson = new Gson();
        productHuntApi = new Retrofit.Builder()
                .baseUrl(ProductHuntService.ENDPOINT)
                .client(client)
                .addConverterFactory(new DenvelopingConverter(gson))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ProductHuntService.class);
    }

}
