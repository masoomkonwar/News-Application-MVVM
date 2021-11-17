package com.androiddevs.mvvmnewsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.mvvmnewsapp.R
import com.androiddevs.mvvmnewsapp.ui.NewsActivity
import com.androiddevs.mvvmnewsapp.ui.NewsAdapter
import com.androiddevs.mvvmnewsapp.ui.NewsViewModel
import com.androiddevs.mvvmnewsapp.ui.utils.Constants
import com.androiddevs.mvvmnewsapp.ui.utils.Constants.Companion.TIME_DELAY
import com.androiddevs.mvvmnewsapp.ui.utils.Resource
import kotlinx.android.synthetic.main.fragment_breaking_news.*

import kotlinx.android.synthetic.main.fragment_search_news.*
import kotlinx.android.synthetic.main.fragment_search_news.paginationProgressBar
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchNewsFragment : Fragment(R.layout.fragment_search_news) {
    lateinit var viewModel : NewsViewModel
    lateinit var newsAdapter : NewsAdapter

    @Override
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel= (activity as NewsActivity).viewModel
        setUpRecyclerView()
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article",it)

            }
            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleFragment,
                bundle
            )
        }
        var job : Job? = null;
        etSearch.addTextChangedListener{ editable->
            job?.cancel()
            job = MainScope().launch {
                delay(TIME_DELAY)
                editable?.let {
                    if(editable.toString().isNotEmpty())
                    {
                        viewModel.getSearchNews(editable.toString())
                    }
                }
            }
        }


        viewModel.searchNews.observe(viewLifecycleOwner, Observer {
                response->
            when(response)
            {
                is Resource.Success->{
                    println("success")
                    paginationProgressBar.visibility = View.INVISIBLE
                    isLoading = false
                    response.data?.let { newsResponse->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastpage = viewModel.searchNewsPage == totalPages
                    }
                }
                is Resource.Loading->{
                    println("loading")
                    paginationProgressBar.visibility = View.VISIBLE
                    isLoading=true
                }
                is Resource.Error->{
                    paginationProgressBar.visibility = View.INVISIBLE
                    isLoading=false
                    response.message?.let { message->
                        Log.e("not angry :",message)
                    }

                }
            }
        })
    }

    private fun setUpRecyclerView(){
        newsAdapter = NewsAdapter()
        println("in breaking news fragment")
        rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListner)
        }
    }
    var isLoading = false
    var isLastpage = false
    var isScrolling = false
    val scrollListner = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
            {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisItem = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val isNLandNNP = !isLoading && !isLastpage
            val isAtLastItem = firstVisItem + visibleItemCount >= totalItemCount
            val isNotAtbeginig = firstVisItem >=0
            val isTotalMoreThanVis = totalItemCount >= Constants.QUERY_PAGE_SIZE

            val ifPaginate = isNLandNNP and isAtLastItem and isNotAtbeginig and isTotalMoreThanVis and isScrolling

            if (ifPaginate)
            {
                viewModel.getSearchNews(etSearch.text.toString())
                isScrolling = false
            }
            else{
                rvSearchNews.setPadding(0,0,0,0)
            }

        }
    }
}