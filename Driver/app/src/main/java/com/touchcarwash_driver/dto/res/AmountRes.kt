package com.touchcarwash_driver.dto.res
import com.google.gson.annotations.SerializedName


data class AmountRes(
    val `data`: Data,
    val response: Response
) {
    data class Data(
        val balance: Int,
        val collected: Int,
        val recieved: Int
    )

    data class Response(
        val status: String
    )
}