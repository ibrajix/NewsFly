/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ibrajix.newsfly.R
import com.ibrajix.newsfly.databinding.FragmentSearchBinding
import com.ibrajix.newsfly.network.ApiStatus
import com.ibrajix.newsfly.ui.adapters.all.SearchNewsAdapter
import com.ibrajix.newsfly.ui.viewmodel.AllNewsViewModel
import com.ibrajix.newsfly.utils.Constant.Companion.QUERY_PAGE_SIZE
import com.ibrajix.newsfly.utils.Utility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchNewsAdapter: SearchNewsAdapter
    private val allNewsViewModel: AllNewsViewModel by viewModels()
    var job: Job? = null

    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        observeData()
        setUpSearch()
        handleClicks()
    }

    private fun setUpSearch(){

        binding.etSearchNews.addTextChangedListener{ editable->
            job?.cancel()
            job = MainScope().launch {
                delay(500L)
            }

            editable?.let {
                if (editable.toString().isNotEmpty()){
                    allNewsViewModel.doSearchForNews(editable.toString())
                }
            }
        }

    }

    private fun setUpRecyclerView(){
        searchNewsAdapter = SearchNewsAdapter(SearchNewsAdapter.OnNewsItemClickListener{ article->
            val action = SearchFragmentDirections.actionSearchFragmentToNewsDetailsFragment(article)
            findNavController().navigate(action)
        })
        binding.rcvSearch.apply {
            adapter = searchNewsAdapter
            addOnScrollListener(this@SearchFragment.scrollListener)
        }
    }

   private fun observeData(){

       allNewsViewModel.searchAllNews.observe(viewLifecycleOwner){ result->

           when(result){
              is ApiStatus.Success -> {
                  hideProgressBar()
                  if (result.data?.status == "ok") {
                      val totalPages = result.data.totalResults
                      isLastPage = allNewsViewModel.searchNewsPage == totalPages
                      if (isLastPage){
                          binding.rcvSearch.setPadding(0,0,0,0)
                      }
                      searchNewsAdapter.submitList(result.data.recentArticles.toList())
                  } else {
                      Utility.displayErrorSnackBar(binding.root, requireContext().getString(R.string.unknown_error_occurred), requireContext())
                  }
                  binding.loading.visibility = View.GONE
              }
               is ApiStatus.Error -> {
                   //log error
                   Utility.displayErrorSnackBar(binding.root, requireContext().getString(R.string.unknown_error_occurred), requireContext())
               }
               is ApiStatus.Loading -> {
                   //show progress bar
                   showProgressBar()
               }
           }

        }

    }

    private fun handleClicks(){

        //on click back, go back
        binding.icBack.setOnClickListener {
          findNavController().popBackStack()
        }

    }

    //show keyboard to type when this view is just active
    override fun onResume() {
        super.onResume()
        binding.etSearchNews.post {
            binding.etSearchNews.requestFocus()
            val input: InputMethodManager =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            input.showSoftInput(binding.etSearchNews, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }


    val scrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoErrors = !isError

            val isLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate = isNoErrors && isLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling

            if(shouldPaginate) {
                allNewsViewModel.doSearchForNews(binding.etSearchNews.text.toString())
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

    }

}