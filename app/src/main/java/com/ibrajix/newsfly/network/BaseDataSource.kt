/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.network

import retrofit2.Response


abstract class BaseDataSource {

    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiStatus<T> {
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    return ApiStatus.Success(body)
                }
            }
            return ApiStatus.Error(null, throwable = Throwable())

        } catch (e: Exception) {
            //log exception here
            return ApiStatus.Error(null, throwable = Throwable())
        }
    }
}
