/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.database.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.ibrajix.newsfly.database.entity.AllNewsRemoteKey


/**
 * Contains data access object (DAO) used for pagination
 */

@Dao
interface AllNewsRemoteKeyDao{

    @Insert(onConflict = REPLACE)
    suspend fun saveRemoteKeys(remoteKey: AllNewsRemoteKey)

    @Query("SELECT * FROM all_news_remote_keys ORDER BY id DESC")
    suspend fun getRemoteKeys(): AllNewsRemoteKey

    @Query("DELETE FROM all_news_remote_keys")
    suspend fun clearRemoteKeys()


}