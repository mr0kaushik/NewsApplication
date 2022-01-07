package com.mr0kaushik.newsapplication.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mr0kaushik.newsapplication.Constants
import com.mr0kaushik.newsapplication.R
import com.mr0kaushik.newsapplication.Resource
import com.mr0kaushik.newsapplication.adapters.ArticleAdapter
import com.mr0kaushik.newsapplication.ui.MainActivity
import com.mr0kaushik.newsapplication.ui.viewmodels.ArticleViewModel
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {
    companion object {
        private const val TAG = "SearchFragment"
    }

    lateinit var viewModel: ArticleViewModel

    lateinit var articleAdapter: ArticleAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel

        setUpRecyclerView()

        setUpEditText()

        viewModel.searchNews.observe(viewLifecycleOwner, { response ->
            try {


                when (response) {
                    is Resource.Loading -> {
                        setProgressBar(View.VISIBLE)
                    }
                    is Resource.Success -> {
                        setProgressBar(View.INVISIBLE)
                        response.data?.let { headlinesResponse ->
                            articleAdapter.differ.submitList(headlinesResponse.articles.toList())
                            val totalPages =
                                headlinesResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                            isLastPage = viewModel.searchNewsPageNumber == totalPages
                        }
                    }
                    is Resource.Error -> {
                        setProgressBar(View.INVISIBLE)
                        response.message?.let { message ->
                            Log.e(
                                TAG,
                                "onViewCreated: HomeFragment Error Message $message"
                            )
                            Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
                        }
                    }
                }
            } catch (t: Throwable) {
                Log.e(TAG, "onViewCreated: ", t.fillInStackTrace())
            }
        })
    }

    private fun setUpEditText() {
        var job: Job? = null
        etSearchText.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(Constants.SEARCH_TEXT_DELAY)
                editable?.let {
                    if (editable.toString().isNotEmpty()) {
                        viewModel.searchNews(editable.toString())
                    }
                }
            }
        }
    }

    private fun setUpRecyclerView() {
        articleAdapter = ArticleAdapter()
        rvHeadlines.apply {
            adapter = articleAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(rvScrollListener)

        }


        articleAdapter.setOnItemClickListener { article ->
            val bundle = Bundle().apply {
                putSerializable("article", article)
            }
            findNavController().navigate(R.id.action_searchFragment_to_articleFragment, bundle)
        }
    }


    private fun setProgressBar(status: Int) {
        paginationProgressBar.visibility = status
        isLoading = status == View.VISIBLE
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    val rvScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount

            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE

            val shouldPaginate =
                isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                viewModel.searchNews(etSearchText.text.toString())
                isScrolling = false
            }

        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

}