package com.example.lajusta

import com.example.lajusta.data.model.GoogleCustomSearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleCustomSearchService {
    @GET("/customsearch/v1")
    fun search(
        @Query("key") apiKey: String,
        @Query("cx") searchEngineId: String,
        @Query("q") query: String,
        @Query("searchType") searchType: String = "image",
        @Query("num") numResults: Int = 1
    ): Call<GoogleCustomSearchResponse>
}