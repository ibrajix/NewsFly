/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.ibrajix.newsfly.database.entity.PopularArticle
import com.ibrajix.newsfly.database.entity.RecentArticle
import com.ibrajix.newsfly.database.main.ArticlesDatabase
import com.ibrajix.newsfly.model.responses.AllNewsResponse
import com.ibrajix.newsfly.network.ApiStatus
import com.ibrajix.newsfly.network.BaseDataSource
import com.ibrajix.newsfly.utils.networkBoundResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@ExperimentalPagingApi
class AllNewsRepository @Inject constructor(private val apiDataSource: ApiDataSource, private val articlesDatabase: ArticlesDatabase): BaseDataSource()  {

    private val newsArticleDao = articlesDatabase.recentArticleDao()
    private val popularNewsArticleDao = articlesDatabase.popularArticleDao()

    /**
     * Get popular news, without pagination but with offline support
     */

    fun getPopularNews(
        forceRefresh: Boolean,
        onFetchSuccess: () -> Unit,
        onFetchFailed: (Throwable) -> Unit
    ): Flow<ApiStatus<List<PopularArticle>>> =
        networkBoundResource(
            query = {
               popularNewsArticleDao.getAllPopularArticles()
            },
            fetch = {
                val response =  apiDataSource.getPopularNews()
                response.popularArticles
            },
            saveFetchResult = { popularNews->
                val popularNewsArticles = popularNews.map { it }
                articlesDatabase.withTransaction {
                    popularNewsArticleDao.deletePopularArticles()
                    popularNewsArticleDao.savePopularArticles(popularNewsArticles)
                }
            },
            shouldFetch = { cachedPopularArticles->
                if (forceRefresh){
                    true
                }
                else{
                    val sortedArticles = cachedPopularArticles.sortedBy { article->
                        article.updatedAt
                    }
                    val oldestTimeStamp = sortedArticles.firstOrNull()?.updatedAt
                    val needsRefresh = oldestTimeStamp == null ||
                            oldestTimeStamp < System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(60)
                    needsRefresh
                }
            },
            onFetchSuccess = onFetchSuccess,
            onFetchFailed = { t ->
                if (t !is HttpException && t !is IOException) {
                    throw t
                }
                onFetchFailed(t)
            }
        )


    /**
     * Get recent paginated news with offline support
     */

    fun getRecentNews() : Flow<PagingData<RecentArticle>> =
        Pager(
                PagingConfig(
                    pageSize = 20,
                    enablePlaceholders = false),
                remoteMediator = RecentArticlesRemoteMediator(apiDataSource = apiDataSource, newsArticleDb = articlesDatabase),
                pagingSourceFactory = {
                    newsArticleDao.getAllRecentArticles()
                }
        ).flow



    /**
     * Search for news without pagination and offline support
     */

    suspend fun searchForNewsItem(q: String) : Flow<ApiStatus<AllNewsResponse>> {
        return flow {
            val result = safeApiCall { apiDataSource.searchForNews(q) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }


}