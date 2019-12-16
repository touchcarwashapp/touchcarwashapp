package com.zemose.network

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory



object GmapRetrofitInstance {

    private var retrofit: Retrofit? = null

    // creating retrofit instance if not created yet
    val retrofitInstance: Retrofit?

    get() {

        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/maps/api/directions/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
        }

        return retrofit

    }

}