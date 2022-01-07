package com.mr0kaushik.newsapplication.api

import com.mr0kaushik.newsapplication.Constants.Companion.API_KEY
import com.mr0kaushik.newsapplication.data.TopHeadlinesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPI {

    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") countryCode: String = "in",
        @Query("category") category: String,
        @Query("page") page: Int = 1,
        @Query("apiKey") apiKey: String = API_KEY,
    ): Response<TopHeadlinesResponse>

    @GET("v2/everything")
    suspend fun searchForNew(
        @Query("q") searchQuery: String,
        @Query("page") page: Int = 1,
        @Query("apiKey") apiKey: String = API_KEY,
    ): Response<TopHeadlinesResponse>
}