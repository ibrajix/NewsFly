/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.network

import com.google.gson.Gson
import com.ibrajix.newsfly.model.responses.ErrorResponse
import retrofit2.Response


abstract class BaseDataSource {

    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Resource<T> {

        try {
            val response = apiCall()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    return Resource.success(body)
                }
            }

            else{
                val message: ErrorResponse = Gson().fromJson(response.errorBody()!!.charStream(), ErrorResponse::class.java)
                return Resource.error(message.message)
            }

            return Resource.failed("Something went wrong, try again later")

        } catch (e: Exception) {
            return Resource.failed("Something went wrong")
        }
    }

}
