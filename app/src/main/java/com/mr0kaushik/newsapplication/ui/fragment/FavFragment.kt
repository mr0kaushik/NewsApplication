package com.mr0kaushik.newsapplication.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mr0kaushik.newsapplication.R
import com.mr0kaushik.newsapplication.adapters.ArticleAdapter
import com.mr0kaushik.newsapplication.ui.MainActivity
import com.mr0kaushik.newsapplication.ui.viewmodels.ArticleViewModel
import kotlinx.android.synthetic.main.fragment_fav.*

class FavFragment : Fragment(R.layout.fragment_fav) {
    lateinit var viewModel: ArticleViewModel
    lateinit var articleAdapter: ArticleAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel

        setUpRecyclerView()

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article = articleAdapter.differ.currentList[position]
                viewModel.delete(article)
                Snackbar.make(view, "Successfully deleted article", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        viewModel.saveArticle(article)
                    }
                }.show()
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(rvSavedArticles)
        }

        viewModel.getSavedNews().observe(viewLifecycleOwner, Observer { articles ->
            articleAdapter.differ.submitList(articles)
        })
    }

    private fun setUpRecyclerView() {
        articleAdapter = ArticleAdapter()
        rvSavedArticles.apply {
            adapter = articleAdapter
            layoutManager = LinearLayoutManager(activity)
        }


        articleAdapter.setOnItemClickListener { article ->
            val bundle = Bundle().apply {
                putSerializable("article", article)
            }
            findNavController().navigate(R.id.action_favFragment_to_articleFragment, bundle)
        }
    }
}