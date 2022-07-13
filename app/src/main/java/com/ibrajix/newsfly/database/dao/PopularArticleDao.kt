package com.ibrajix.newsfly.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ibrajix.newsfly.database.entity.PopularArticle
import kotlinx.coroutines.flow.Flow

/**
 * Contains data access object (DAO) used for querying popular articles from database
 */

@Dao
interface PopularArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePopularArticles(popularArticle: List<PopularArticle>)

    @Query("SELECT * FROM popular_articles")
    fun getAllPopularArticles(): Flow<List<PopularArticle>>

    @Query("DELETE FROM popular_articles")
    suspend fun deletePopularArticles()

}