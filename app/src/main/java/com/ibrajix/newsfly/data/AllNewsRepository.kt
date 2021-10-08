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
import com.ibrajix.newsfly.network.BaseDataSource
import com.ibrajix.newsfly.network.Resource
import com.ibrajix.newsfly.utils.networkBoundResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class AllNewsRepository @Inject constructor(private val apiDataSource: ApiDataSource, private val articlesDatabase: ArticlesDatabase): BaseDataSource()  {

    private val newsArticleDao = articlesDatabase.articleDao()

    /**
     * Get recent paginated news with offline support
     */

    fun getRecentNews() : Flow<PagingData<RecentArticle>> =
        Pager(
                PagingConfig(pageSize = 10, maxSize = 20, prefetchDistance = 2, enablePlaceholders = false),
                remoteMediator = AllNewsRemoteMediator(apiDataSource = apiDataSource, newsArticleDb = articlesDatabase),
                pagingSourceFactory = {
                    newsArticleDao.getAllRecentArticles()
                }
        ).flow


    /**
     * Get popular news, without pagination but with offline support
     */

    fun getPopularNews(
            forceRefresh: Boolean,
            onFetchSuccess: () -> Unit,
            onFetchFailed: (Throwable) -> Unit
    ): Flow<Resource<List<PopularArticle>>> =
            networkBoundResource(
                    query = {
                        newsArticleDao.getAllPopularArticles()
                    },
                    fetch = {
                        val response = apiDataSource.getPopularNews()
                        response.popularArticles
                    },
                    saveFetchResult = {
                        serverBreakingNewsArticles ->
                        val popularNewsArticles = serverBreakingNewsArticles.map { it }
                        articlesDatabase.withTransaction {
                            newsArticleDao.deletePopularArticles()
                            newsArticleDao.savePopularArticles(popularNewsArticles)
                        }
                    },
                    shouldFetch = { cachedArticles ->
                        if (forceRefresh) {
                            true
                        } else {
                            val sortedArticles = cachedArticles.sortedBy { article ->
                                article.publishedAt
                            }
                            val oldestTimestamp = sortedArticles.firstOrNull()?.id
                            val needsRefresh = oldestTimestamp == null ||
                                    oldestTimestamp < System.currentTimeMillis() -
                                    TimeUnit.MINUTES.toMillis(60)
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
     * Search for news without pagination and offline support
     */

    suspend fun searchForNewsItem(q: String) : Flow<Resource<AllNewsResponse>> {
        return flow {
            val result = safeApiCall { apiDataSource.searchForNews(q) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }


}