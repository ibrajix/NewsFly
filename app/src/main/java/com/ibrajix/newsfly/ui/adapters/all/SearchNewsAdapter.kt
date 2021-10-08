/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.ui.adapters.all

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ibrajix.newsfly.database.entity.RecentArticle
import com.ibrajix.newsfly.databinding.RcvRecentNewsBinding
import com.ibrajix.newsfly.ui.adapters.callbacks.RecentNewsDiffCallback

class SearchNewsAdapter(private val onClickListener: OnNewsItemClickListener) : ListAdapter<RecentArticle, SearchNewsAdapter.SearchNewsViewHolder>(
    RecentNewsDiffCallback()
) {


    class SearchNewsViewHolder private constructor(private val binding: RcvRecentNewsBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: RecentArticle){
            binding.model = item
            binding.executePendingBindings()
        }

        companion object{
            fun from(parent: ViewGroup) : SearchNewsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RcvRecentNewsBinding.inflate(layoutInflater, parent, false)
                return SearchNewsViewHolder(binding)
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchNewsViewHolder {
        return SearchNewsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: SearchNewsViewHolder, position: Int) {
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

    class OnNewsItemClickListener(val clickListener: (searchArticle: RecentArticle) -> Unit){
        fun onClickNews(searchArticle: RecentArticle) = clickListener(searchArticle)
    }


}