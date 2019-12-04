package com.zemose.network

import com.touchcarwash_driver.dto.res.DefaultRes
import com.touchcarwash_driver.dto.res.RegRes
import com.touchcarwash_driver.dto.res.JobsRes
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface UserService {

    //register
    @FormUrlEncoded
    @POST("registration_driver.php")
    fun signUp(
            @Header("Content-Type") contentType: String,
            @Field("mobile") mobile: String,
            @Field("fcmid") fcmid: String): Call<RegRes>

    //upload photo
    @Multipart
    @POST("updatedriverpic.php")
    fun photoUpdate(
            @Part("userid") userId: RequestBody,
            @Part("photoexist") photoExist: RequestBody,
            @Part driverpic: MultipartBody.Part
    ): Call<DefaultRes>

    //remove photo
    @FormUrlEncoded
    @POST("removePhoto.php")
    fun removeProfilePhoto(
            @Header("Content-Type") contentType: String,
            @Field("userId") userId: String): Call<DefaultRes>


    //isOnline
    @FormUrlEncoded
    @POST("updatedriver_onlinestatus.php")
    fun availabilityStatus(
            @Header("Content-Type") contentType: String,
            @Field("userid") userId: String,
            @Field("status") isOnline: String): Call<DefaultRes>

    //update driver location
    @FormUrlEncoded
    @POST("updatedriver_worklocation.php")
    fun updateLocation(
            @Header("Content-Type") contentType: String,
            @Field("userid") userId: String,
            @Field("workaddress") address: String,
            @Field("worklocation") location: String): Call<DefaultRes>

    //update radius
    @FormUrlEncoded
    @POST("updatedriver_workradius.php")
    fun updateRadius(
            @Header("Content-Type") contentType: String,
            @Field("userid") userId: String,
            @Field("radius") radius: String): Call<DefaultRes>

    //get confirmed / pending jobs
    @FormUrlEncoded
    @POST("getdriver_pending_confirmed_jobs.php")
    fun getJobs(
            @Header("Content-Type") contentType: String,
            @Field("userid") userId: String): Call<JobsRes>


























}
