package com.idz.bookreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idz.bookreview.model.Review
import com.idz.bookreview.model.dao.ReviewDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.idz.bookreview.model.networking.FirebaseService

class ReviewViewModel(
    private val reviewDao: ReviewDao,
    private val firebaseService: FirebaseService
) : ViewModel() {

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
            reviewDao.insertReview(review) // שמירה במסד הנתונים המקומי
            firebaseService.addReviewToFirestore(review) //  שמירה ב-Firestore
        }
    }

    fun getReviewsByUser(userId: String): LiveData<List<Review>> {
        return reviewDao.getReviewsByUser(userId)
    }

    fun updateReview(review: Review) {
        viewModelScope.launch {
            reviewDao.updateReview(review) // עדכון ב־Room
            firebaseService.updateReviewInFirestore(review) // עדכון ב־Firestore
        }
    }


    fun deleteReview(review: Review) {
        viewModelScope.launch {
            reviewDao.deleteReview(review) // מחיקה מ־Room
            firebaseService.deleteReviewFromFirestore(review.id) // מחיקה מ־Firestore
        }
    }


}
