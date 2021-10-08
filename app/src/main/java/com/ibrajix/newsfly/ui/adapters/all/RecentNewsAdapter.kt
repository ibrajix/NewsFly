/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.ui.adapters.all

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ibrajix.newsfly.database.entity.RecentArticle
import com.ibrajix.newsfly.databinding.RcvRecentNewsBinding
import com.ibrajix.newsfly.ui.adapters.callbacks.RecentNewsDiffCallback

class RecentNewsAdapter(private val onClickListener: OnNewsItemClickListener) : PagingDataAdapter<RecentArticle, RecentNewsAdapter.RecentNewsViewHolder>(RecentNewsDiffCallback()) {

    class RecentNewsViewHolder private constructor(private val binding: RcvRecentNewsBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: RecentArticle){
            binding.model = item
            binding.executePendingBindings()
        }

        companion object{
            fun from(parent: ViewGroup) : RecentNewsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RcvRecentNewsBinding.inflate(layoutInflater, parent, false)
                return RecentNewsViewHolder(binding)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentNewsViewHolder {
        return RecentNewsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecentNewsViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
        holder.itemView.setOnClickListener {
            if (item != null) {
                onClickListener.onClickNews(item)
            }
        }
    }

    class OnNewsItemClickListener(val clickListener: (recentArticle: RecentArticle) -> Unit){
        fun onClickNews(recentArticle: RecentArticle) = clickListener(recentArticle)
    }


}