/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.ui.viewmodel

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ibrajix.newsfly.data.AllNewsRepository
import com.ibrajix.newsfly.database.entity.RecentArticle
import com.ibrajix.newsfly.model.responses.AllNewsResponse
import com.ibrajix.newsfly.network.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class AllNewsViewModel @Inject constructor(private val allNewsRepository: AllNewsRepository) : ViewModel() {

    //get searched news using live data (you can also use stateFlow)
    private val _searchAllNews = MutableLiveData<Resource<AllNewsResponse>>()
    val searchAllNews: LiveData<Resource<AllNewsResponse>> = _searchAllNews

    private val refreshTriggerChannel = Channel<Refresh>()
    private val refreshTrigger = refreshTriggerChannel.receiveAsFlow()

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private var pendingScrollToTopAfterRefresh = false


    //get recent news
    fun getRecentNews(): Flow<PagingData<RecentArticle>> {
        return allNewsRepository.getRecentNews().cachedIn(viewModelScope)
    }


    //get breaking news
    val getPopularNews = refreshTrigger.flatMapLatest { refresh ->
        allNewsRepository.getPopularNews(
                refresh == Refresh.FORCE,
                onFetchSuccess = {
                    pendingScrollToTopAfterRefresh = true
                },
                onFetchFailed = { t ->
                    viewModelScope.launch { eventChannel.send(Event.ShowErrorMessage(t)) }
                }
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)


    fun onStart() {
        if (getPopularNews.value?.status != Resource.Status.LOADING) {
            viewModelScope.launch {
                refreshTriggerChannel.send(Refresh.NORMAL)
            }
        }
    }

    fun onManualRefresh() {
        if (getPopularNews.value?.status != Resource.Status.LOADING) {
            viewModelScope.launch {
                refreshTriggerChannel.send(Refresh.FORCE)
            }
        }
    }

    fun doSearchForNews(q: String){
        viewModelScope.launch {
            allNewsRepository.searchForNewsItem(q)
                .catch { e->
                    _searchAllNews.value = Resource.error(e.toString())
                }
                .collect {
                    _searchAllNews.value = it
                }
        }
    }

    enum class Refresh {
        FORCE, NORMAL
    }

    sealed class Event {
        data class ShowErrorMessage(val error: Throwable) : Event()
    }

}