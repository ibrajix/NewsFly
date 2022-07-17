/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.model.responses

import com.google.gson.annotations.SerializedName
import com.ibrajix.newsfly.database.entity.RecentArticle

data class AllNewsResponse(
        @SerializedName("articles")
        val recentArticles: MutableList<RecentArticle>,
        val status: String,
        val totalResults: Int
)