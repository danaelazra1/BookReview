package com.idz.bookreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idz.bookreview.model.Review
import com.idz.bookreview.model.dao.ReviewDao
import kotlinx.coroutines.launch

class ReviewViewModel(private val reviewDao: ReviewDao) : ViewModel() {

    val allReviews: LiveData<List<Review>> = reviewDao.getAllReviews()

    fun addReview(review: Review) {
        viewModelScope.launch {
            reviewDao.insertReview(review)
        }
    }

    fun getReviewsByUser(userId: String): LiveData<List<Review>> {
        return reviewDao.getReviewsByUser(userId)
    }
}



