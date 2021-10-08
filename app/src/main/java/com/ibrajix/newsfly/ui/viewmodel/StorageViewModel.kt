/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ibrajix.newsfly.storage.DataStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StorageViewModel @Inject constructor(private val dataStorage: DataStorage) : ViewModel()  {

    /**
     * This sets the theme to either light or dark mode
     */
    val selectedTheme = dataStorage.selectedTheme().asLiveData()
    fun changeSelectedTheme(theme: String){
        viewModelScope.launch {
            dataStorage.setSelectedTheme(theme)
        }
    }

}