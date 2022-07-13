/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.ibrajix.newsfly.database.entity.RecentArticle
import com.ibrajix.newsfly.database.entity.RecentArticlesRemoteKey
import com.ibrajix.newsfly.database.main.ArticlesDatabase
import com.ibrajix.newsfly.utils.Constant.Companion.NEWS_API_STARTING_PAGE_INDEX
import retrofit2.HttpException
import java.io.IOException


@ExperimentalPagingApi
class RecentArticlesRemoteMediator(
        private val apiDataSource: ApiDataSource,
        private val newsArticleDb: ArticlesDatabase,
) : RemoteMediator<Int, RecentArticle>() {

    private val recentArticleDao = newsArticleDb.recentArticleDao()
    private val remoteKeysDao = newsArticleDb.newsRemoteKeyDao()

    override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, RecentArticle>
    ): MediatorResult {

        val page = when(loadType){

            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextPageKey?.minus(1) ?: NEWS_API_STARTING_PAGE_INDEX
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevPageKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }

            LoadType.APPEND -> {

                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextPageKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }


        }

       try {

           val apiResponse = apiDataSource.getRecentNews(page = page, pageSize = state.config.pageSize)

           val recentArticles = apiResponse.recentArticles

           val endOfPageReached = recentArticles.isEmpty()


           newsArticleDb.withTransaction {

               if (loadType == LoadType.REFRESH){
                   remoteKeysDao.clearRemoteKeys()
                   recentArticleDao.deleteRecentArticles()
               }

               val prevKey = if (page == NEWS_API_STARTING_PAGE_INDEX) null else {
                   page - 1
               }

               val nextKey = if (endOfPageReached) null else page + 1

               val keys = recentArticles.map {
                   RecentArticlesRemoteKey(
                       url = it.url,
                       nextPageKey = nextKey,
                       prevPageKey = prevKey,
                   )
               }

               remoteKeysDao.saveRemoteKeys(keys)
               recentArticleDao.saveRecentArticles(recentArticles)

           }

           return MediatorResult.Success(endOfPaginationReached = endOfPageReached)

       }catch (e: IOException){
           return MediatorResult.Error(e)
       } catch (e: HttpException){
        return MediatorResult.Error(e)
       }
    }


    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, RecentArticle>) : RecentArticlesRemoteKey? {
        return state.pages.lastOrNull {
            it.data.isEmpty()
        }?.data?.lastOrNull()?.let { recentArticle ->
            remoteKeysDao.getRemoteKeys(recentArticle.url)
        }
    }


    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, RecentArticle>) : RecentArticlesRemoteKey? {
        return state.pages.firstOrNull {
            it.data.isNotEmpty()
        }?.data?.firstOrNull()?.let {recentArticle ->
            remoteKeysDao.getRemoteKeys(recentArticle.url)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, RecentArticle>) : RecentArticlesRemoteKey? {

        return state.anchorPosition?.let { position->
            state.closestItemToPosition(position)?.url?.let {
                remoteKeysDao.getRemoteKeys(it)
            }
        }

    }

}