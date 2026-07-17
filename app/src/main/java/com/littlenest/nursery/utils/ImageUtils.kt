package com.littlenest.nursery.utils

object ImageUtils {

    fun resolveImageUrl(uploadsBaseUrl: String, url: String): String {
        return if (url.startsWith("http")) {
            url
        } else {
            "$uploadsBaseUrl$url"
        }
    }
}