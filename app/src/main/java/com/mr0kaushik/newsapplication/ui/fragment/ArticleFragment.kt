package com.mr0kaushik.newsapplication.ui.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.mr0kaushik.newsapplication.R
import com.mr0kaushik.newsapplication.ui.MainActivity
import com.mr0kaushik.newsapplication.ui.viewmodels.ArticleViewModel
import kotlinx.android.synthetic.main.fragment_article.*

class ArticleFragment : DialogFragment(R.layout.fragment_article) {

    lateinit var viewModel: ArticleViewModel
    var isArticlePresent = false

    val args: ArticleFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel
        val article = args.article

        viewModel.isArticleExist.observe(viewLifecycleOwner, Observer { isPresent ->
            isArticlePresent = isPresent
            if (isPresent) {
                btnFab.setImageResource(R.drawable.ic_delete)
            } else {
                btnFab.setImageResource(R.drawable.ic_favorite)
            }
        })


        viewModel.isAlreadyPresent(article.url)


        val wvClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.INVISIBLE
            }
        }
        webView.apply {
            webViewClient = wvClient
            loadUrl(article.url)
        }


        btnFab.setOnClickListener {
            if (isArticlePresent) {
                viewModel.delete(article)
                Snackbar.make(
                    view,
                    "Article Removed from Favorite",
                    Snackbar.LENGTH_SHORT
                ).apply {
                    setAction("Undo") {
                        viewModel.saveArticle(article)
                    }
                }.show()
            } else {
                viewModel.saveArticle(article)
                Snackbar.make(view, "Article Saved", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}