/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.ibrajix.newsfly.databinding.FragmentNewsDetailsBinding
import com.ibrajix.newsfly.utils.Utility

class NewsDetailsFragment : Fragment() {

    private var _binding: FragmentNewsDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: NewsDetailsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewsDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        handleClicks()
    }

    private fun setUpViews(){

        //load image using glide
        Glide.with(binding.imgNews)
            .load(args.recentArticle.urlToImage)
            .into(binding.imgNews)

        //display date
        binding.txtDate.text = args.recentArticle.publishedAt?.let { Utility.formatDate(it) }

        //display title
        binding.txtTitle.text = args.recentArticle.title

        //display source
        binding.txtSource.text = args.recentArticle.source?.name

        //display article
        binding.txtArticle.text = args.recentArticle.content

    }


    private fun handleClicks(){

        //on click back
        binding.icBack.setOnClickListener {
            findNavController().popBackStack()
        }

        //on click view full article, open webView
        binding.btnViewFullArticle.setOnClickListener {
            val action = args.recentArticle.url?.let { it1 -> NewsDetailsFragmentDirections.actionNewsDetailsFragmentToViewFullArticleFragment(url = it1) }
            if (action != null) {
                findNavController().navigate(action)
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}