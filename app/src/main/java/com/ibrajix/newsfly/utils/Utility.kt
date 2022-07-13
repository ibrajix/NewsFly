/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.ibrajix.newsfly.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Utility {

    /**
     * This function formats the date returned from news api to a more readable format e.g (2021/12/21)
     */

    fun formatDate(date: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK)
        val outputFormat = SimpleDateFormat("yyyy/MM/dd ", Locale.UK)
        try {
            return outputFormat.format(inputFormat.parse(date) ?: "")
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * This function helps to display error in a snackBar
     */

    fun displayErrorSnackBar(view: View, s: String, context: Context) {
        Snackbar.make(view, s, Snackbar.LENGTH_LONG)
            .withColor(ContextCompat.getColor(context, R.color.red))
            .setTextColor(ContextCompat.getColor(context, R.color.white))
            .show()
    }

    /**
     * This function helps to set the color of the snackBar as used in 'displayErrorSnackBar' function above
     */

    private fun Snackbar.withColor(@ColorInt colorInt: Int): Snackbar {
        this.view.setBackgroundColor(colorInt)
        return this
    }

    /**
    * This function helps to make the status bar transparent
    */
    fun isConnectedToInternet(context: Context): Boolean{
        var isConnected = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    if (hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
                        isConnected = true
                    } else if (hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)){
                        isConnected = true
                    } else if (hasTransport(NetworkCapabilities.TRANSPORT_VPN)){
                        isConnected = true
                    }
                }
            }
        }else{
            cm?.run {
                cm.activeNetworkInfo?.run {
                    when(type){
                        ConnectivityManager.TYPE_WIFI -> {
                            isConnected = true
                        }
                        ConnectivityManager.TYPE_MOBILE -> {
                            isConnected = true
                        }
                        ConnectivityManager.TYPE_VPN -> {
                            isConnected = true
                        }
                    }
                }
            }
        }
     return isConnected
    }

}