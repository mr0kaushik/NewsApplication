package com.mr0kaushik.newsapplication.repository

import com.mr0kaushik.newsapplication.api.RetrofitInstance
import com.mr0kaushik.newsapplication.data.Article
import com.mr0kaushik.newsapplication.db.ArticleDatabase

class ArticleRepository(
    val db: ArticleDatabase
) {

    suspend fun getTopHeadlines(countryCode: String, pageNumber: Int, category: String) =
        RetrofitInstance.api.getTopHeadlines(
            countryCode = countryCode,
            category = category,
            pageNumber
        )

    suspend fun searchForNews(searchQuery: String, pageNumber: Int) =
        RetrofitInstance.api.searchForNew(
            searchQuery, pageNumber
        )

    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)

    suspend fun deleteArticle(article: Article) = db.getArticleDao().delete(article)

    fun getSavedNews() = db.getArticleDao().getAllArticles()

    fun getArticle(url: String) = db.getArticleDao().getArticleFromURL(url)

    fun isArticleExist(url: String) = db.getArticleDao().isArticleExist(url)
}