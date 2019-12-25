package com.zemose.network

import com.touchcarwash_driver.dto.res.*
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
    @POST("updatedriverpic.php")
    fun removeProfilePhoto(
            @Header("Content-Type") contentType: String,
            @Field("photoexist") photoExist: String,
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

    //get confirmed / pending jobs count
    @FormUrlEncoded
    @POST("getdriver_pending_confirmed_jobs.php")
    fun getJobsCount(
            @Header("Content-Type") contentType: String,
            @Field("userid") userId: String): Call<JobsRes>

    //get pending jobs list
    @FormUrlEncoded
    @POST("driver_getpendingorder.php")
    fun getPendingList(
            @Header("Content-Type") contentType: String,
            @Field("userid") userId: String): Call<CommonJobsRes>

    //get confirmed jobs list
    @FormUrlEncoded
    @POST("driver_getconifmredorder.php")
    fun getConfirmedList(
            @Header("Content-Type") contentType: String,
            @Field("userid") userId: String): Call<CommonJobsRes>

    //confirm Order
    @FormUrlEncoded
    @POST("driver_confirmorder.php")
    fun confirmOrder(
            @Header("Content-Type") contentType: String,
            @Field("orderid") orderId: String): Call<DefaultRes>

    //start tracking
    @FormUrlEncoded
    @POST("update_washvehiclestart.php")
    fun startTracking(
            @Header("Content-Type") contentType: String,
            @Field("orderid") orderId: String): Call<DefaultRes>

    //reached destination
    @FormUrlEncoded
    @POST("update_washvehiclereached.php")
    fun reachedDestination(
            @Header("Content-Type") contentType: String,
            @Field("orderid") orderId: String,
            @Field("totaltime") totalTime: String): Call<DefaultRes>

    //start wash
    @FormUrlEncoded
    @POST("update_startwash.php")
    fun startWash(
            @Header("Content-Type") contentType: String,
            @Field("orderid") orderId: String): Call<DefaultRes>

    //finish wash
    @Multipart
    @POST("finishwash.php")
    fun finishWash(
            @Part("orderid") orderId: RequestBody,
            @Part("bf_front_exist") beforeFrontExist: RequestBody,
            @Part("bf_back_exist") beforeBackExist: RequestBody,
            @Part("bf_right_exist") beforeRightExist: RequestBody,
            @Part("bf_left_exist") beforeLeftExist: RequestBody,
            @Part("bf_top_exist") beforeTopExist: RequestBody,
            @Part("af_front_exist") AfterFrontExist: RequestBody,
            @Part("af_back_exist") AfterBackExist: RequestBody,
            @Part("af_right_exist") AfterRightExist: RequestBody,
            @Part("af_left_exist") AfterLeftExist: RequestBody,
            @Part("af_top_exist") AfterTopExist: RequestBody,
            @Part bf_front: MultipartBody.Part,
            @Part bf_back: MultipartBody.Part,
            @Part bf_right: MultipartBody.Part,
            @Part bf_left: MultipartBody.Part,
            @Part bf_top: MultipartBody.Part,
            @Part af_front: MultipartBody.Part,
            @Part af_back: MultipartBody.Part,
            @Part af_right: MultipartBody.Part,
            @Part af_left: MultipartBody.Part,
            @Part af_top: MultipartBody.Part,
            @Part("washids") washIds: RequestBody,
            @Part("acrsids") accessIds: RequestBody,
            @Part("nettotal") netTotal: RequestBody,
            @Part("extraamount") extraAmount: RequestBody,
            @Part("discount") discount: RequestBody,
            @Part("grandtotal") grandTotal: RequestBody,
            @Part("recieved") received: RequestBody,
            @Part("balance") balance: RequestBody,
            @Part("washtime") washTime: RequestBody,
            @Part("userid") userId: RequestBody

    ): Call<DefaultRes>


    //rate driver
    @FormUrlEncoded
    @POST("update_driverreview.php")
    fun rateDriver(
            @Header("Content-Type") contentType: String,
            @Field("orderid") orderId: String,
            @Field("review") review: String): Call<DefaultRes>

    //get amounts
    @FormUrlEncoded
    @POST("getmyaccountdriver.php")
    fun getAmounts(
            @Header("Content-Type") contentType: String,
            @Field("userid") userId: String): Call<AmountRes>

    //get gallery images
    @FormUrlEncoded
    @POST("getmygallery_driver.php")
    fun getGallery(
            @Header("Content-Type") contentType: String,
            @Field("userid") userId: String): Call<GalleryRes>

}
