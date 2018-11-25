package com.task.ui.component.news

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.task.data.remote.dto.NewsItem
import com.task.data.remote.dto.NewsModel
import com.task.ui.base.Presenter
import com.task.ui.base.listeners.BaseCallback
import com.task.ui.base.listeners.RecyclerItemListener
import com.task.usecase.NewsUseCase
import com.task.utils.ObjectUtil
import javax.inject.Inject

/**
 * Created by AhmedEltaher on 5/12/2016
 */

class HomePresenter @Inject
constructor(private val newsUseCase: NewsUseCase) : Presenter<HomeContract.View>(), HomeContract
.Presenter, RecyclerItemListener {

    override fun getRecyclerItemListener(): RecyclerItemListener {
        return this
    }

    override fun onItemSelected(position: Int) {
        getView()?.navigateToDetailsScreen(newsModel?.newsItems!![position])
    }

    @get:VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var newsModel: NewsModel? = null
        private set


    private val callback = object : BaseCallback {
        override fun onSuccess(newsModel: NewsModel) {
            getView()?.decrementCountingIdlingResource()
            this@HomePresenter.newsModel = newsModel
            var newsItems: List<NewsItem>? = null
            if (!ObjectUtil.isNull(newsModel)) {
                newsItems = newsModel.newsItems
            }
            if (!ObjectUtil.isNull(newsItems) && !newsItems!!.isEmpty()) {
                getView()?.initializeNewsList(newsModel.newsItems!!)
                showList(true)
            } else {
                showList(false)
            }
            getView()?.setLoaderVisibility(false)
        }

        override fun onFail() {
            getView()?.decrementCountingIdlingResource()
            showList(false)
            getView()?.setLoaderVisibility(false)
        }
    }

    override fun initialize(extras: Bundle?) {
        super.initialize(extras)
        getNews()
    }

    override fun getNews() {
        getView()?.setLoaderVisibility(true)
        getView()?.setNoDataVisibility(false)
        getView()?.setListVisibility(false)
        getView()?.incrementCountingIdlingResource()
        newsUseCase.getNews(callback)
    }

    override fun unSubscribe() {
        newsUseCase.unSubscribe()
    }

    override fun onSearchClick(newsTitle: String) {
        val news = newsModel!!.newsItems
        if (!ObjectUtil.isEmpty(newsTitle) && !ObjectUtil.isNull(news) && !news!!.isEmpty()) {
            val newsItem = newsUseCase.searchByTitle(news, newsTitle)
            if (!ObjectUtil.isNull(newsItem)) {
                getView()?.navigateToDetailsScreen(newsItem!!)
            } else {
                getView()?.showSearchError()
            }
        } else {
            getView()?.showSearchError()
        }
    }

    private fun showList(isVisible: Boolean) {
        getView()?.setNoDataVisibility(!isVisible)
        getView()?.setListVisibility(isVisible)
    }
}