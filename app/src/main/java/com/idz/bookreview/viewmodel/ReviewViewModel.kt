package com.idz.bookreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idz.bookreview.model.Review
import com.idz.bookreview.model.dao.ReviewDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReviewViewModel(private val reviewDao: ReviewDao) : ViewModel() {

    val allReviews: LiveData<List<Review>> = reviewDao.getAllReviews()
    val favoriteReviews: LiveData<List<Review>> = reviewDao.getFavoriteReviewsLive()

    fun toggleFavorite(review: Review) {
        viewModelScope.launch(Dispatchers.IO) {
            if (review.isFavorite) {
                reviewDao.removeFromFavorites(review.id)
            } else {
                reviewDao.addToFavorites(review.id)
            }
        }
    }

    fun addReview(review: Review) {
        viewModelScope.launch(Dispatchers.IO) {
            reviewDao.insertReview(review) // ✅ שינוי השם לשם הנכון
        }
    }

    fun getReviewsByUser(userId: String): LiveData<List<Review>> {
        return reviewDao.getReviewsByUser(userId)
    }


}




