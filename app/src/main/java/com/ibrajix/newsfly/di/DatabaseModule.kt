/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/
package com.ibrajix.newsfly.di

import android.app.Application
import androidx.room.Room
import com.ibrajix.newsfly.database.main.ArticlesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(application: Application, callback: ArticlesDatabase.Callback)
            = Room.databaseBuilder(application, ArticlesDatabase::class.java, "article_database")
        .fallbackToDestructiveMigration()
        .addCallback(callback)
        .build()

    @Provides
    fun providePopularArticleDao(articlesDatabase: ArticlesDatabase) =
        articlesDatabase.popularArticleDao()

    @Provides
    fun provideRecentArticleDao(articlesDatabase: ArticlesDatabase) =
        articlesDatabase.recentArticleDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun providesApplicationScope() = CoroutineScope(SupervisorJob())

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope