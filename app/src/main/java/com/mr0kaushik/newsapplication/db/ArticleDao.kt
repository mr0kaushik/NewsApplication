package com.mr0kaushik.newsapplication.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mr0kaushik.newsapplication.data.Article

@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(article: Article): Long

    @Query("SELECT * FROM articles")
    fun getAllArticles(): LiveData<List<Article>>

    @Delete()
    suspend fun delete(article: Article)

    @Query("SELECT * FROM articles WHERE url==:url")
    fun getArticleFromURL(url: String): LiveData<Article?>

    @Query("SELECT EXISTS(SELECT * FROM articles WHERE url==:url)")
    fun isArticleExist(url: String): LiveData<Boolean>
}