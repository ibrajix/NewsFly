/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.model.responses

import com.google.gson.annotations.SerializedName
import com.ibrajix.newsfly.database.entity.PopularArticle

data class PopularNewsResponse(
        @SerializedName("articles")
        val popularArticles: List<PopularArticle>,
        val status: String,
        val totalResults: Int
)