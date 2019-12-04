package com.zemose.network

import com.touchcarwash_driver.Temp
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientInstance {

    private var retrofit: Retrofit? = null

    // creating retrofit instance if not created yet
    val retrofitInstance: Retrofit?

    get() {

        if (retrofit == null) {
            retrofit = retrofit2.Retrofit.Builder()
                    .baseUrl(Temp.weblink)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }

        return retrofit

    }

}