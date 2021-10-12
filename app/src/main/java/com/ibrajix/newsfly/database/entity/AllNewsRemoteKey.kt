/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Model for pagination using remote mediator
 */

@Entity(tableName = "all_news_remote_keys")
data class AllNewsRemoteKey(
        @PrimaryKey(autoGenerate = true)
        val id: Int,
        val nextPageKey: Int,
        val prevPageKey: Int,
)