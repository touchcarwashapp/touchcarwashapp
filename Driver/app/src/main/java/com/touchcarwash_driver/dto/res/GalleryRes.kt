package com.touchcarwash_driver.dto.res
import com.google.gson.annotations.SerializedName


data class GalleryRes(
    val `data`: List<String>,
    val response: Response
) {
    data class Response(
        val status: String
    )
}