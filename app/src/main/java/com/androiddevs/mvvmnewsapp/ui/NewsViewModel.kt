package com.androiddevs.mvvmnewsapp.ui

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.mvvmnewsapp.ui.models.Article
import com.androiddevs.mvvmnewsapp.ui.models.NewsResponse
import com.androiddevs.mvvmnewsapp.ui.repository.NewsRepository
import com.androiddevs.mvvmnewsapp.ui.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel (
    val newsRepository: NewsRepository,
    app : Application
        ) : AndroidViewModel(app) {
            val breakingNews : MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
            var breakingNewsPage = 1
            var breakingNewsResponse : NewsResponse?=null


            val searchNews : MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
            var searchNewsPage = 1
            var searchNewsResponse : NewsResponse?=null

            val savedNews : MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
            init {
                getBreakingNews("in")
                Log.d("TAG_1","getting breaking news")
            }
            fun getBreakingNews(countryCode : String) = viewModelScope.launch {

                safeBreakingNewsCall(countryCode)
            }

            fun getSearchNews(searchQuery: String) = viewModelScope.launch {
                searchNews.postValue(Resource.Loading())
                val response = newsRepository.getSearchNews(searchQuery,searchNewsPage)
                searchNews.postValue(handleSearchNews(response))
            }

            private  fun handleBreakingNews(response: Response<NewsResponse>): Resource<NewsResponse>
            {
                if (response.isSuccessful){
                    response.body()?.let { resultResponse->

                        breakingNewsPage++

                        if(breakingNewsResponse==null)

                        {
                            breakingNewsResponse = resultResponse
                        }
                        else{
                            var oldArticles = breakingNewsResponse?.articles
                            val newArticles = resultResponse.articles
                            oldArticles?.addAll(newArticles)
                            if (oldArticles != null) {
                                resultResponse.articles = oldArticles
                            }
                        }
                        return Resource.Success(resultResponse)
                    }
                }
                return Resource.Error(response.message())
            }

            private  fun handleSearchNews(response: Response<NewsResponse>): Resource<NewsResponse>
            {
                if (response.isSuccessful){
                    response.body()?.let { resultResponse->

                        searchNewsPage++

                        if(searchNewsResponse==null)
                        {
                            searchNewsResponse = resultResponse
                        }
                        else{
                            val oldArticles = searchNewsResponse?.articles
                            val newArticles = resultResponse.articles
                            oldArticles?.addAll(newArticles)
                            oldArticles?.addAll(newArticles)
                            if (oldArticles != null) {
                                resultResponse.articles = oldArticles
                            }
                        }
                        return Resource.Success(resultResponse)
                    }
                }
                return Resource.Error(response.message())
            }
            private suspend fun safeBreakingNewsCall(countryCode: String)
            {
                breakingNews.postValue(Resource.Loading())
                try {
                    if (hasInternetService()){
                        val response = newsRepository.getBreakingNews(countryCode,breakingNewsPage)
                        breakingNews.postValue(handleBreakingNews(response))
                    }
                    else{
                        breakingNews.postValue(Resource.Error("an error has occured"))
                    }

                }catch (t: Throwable)
                {
                    when(t){
                        is IOException -> breakingNews.postValue(Resource.Error("network falure"))
                        else -> breakingNews.postValue(Resource.Error("Conversion error"))
                    }
                }
            }
            fun saveArticle(article: Article) = viewModelScope.launch {
                newsRepository.upsert(article)
            }
            fun getSavedNews() = newsRepository.getSavedArticles()
            fun deleteArticle(article: Article) = viewModelScope.launch {
                newsRepository.deleteArticle(article)
            }
            fun hasInternetService() : Boolean{

                val connectivityManager = getApplication<NewsApplication>().getSystemService(
                    Context.CONNECTIVITY_SERVICE
                ) as ConnectivityManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    val activeNet = connectivityManager.activeNetwork?: return false
                    val capabilities = connectivityManager.getNetworkCapabilities(activeNet) ?: return false
                    return when{
                        capabilities.hasTransport(TRANSPORT_WIFI) -> true
                        capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                        capabilities.hasTransport(TRANSPORT_ETHERNET) -> true

                        else ->false
                    }

                }
                else{
                    connectivityManager.activeNetworkInfo?.run {
                        return when(type){
                            TYPE_WIFI -> true
                            TYPE_MOBILE -> true
                            TYPE_ETHERNET->true
                            else -> false
                        }
                    }
                }
                return false
            }
}