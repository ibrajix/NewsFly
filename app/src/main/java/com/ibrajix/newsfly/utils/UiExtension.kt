/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.View

/**
 * Contains UI-Related extension functions
 */

//this function changes the status bar color to white on light theme
fun Activity.whiteStatsBar() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        window.statusBarColor = Color.WHITE
    } else {
        window.setDecorFitsSystemWindows(false)
        window.statusBarColor = Color.WHITE
    }
}

//this checks if dark theme is enabled
fun Context.isDarkThemeOn(): Boolean {
    return resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

//this is used with the 'when' block to specify all cases
val <T> T.exhaustive: T
    get() = this
