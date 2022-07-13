/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.database.main

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ibrajix.newsfly.database.dao.RecentArticlesRemoteKeyDao
import com.ibrajix.newsfly.database.dao.PopularArticleDao
import com.ibrajix.newsfly.database.dao.RecentArticleDao
import com.ibrajix.newsfly.database.entity.RecentArticlesRemoteKey
import com.ibrajix.newsfly.database.entity.PopularArticle
import com.ibrajix.newsfly.database.entity.RecentArticle
import com.ibrajix.newsfly.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@Database(entities = [RecentArticle::class, PopularArticle::class, RecentArticlesRemoteKey::class],  version = 16)
@TypeConverters(RoomConverter::class)
abstract class ArticlesDatabase : RoomDatabase() {

    abstract fun recentArticleDao(): RecentArticleDao
    abstract fun popularArticleDao(): PopularArticleDao
    abstract fun newsRemoteKeyDao(): RecentArticlesRemoteKeyDao

    class Callback @Inject constructor(@ApplicationScope private val applicationScope: CoroutineScope) : RoomDatabase.Callback(){
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
        }
    }

}