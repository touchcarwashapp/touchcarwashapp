package com.touchcarwash_driver.dto.res

data class DefaultRes(
        val `data`: String,
        val response: Response
) {
    data class Response(val status: String)
}