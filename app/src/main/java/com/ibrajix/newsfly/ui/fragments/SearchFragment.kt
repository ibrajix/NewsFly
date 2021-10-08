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
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import com.ibrajix.newsfly.databinding.FragmentSearchBinding
import com.ibrajix.newsfly.network.Resource
import com.ibrajix.newsfly.ui.adapters.all.SearchNewsAdapter
import com.ibrajix.newsfly.ui.viewmodel.AllNewsViewModel
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
        binding.rcvSearch.adapter = searchNewsAdapter
    }

   private fun observeData(){

        allNewsViewModel.searchAllNews.observe(viewLifecycleOwner){

            when (it.status) {

                Resource.Status.SUCCESS -> {

                    if (it.data?.status == "ok") {
                        searchNewsAdapter.submitList(it.data.recentArticles)
                    } else {

                        Toast.makeText(
                            requireContext(),
                            "Ooops, something went wrong",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }

                Resource.Status.ERROR -> {

                }

                Resource.Status.LOADING -> {

                }

                Resource.Status.FAILURE -> {

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

}