/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.ui.adapters.all

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ibrajix.newsfly.database.entity.PopularArticle
import com.ibrajix.newsfly.databinding.RcvPopularNewsBinding
import com.ibrajix.newsfly.ui.adapters.callbacks.PopularNewsDiffCallback


class PopularNewsAdapter(private val onClickListener: OnNewsItemClickListener) : ListAdapter<PopularArticle, PopularNewsAdapter.PopularNewsViewHolder>(
    PopularNewsDiffCallback()
) {

   class PopularNewsViewHolder private constructor(private val binding: RcvPopularNewsBinding) : RecyclerView.ViewHolder(binding.root){

       fun bind(item: PopularArticle){
           binding.model = item
           binding.executePendingBindings()
       }

       companion object{
           fun from(parent: ViewGroup) : PopularNewsViewHolder {
               val layoutInflater = LayoutInflater.from(parent.context)
               val binding = RcvPopularNewsBinding.inflate(layoutInflater, parent, false)
               return PopularNewsViewHolder(binding)
           }
       }


   }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularNewsViewHolder {
        return PopularNewsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: PopularNewsViewHolder, position: Int) {
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

    class OnNewsItemClickListener(val clickListener: (popularArticle: PopularArticle) -> Unit){
        fun onClickNews(popularArticle: PopularArticle) = clickListener(popularArticle)
    }


}