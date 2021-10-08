/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.ui.adapters.all

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.ibrajix.newsfly.database.entity.PopularArticle
import com.ibrajix.newsfly.database.entity.RecentArticle


@BindingAdapter("displayImage")
fun ImageView.setDisplayImage(item: RecentArticle){
    Glide.with(this)
        .load(item.urlToImage)
        .into(this)
}

@BindingAdapter("displayImage2")
fun ImageView.setDisplayImage2(item: PopularArticle){
    Glide.with(this)
            .load(item.urlToImage)
            .into(this)
}
