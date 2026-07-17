package com.littlenest.nursery.utils

import com.google.gson.Gson
import retrofit2.Response

data class ApiError(
    val message: String?
)

fun Response<*>.parseError(): String {
    val httpCode = code()
    return try {
        val errorBody = errorBody()?.string()

        if (errorBody.isNullOrEmpty()) {
            return "Error: $httpCode"
        }

        val apiError = Gson().fromJson(errorBody, ApiError::class.java)
        apiError?.message ?: "Error: $httpCode"

    } catch (e: Exception) {
        "Error: $httpCode"
    }
}