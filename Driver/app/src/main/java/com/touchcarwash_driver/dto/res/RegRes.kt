package com.touchcarwash_driver.dto.res
import com.google.gson.annotations.SerializedName


data class RegRes(
    val `data`: Data,
    val response: Response
) {
    data class Data(
        val driveimgsig: String,
        val imgsig: String,
        val name: String,
        val registernumber: String,
        val sn: String,
        val washvehicleid: String
    )

    data class Response(
        val status: String
    )
}