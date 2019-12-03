package com.zemose.network

import com.touchcarwash_driver.dto.res.RegRes
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST


interface UserService {

    @FormUrlEncoded
    @POST("register_driver.php")
    fun signUp(@Header("Content-Type") contentType: String,
               @Field("mobile") mobile: String,
               @Field("fcmid") fcmid: String): Call<RegRes>
//
//    @Multipart
//    @POST("customer/uploadCustomerImage")
//    fun photoUpdate(
//        @Header("x-access-token") token: String,
//        @Part("customerId") customerId: RequestBody,
//        @Part customerIcon: MultipartBody.Part
//    ): Call<DefaultRes>


}
