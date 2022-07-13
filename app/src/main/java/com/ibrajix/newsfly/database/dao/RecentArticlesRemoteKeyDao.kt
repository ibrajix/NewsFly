/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.database.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.ibrajix.newsfly.database.entity.RecentArticlesRemoteKey


/**
 * Contains data access object (DAO) used for pagination
 */

@Dao
interface RecentArticlesRemoteKeyDao{

    @Insert(onConflict = REPLACE)
    suspend fun saveRemoteKeys(remoteKey: List<RecentArticlesRemoteKey>)

    @Query("SELECT * FROM all_news_remote_keys WHERE url = :url")
    suspend fun getRemoteKeys(url: String): RecentArticlesRemoteKey?

    @Query("DELETE FROM all_news_remote_keys")
    suspend fun clearRemoteKeys()


}