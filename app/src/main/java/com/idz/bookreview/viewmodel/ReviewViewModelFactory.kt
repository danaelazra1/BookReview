package com.idz.bookreview.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.idz.bookreview.model.dao.ReviewDao
import com.idz.bookreview.model.networking.FirebaseService

class ReviewViewModelFactory(
    private val reviewDao: ReviewDao,
    private val firebaseService: FirebaseService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            return ReviewViewModel(reviewDao, firebaseService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


