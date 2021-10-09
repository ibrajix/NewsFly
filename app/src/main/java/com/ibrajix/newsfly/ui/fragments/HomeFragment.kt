/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import com.ibrajix.newsfly.R
import com.ibrajix.newsfly.databinding.FragmentHomeBinding
import com.ibrajix.newsfly.network.Resource
import com.ibrajix.newsfly.ui.adapters.all.AllNewsLoadStateAdapter
import com.ibrajix.newsfly.ui.adapters.all.PopularNewsAdapter
import com.ibrajix.newsfly.ui.adapters.all.RecentNewsAdapter
import com.ibrajix.newsfly.ui.viewmodel.AllNewsViewModel
import com.ibrajix.newsfly.ui.viewmodel.StorageViewModel
import com.ibrajix.newsfly.utils.Utility
import com.ibrajix.newsfly.utils.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
@ExperimentalCoroutinesApi
@ExperimentalPagingApi
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val storageViewModel: StorageViewModel by viewModels()
    private val newsViewModel: AllNewsViewModel by viewModels()
    private var currentTheme: String = "light"
    lateinit var popularNewsAdapter: PopularNewsAdapter
    lateinit var recentNewsAdapter: RecentNewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView(){


        //start shimmer
        binding.shimmerLayout.startShimmer()

        //set up adapter
        setUpAdapter()

        //observe all observables
        observeDataSet()

        //handle clicks by user
        handleClicks()

        //handle swipe to refresh
        handleSwipeToRefresh()

    }


    private fun handleSwipeToRefresh(){

        //on swipe
        binding.mainContainer.setOnRefreshListener {
            observeDataSet()
            binding.mainContainer.isRefreshing = false
        }

    }



    private fun setUpAdapter(){

        popularNewsAdapter = PopularNewsAdapter(onClickListener = PopularNewsAdapter.OnNewsItemClickListener { article ->
            //open article url in browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
            startActivity(intent)
        })

        recentNewsAdapter = RecentNewsAdapter(onClickListener = RecentNewsAdapter.OnNewsItemClickListener { article ->
            //go to news newsDetailsFragment when recyclerview item clicked
            val action = HomeFragmentDirections.actionHomeFragmentToNewsDetailsFragment(article)
            findNavController().navigate(action)
        })

        //initialize first recyclerview with an horizontal scroll view
        binding.popularNewsRcv.adapter = popularNewsAdapter

        //initialize second recyclerview with a linear layout
        binding.recentNewsRcv.apply {
            adapter = recentNewsAdapter
            adapter = recentNewsAdapter.withLoadStateHeaderAndFooter(
                    header = AllNewsLoadStateAdapter { recentNewsAdapter.retry() },
                    footer = AllNewsLoadStateAdapter { recentNewsAdapter.retry() }
            )
        }

    }


    private fun observeDataSet(){

        //observe selected theme
        storageViewModel.selectedTheme.observe(viewLifecycleOwner){
            currentTheme = it
            when(currentTheme){
                requireContext().getString(R.string.light_mode) -> binding.icThemeMode.setImageResource(
                        R.drawable.ic_dark_mode
                )
                else -> binding.icThemeMode.setImageResource(R.drawable.ic_light_mode)
            }
        }

        //this is the new recommended way of collecting flow in UI
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                newsViewModel.getRecentNews().collectLatest { pagingData->
                    recentNewsAdapter.submitData(pagingData)
                }
            }
        }

        //get popular news and observe
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            newsViewModel.getPopularNews.collect {

                popularNewsAdapter.submitList(it?.data?.sortedBy { it.publishedAt })

                 it ?: return@collect

                when (it.status) {

                    Resource.Status.SUCCESS -> {

                        //hide shimmer
                        binding.shimmerLayout.visibility = View.INVISIBLE

                        //show recyclerview
                        binding.popularNewsRcv.visibility = View.VISIBLE

                        //show recent news title
                        binding.recentNews.visibility = View.VISIBLE

                        //hide error view, if by any means it was shown
                        binding.lytError.visibility = View.INVISIBLE
                    }

                    Resource.Status.LOADING -> {

                    }

                    Resource.Status.ERROR -> {

                    }

                    Resource.Status.FAILURE -> {

                    }

                }
            }
        }

        //using events, show appropriate error in snackBar (if any)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            newsViewModel.events.collect { event ->
                when (event) {
                    is AllNewsViewModel.Event.ShowErrorMessage ->
                        showError(event.error.localizedMessage ?: "")
                                .exhaustive
                }

            }
        }

    }

    private fun showError(error: String){

        //stop shimmer effect and don't show it
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.INVISIBLE

        //show a snackBar
        Utility.displayErrorSnackBar(binding.root, error, requireContext())

    }


    private fun handleClicks(){

        //on click change theme
        binding.icThemeMode.setOnClickListener {

            if (currentTheme == requireContext().getString(R.string.light_mode)){
                //change to dark theme asap
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                //change the preference to light mode
                storageViewModel.changeSelectedTheme(requireContext().getString(R.string.dark_mode))
            }
            else{
                //change to light theme asap
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                //change the preference to light mode
                storageViewModel.changeSelectedTheme(requireContext().getString(R.string.light_mode))
            }
        }

        //on click search edit text - go to searchFragment
        binding.editText.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToSearchFragment()
            findNavController().navigate(action)
        }

        //on click refresh
        binding.root.setOnRefreshListener {
           newsViewModel.onManualRefresh()
        }

        binding.btnRetry.setOnClickListener {
            newsViewModel.onManualRefresh()
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

     override fun onResume() {
         binding.shimmerLayout.startShimmer()
         super.onResume()
    }

     override fun onPause() {
         binding.shimmerLayout.stopShimmer()
         super.onPause()
    }

    override fun onStart() {
        super.onStart()
        newsViewModel.onStart()
    }

}