/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import com.bumptech.glide.load.engine.Resource
import com.ibrajix.newsfly.data.AllNewsRepository
import com.ibrajix.newsfly.database.entity.PopularArticle
import com.ibrajix.newsfly.network.ApiStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class AllNewsViewModel @Inject constructor(private val allNewsRepository: AllNewsRepository) : ViewModel() {

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


}