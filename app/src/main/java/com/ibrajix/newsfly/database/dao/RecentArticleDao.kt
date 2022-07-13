/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.database.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.ibrajix.newsfly.database.entity.PopularArticle
import com.ibrajix.newsfly.database.entity.RecentArticle
import kotlinx.coroutines.flow.Flow

/**
 * Contains data access object (DAO) used for querying recent articles from database
 */

@Dao
interface RecentArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecentArticles(recentArticles: List<RecentArticle>)

    @Query("SELECT * FROM recent_articles")
    fun getAllRecentArticles(): PagingSource<Int, RecentArticle>

    @Query("DELETE FROM recent_articles")
    suspend fun deleteRecentArticles()

}