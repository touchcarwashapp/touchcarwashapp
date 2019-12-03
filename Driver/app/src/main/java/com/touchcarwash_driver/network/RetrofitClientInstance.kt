package com.zemose.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientInstance {

    private var retrofit: Retrofit? = null

    // creating retrofit instance if not created yet
    val retrofitInstance: Retrofit?

    get() {

        if (retrofit == null) {
            retrofit = retrofit2.Retrofit.Builder()
                    .baseUrl("http://touchcarwashapp.xyz/touchcarwash/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }

        return retrofit

    }

}