package com.mr0kaushik.newsapplication.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mr0kaushik.newsapplication.repository.ArticleRepository

class ArticleViewModelProviderFactory(
    val app: Application,
    private val articleRepository: ArticleRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ArticleViewModel(app, articleRepository) as T
    }

}