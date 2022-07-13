/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.ibrajix.newsfly.R
import com.ibrajix.newsfly.ui.viewmodel.StorageViewModel
import com.ibrajix.newsfly.utils.isDarkThemeOn
import com.ibrajix.newsfly.utils.transparentStatusBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: StorageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        setTheme(R.style.Theme_CryptoNews)

        super.onCreate(savedInstanceState)

        if(!isDarkThemeOn()){
            transparentStatusBar()
        }

        checkSelectedTheme()
        setContentView(R.layout.activity_main)


    }

    private fun checkSelectedTheme(){
        viewModel.selectedTheme.observe(this){
            when(it){
                applicationContext.getString(R.string.light_mode) -> AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO)
                applicationContext.getString(R.string.dark_mode) -> AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES)
                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

}