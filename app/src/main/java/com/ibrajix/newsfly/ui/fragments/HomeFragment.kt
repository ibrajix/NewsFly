/*
 * Written and Developed by Inuwa Ibrahim - https://linktr.ee/Ibrajix
*/

package com.ibrajix.newsfly.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import com.google.android.material.snackbar.Snackbar
import com.ibrajix.newsfly.R
import com.ibrajix.newsfly.databinding.FragmentHomeBinding
import com.ibrajix.newsfly.network.ApiStatus
import com.ibrajix.newsfly.ui.adapters.all.PopularNewsAdapter
import com.ibrajix.newsfly.ui.adapters.all.RecentNewsAdapter
import com.ibrajix.newsfly.ui.viewmodel.AllNewsViewModel
import com.ibrajix.newsfly.ui.viewmodel.StorageViewModel
import com.ibrajix.newsfly.utils.Utility
import com.ibrajix.newsfly.utils.Utility.isConnectedToInternet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private var isUsersFirstTimeVisit: Boolean = false
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

        checkIfUsersFirstTimeWithoutInternet()

        setUpAdapter()

        handleClicks()

        handleSwipeToRefresh()

        checkUiMode()

    }



    private fun checkUiMode(){
        storageViewModel.selectedTheme.observe(viewLifecycleOwner){
            currentTheme = it
            when(currentTheme){
                requireContext().getString(R.string.light_mode) -> {
                    binding.icThemeMode.setBackgroundResource(R.drawable.ic_dark_mode)
                }
                else -> binding.icThemeMode.setImageResource(R.drawable.ic_light_mode)
            }
        }
    }

    private fun checkIfUsersFirstTimeWithoutInternet(){

        storageViewModel.isUsersFirstTime.observe(viewLifecycleOwner){usersFirstTime->

            isUsersFirstTimeVisit = usersFirstTime

            if (usersFirstTime == true && !isConnectedToInternet(requireContext())){
                //open alert dialog
                binding.lytNotConnected.visibility = View.VISIBLE
                binding.lytMain.visibility = View.GONE
            }
            else{
                getData()
                binding.lytNotConnected.visibility = View.GONE
                binding.lytMain.visibility = View.VISIBLE
            }
        }

    }


    private fun getData(){

        lifecycleScope.launch {

            repeatOnLifecycle(Lifecycle.State.STARTED){
                newsViewModel.getPopularAndRecentNews.collect{ result->

                    val news = result ?: return@collect

                    popularNewsAdapter.submitList(news.data)

                    when(result){

                        is ApiStatus.Success -> {
                            storageViewModel.changeUsersFirstTime(false)
                            binding.shimmerLayout.visibility = View.GONE
                        }
                        is ApiStatus.Error -> {
                            binding.shimmerLayout.visibility = View.GONE
                        }
                        is ApiStatus.Loading -> {
                            if (isUsersFirstTimeVisit && isConnectedToInternet(requireContext())){
                                binding.shimmerLayout.visibility = View.VISIBLE
                            }
                            binding.root.isRefreshing = true
                        }
                        null -> {
                            if (isUsersFirstTimeVisit && isConnectedToInternet(requireContext())){
                                binding.shimmerLayout.visibility = View.VISIBLE
                            }
                        }
                    }

                    //handle refresh
                    binding.root.isRefreshing = result is ApiStatus.Loading

                }

            }

        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                newsViewModel.snackErrorMessage.collect{ error->
                    //error here is the message returned from api (I don't wanna show that)
                    Utility.displayErrorSnackBar(binding.root, getString(R.string.not_connected_internet_error), requireContext())
                }
            }
        }


        lifecycleScope.launch{
            repeatOnLifecycle(Lifecycle.State.STARTED){
                newsViewModel.getRecentNews().collect{
                    recentNewsAdapter.submitData(it)
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        newsViewModel.getLatestPopularNews()
    }

    private fun setUpAdapter(){

        //popular news
        popularNewsAdapter = PopularNewsAdapter(PopularNewsAdapter.OnNewsItemClickListener{ article->
            //do something
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
            startActivity(intent)
        })

        binding.popularNewsRcv.apply {
            adapter = popularNewsAdapter
        }

        //recent news
        recentNewsAdapter = RecentNewsAdapter(RecentNewsAdapter.OnNewsItemClickListener{ article->
            //go to news newsDetailsFragment when recyclerview item clicked
            val action = HomeFragmentDirections.actionHomeFragmentToNewsDetailsFragment(article)
            findNavController().navigate(action)
        })

        binding.recentNewsRcv.apply {
            adapter = recentNewsAdapter
        }



    }

    private fun handleClicks(){

        binding.btnRetryInternet.setOnClickListener {
            checkIfUsersFirstTimeWithoutInternet()
        }

        binding.icThemeMode.setOnClickListener{
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

    }

    private fun handleSwipeToRefresh(){
        binding.root.setOnRefreshListener {
           newsViewModel.onRefreshSwiped()
        }
    }

}