package com.mr0kaushik.newsapplication.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mr0kaushik.newsapplication.NewsApplication
import com.mr0kaushik.newsapplication.Resource
import com.mr0kaushik.newsapplication.data.Article
import com.mr0kaushik.newsapplication.data.TopHeadlinesResponse
import com.mr0kaushik.newsapplication.repository.ArticleRepository
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class ArticleViewModel(
    app: Application,
    private val articleRepository: ArticleRepository
) : AndroidViewModel(app) {
    var countryCode: String = "in"

    val topHeadlines: MutableLiveData<Resource<TopHeadlinesResponse>> = MutableLiveData()
    var topHeadLinesPageNumber = 1
    var topHeadlinesCategory: String = ""
    var topHeadlinesResponse: TopHeadlinesResponse? = null


    val searchNews: MutableLiveData<Resource<TopHeadlinesResponse>> = MutableLiveData()
    var searchNewsPageNumber = 1

    private var searchNewsResponse: TopHeadlinesResponse? = null


    val isArticleExist: MutableLiveData<Boolean> = MutableLiveData(false)

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        searchNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = articleRepository.searchForNews(searchQuery, searchNewsPageNumber)
                searchNews.postValue(handleSearchQueryResponse(response))
            } else {
                searchNews.postValue(Resource.Error("No internet connection available"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchNews.postValue(Resource.Error("Network Failure Error"))
                else -> searchNews.postValue(Resource.Error(t.message.toString()))
            }
        }
    }

    fun getTopHeadlines() = viewModelScope.launch {
        topHeadlines.postValue(Resource.Loading())
        try {

            if (hasInternetConnection()) {

                val response =
                    articleRepository.getTopHeadlines(
                        countryCode,
                        topHeadLinesPageNumber,
                        topHeadlinesCategory
                    )
                topHeadlines.postValue(handleTopHeadlinesResponse(response))
            } else {
                topHeadlines.postValue(Resource.Error("No internet connection available"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> topHeadlines.postValue(Resource.Error("Network Failure Error"))
                else -> topHeadlines.postValue(Resource.Error(t.message.toString()))
            }
        }

    }

    fun resetTopHeadlinesPageNumber() {
        topHeadLinesPageNumber = 1
        topHeadlinesResponse = null
    }

    private fun handleTopHeadlinesResponse(response: Response<TopHeadlinesResponse>): Resource<TopHeadlinesResponse> {
        if (response.isSuccessful) {
            response.body()?.let { result ->
                topHeadLinesPageNumber++
                if (topHeadlinesResponse == null) {
                    topHeadlinesResponse = result
                } else {
                    val oldArticles = topHeadlinesResponse?.articles
                    oldArticles?.addAll(result.articles)
                }

                return Resource.Success(topHeadlinesResponse ?: result)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchQueryResponse(response: Response<TopHeadlinesResponse>): Resource<TopHeadlinesResponse> {
        if (response.isSuccessful) {
            response.body()?.let { result ->
                searchNewsPageNumber++
                if (searchNewsResponse == null) {
                    searchNewsResponse = result
                } else {
                    val oldArticles = searchNewsResponse?.articles
                    oldArticles?.addAll(result.articles)
                }
                return Resource.Success(searchNewsResponse ?: result)
            }
        }
        return Resource.Error(response.message())
    }

    fun isAlreadyPresent(url : String) = viewModelScope.launch {
        val result = articleRepository.getArticle(url)
        isArticleExist.postValue(result.value != null)
    }

    private fun setArticle(isPresent: Boolean) {
        isArticleExist.postValue(isPresent)
    }

    fun saveArticle(article: Article) = viewModelScope.launch {
        articleRepository.upsert(article)
        setArticle(true)
    }

    fun getSavedNews() = articleRepository.getSavedNews()

    fun delete(article: Article) = viewModelScope.launch {
        articleRepository.deleteArticle(article)
        setArticle(false)
    }

    companion object {
        private const val TAG = "ArticleViewModel"
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager =
            getApplication<NewsApplication>().getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }
}