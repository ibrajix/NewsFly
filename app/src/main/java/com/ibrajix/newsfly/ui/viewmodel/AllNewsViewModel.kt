/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.protobuf.Api
import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bumptech.glide.load.engine.Resource
import com.ibrajix.newsfly.data.AllNewsRepository
import com.ibrajix.newsfly.database.entity.PopularArticle
import com.ibrajix.newsfly.database.entity.RecentArticle
import com.ibrajix.newsfly.model.responses.AllNewsResponse
import com.ibrajix.newsfly.network.ApiService
import com.ibrajix.newsfly.network.ApiStatus
import com.ibrajix.newsfly.utils.Utility.isConnectedToInternet
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Response.error

@HiltViewModel
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class AllNewsViewModel @Inject constructor(private val allNewsRepository: AllNewsRepository) : ViewModel() {


     /**
     * Popular and Recent ews
     */

    private val _snackErrorMessage = MutableSharedFlow<String>()
    val snackErrorMessage = _snackErrorMessage.asSharedFlow()

    private val refreshTriggerChannel = Channel<RefreshLoad>()
    private val refreshTrigger = refreshTriggerChannel.receiveAsFlow()

    val getPopularAndRecentNews = refreshTrigger.flatMapLatest { refresh->
        allNewsRepository.getPopularNews(
            forceRefresh = refresh == RefreshLoad.FORCE,
            onFetchSuccess = {
                //do something
            },
            onFetchFailed = {
                viewModelScope.launch {
                    _snackErrorMessage.emit(it.toString())
                }
            }
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)


    enum class RefreshLoad {
        FORCE, NORMAL
    }

    fun getLatestPopularNews(){
        if (getPopularAndRecentNews.value !is ApiStatus.Loading){
            viewModelScope.launch {
                refreshTriggerChannel.send(RefreshLoad.NORMAL)
            }
        }
    }

    fun onRefreshSwiped(){
        if (getPopularAndRecentNews.value !is ApiStatus.Loading){
            viewModelScope.launch {
                refreshTriggerChannel.send(RefreshLoad.FORCE)
            }
        }
    }


    fun getRecentNews() : Flow<PagingData<RecentArticle>> {
       return allNewsRepository.getRecentNews().cachedIn(viewModelScope)
    }


    //get searched news using live data (you can also use stateFlow)
    private val _searchAllNews = MutableLiveData<ApiStatus<AllNewsResponse>>()
    val searchAllNews: LiveData<ApiStatus<AllNewsResponse>> = _searchAllNews

    var searchNewsPage = 1
    var searchNewsResponse: AllNewsResponse? = null
    var newSearchQuery:String? = null
    var oldSearchQuery:String? = null

    fun doSearchForNews(q: String) {
        viewModelScope.launch {
            newSearchQuery = q
            _searchAllNews.postValue(ApiStatus.Loading(data = null))
            try {
                val response = allNewsRepository.searchForNewsItem(q, searchNewsPage)
                if (searchNewsResponse == null || newSearchQuery != oldSearchQuery){
                    searchNewsPage = 1
                    oldSearchQuery = newSearchQuery
                    searchNewsResponse = response.data
                }else{
                    searchNewsPage++
                    val oldArticles = searchNewsResponse?.recentArticles
                    val newArticles = response.data?.recentArticles
                    if (newArticles != null) {
                        oldArticles?.addAll(newArticles)
                    }
                }
                _searchAllNews.postValue(ApiStatus.Success(searchNewsResponse?: response.data))
            }catch (e: Exception){
                //log error
                ApiStatus.Error(data = null, throwable = e)
            }

        }
    }


}