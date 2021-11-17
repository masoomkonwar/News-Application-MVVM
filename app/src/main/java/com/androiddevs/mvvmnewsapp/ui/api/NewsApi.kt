package com.androiddevs.mvvmnewsapp.ui.api

import com.androiddevs.mvvmnewsapp.ui.utils.Constants.Companion.API_KEY
import com.androiddevs.mvvmnewsapp.ui.models.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("v2/top-headlines")
    suspend fun getBreakingNews(
        @Query("country")
        countryCode: String = "us",
        @Query("page")
        pageNum : Int = 1,
        @Query("apikey")
        apiKey : String = API_KEY
        ) : Response<NewsResponse>
    @GET("v2/everything")
    suspend fun getAllNews(
        @Query("q")
        seacrhQuery: String,
        @Query("page")
        pageNum : Int = 1,
        @Query("apikey")
        apiKey : String = API_KEY
    ) : Response<NewsResponse>

}