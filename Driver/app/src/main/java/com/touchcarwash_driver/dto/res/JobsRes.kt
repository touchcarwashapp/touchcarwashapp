package com.touchcarwash_driver.dto.res


data class JobsRes(
    val `data`: Data,
    val response: Response
) {
    data class Data(
        val confirmed: Int,
        val pending: Int
    )

    data class Response(
        val status: String
    )
}