/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.network

import com.ibrajix.newsfly.model.responses.AllNewsResponse
import com.ibrajix.newsfly.model.responses.PopularNewsResponse
import com.ibrajix.newsfly.utils.Urls
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    //get all recent news in the us
    @GET("top-headlines?country=us")
    suspend fun getRecentNews(
            @Query("apiKey") apiKey: String = Urls.API_KEY,
            @Query("page") page: Int?,
            @Query("pageSize") pageSize: Int
    ) : AllNewsResponse

    //get all popular news related to apple
    @GET("top-headlines")
    suspend fun getPopularNews(
            @Query("country") country: String = "us",
            @Query("apiKey") apiKey: String = Urls.API_KEY,
            @Query("pageSize") pageSize: Int = 5
    ) : PopularNewsResponse

    //get all news when searching
    @GET("everything")
    suspend fun searchForNews(
            @Query("q") q: String,
            @Query("apiKey") apiKey: String = Urls.API_KEY
    ): Response<AllNewsResponse>

}