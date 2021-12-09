/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.ui.adapters.callbacks

import androidx.recyclerview.widget.DiffUtil
import com.ibrajix.newsfly.database.entity.RecentArticle

class RecentNewsDiffCallback : DiffUtil.ItemCallback<RecentArticle>() {

    override fun areItemsTheSame(oldItem: RecentArticle, newItem: RecentArticle): Boolean {
        return oldItem.url == newItem.url
    }

    override fun areContentsTheSame(oldItem: RecentArticle, newItem: RecentArticle): Boolean {
        return oldItem == newItem
    }

}