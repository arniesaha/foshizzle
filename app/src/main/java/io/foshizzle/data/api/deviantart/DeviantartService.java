package io.foshizzle.data.api.deviantart;

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


import java.util.UUID;

import io.foshizzle.data.api.deviantart.model.CommentResponse;
import io.foshizzle.data.api.deviantart.model.DailyDeviation;
import io.foshizzle.data.api.deviantart.model.Deviation;
import io.foshizzle.data.api.deviantart.model.Gallery;
import io.foshizzle.data.api.deviantart.model.Like;
import io.foshizzle.data.api.deviantart.model.Popular;
import io.foshizzle.data.api.deviantart.model.User;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Deviantart API - https://www.deviantart.com/developers/rss
 */
public interface DeviantartService {

    String ENDPOINT = "https://www.deviantart.com/api/";
    String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss Z";
    int PER_PAGE_MAX = 100;
    int PER_PAGE_DEFAULT = 12;



//    @GET("/browse/popular")
//    Call<List<UserFeed>> getPopularDeviations(@Query("access_token") String accessToken, @Query("limit") int limit, @Query("offset") int offset);

    @GET("v1/oauth2/user/whoami")
    Call<User> getAuthenticatedUser();

    @GET("v1/oauth2/browse/popular")
    Call<Popular> getPopularDeviations(@Query("category_path") String category_path,
                                       @Query("q") String q,
                                       @Query("limit") int limit,
                                       @Query("offset") int offset);

    @GET("v1/oauth2/browse/dailydeviations")
    Call<DailyDeviation> getDailyDeviations();

    @GET("v1/oauth2/gallery/all")
    Call<Gallery> getMyDeviations(@Query("username") String username);

    @GET("v1/oauth2/gallery")
    Call<Gallery> getFeatureDeviations(@Query("folderid") String folderid);

    @GET("v1/oauth2/deviation/{id}")
    Call<Deviation> getDeviation(@Path("id") long deviationId);


     /* Comments */

    @GET("v1/oauth2/comments/deviation/{deviationid}")
    Call<CommentResponse> getComments(@Path("deviationid") UUID deviationId);

        /* Like */

    @POST("v1/oauth2/collections/fave/{deviationid}")
    Call<Like> like(@Path("deviationid") UUID deviationId);

      /* Like */

    @POST("v1/oauth2/collections/unfave/{deviationid}")
    Call<Like> unlike(@Path("deviationid") UUID deviationId);

}
