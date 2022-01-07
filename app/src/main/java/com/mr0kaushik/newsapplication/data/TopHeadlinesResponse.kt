package com.mr0kaushik.newsapplication.data

data class TopHeadlinesResponse(
    val articles: MutableList<Article>,
    val status: String,
    val totalResults: Int
)