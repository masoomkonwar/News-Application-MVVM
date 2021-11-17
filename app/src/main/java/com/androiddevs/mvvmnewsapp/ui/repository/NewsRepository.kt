package com.androiddevs.mvvmnewsapp.ui.repository

import com.androiddevs.mvvmnewsapp.ui.api.RetrofitInstance
import com.androiddevs.mvvmnewsapp.ui.database.ArticleDatabase
import com.androiddevs.mvvmnewsapp.ui.models.Article

class NewsRepository (
    val db : ArticleDatabase
        ) {

    suspend fun getBreakingNews(countryCode : String, pageNumber : Int) =
        RetrofitInstance.api.getBreakingNews(countryCode,pageNumber)

    suspend fun getSearchNews(seacrhQuery : String , pageNumber: Int) = RetrofitInstance.api.getAllNews(seacrhQuery,pageNumber)

    suspend fun upsert( article: Article) = db.getArticleDao().upsert(article)
    fun getSavedArticles() = db.getArticleDao().getAllArticles()

    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)

}