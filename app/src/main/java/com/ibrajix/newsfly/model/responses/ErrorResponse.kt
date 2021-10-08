/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.model.responses

data class ErrorResponse(
    val code: String,
    val message: String,
    val status: String
)