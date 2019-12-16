package com.touchcarwash_driver.network

import com.touchcarwash_driver.dto.res.DirectionRes
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionInterface {

    @GET("json")
    fun getDirection(
        @Query("mode") mode: String,
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") APIkey: String
    ): Single<DirectionRes>

}