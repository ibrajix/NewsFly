/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.ibrajix.newsfly.database.entity.AllNewsRemoteKey
import com.ibrajix.newsfly.database.entity.RecentArticle
import com.ibrajix.newsfly.database.main.ArticlesDatabase
import com.ibrajix.newsfly.utils.Constant.Companion.NEWS_STARTING_PAGE_INDEX
import retrofit2.HttpException
import java.io.IOException

@ExperimentalPagingApi
class AllNewsRemoteMediator(
    private val apiDataSource: ApiDataSource,
    private val newsArticleDb: ArticlesDatabase,
) : RemoteMediator<Int, RecentArticle>() {

    private val newsArticleDao = newsArticleDb.articleDao()
    private val remoteKeysDao = newsArticleDb.newsRemoteKeyDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, RecentArticle>
    ): MediatorResult {
        return try {

            val loadKey = when(loadType){
                LoadType.REFRESH -> NEWS_STARTING_PAGE_INDEX
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> remoteKeysDao.getRemoteKeys().nextPageKey
            }
            val response = apiDataSource.getRecentNews(
                    pageSize = state.config.pageSize,
                    page = loadKey
            )

            val listing = response.recentArticles
            val articles = listing.map { it }

            val nextPageKey = loadKey + 1
            val prevPageKey = loadKey - 1

           newsArticleDb.withTransaction {
               remoteKeysDao.saveRemoteKeys(AllNewsRemoteKey(0, nextPageKey = nextPageKey, prevPageKey = prevPageKey))
               newsArticleDao.saveRecentArticles(articles)
           }

            MediatorResult.Success(endOfPaginationReached = listing.isEmpty())

        } catch (exception: IOException) {
            MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            MediatorResult.Error(exception)
        }

    }

}