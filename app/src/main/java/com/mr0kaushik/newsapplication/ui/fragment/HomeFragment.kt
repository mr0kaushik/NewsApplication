package com.mr0kaushik.newsapplication.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.mr0kaushik.newsapplication.Constants
import com.mr0kaushik.newsapplication.R
import com.mr0kaushik.newsapplication.Resource
import com.mr0kaushik.newsapplication.adapters.ArticleAdapter
import com.mr0kaushik.newsapplication.ui.MainActivity
import com.mr0kaushik.newsapplication.ui.viewmodels.ArticleViewModel
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : Fragment(R.layout.fragment_home), ChipGroup.OnCheckedChangeListener {

    lateinit var viewModel: ArticleViewModel

    lateinit var articleAdapter: ArticleAdapter

    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel

        setUpCategoryList()
        setUpRecyclerView()


        viewModel.topHeadlines.observe(viewLifecycleOwner, { response ->
            try {

                when (response) {
                    is Resource.Loading -> {
                        setProgressBar(View.VISIBLE)
                    }
                    is Resource.Success -> {
                        setProgressBar(View.INVISIBLE)
                        response.data?.let { headlinesResponse ->

                            try {
                                articleAdapter.differ.submitList(headlinesResponse.articles.toList())
                                val totalPages =
                                    headlinesResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                                isLastPage = viewModel.topHeadLinesPageNumber == totalPages
                            } catch (t: Throwable) {
                                Log.e(TAG, "onViewCreated: ", t)
                            }
                        }
                    }
                    is Resource.Error -> {
                        setProgressBar(View.INVISIBLE)
                        response.message?.let {
                            Snackbar.make(view, it, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }

            } catch (t: Throwable) {
                Log.e(TAG, "onViewCreated: ", t.fillInStackTrace())
            }
        })

    }



    private fun setUpCategoryList() {
        val categories = resources.getStringArray(R.array.news_categories)

        for (category in categories) {
            val chip = layoutInflater.inflate(R.layout.chip_layout, cgCategory, false) as Chip
            chip.apply {
                text = category
                id = ViewCompat.generateViewId()
                isCheckable = true
                isFocusable = true
                isClickable = true
            }
            cgCategory.addView(chip)
        }
        cgCategory.setOnCheckedChangeListener(this)
        cgCategory.isSingleSelection = true
        (cgCategory.getChildAt(0) as Chip).isChecked = true

    }


    private fun setProgressBar(status: Int) {
        paginationProgressBar.visibility = status
        isLoading = status == View.VISIBLE
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
            findNavController().navigate(R.id.action_homeFragment_to_articleFragment, bundle)
        }
    }

    override fun onCheckedChanged(group: ChipGroup?, checkedId: Int) {
        group?.findViewById<Chip>(checkedId)?.text?.let { text ->
            viewModel.resetTopHeadlinesPageNumber()
            viewModel.topHeadlinesCategory = if (text == getString(R.string.news_category_all)) {
                ""
            } else {
                text.toString().lowercase()
            }
            viewModel.getTopHeadlines()
        }

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
                viewModel.getTopHeadlines()
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

