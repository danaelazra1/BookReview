package com.idz.bookreview.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.idz.bookreview.model.Review
import com.idz.bookreview.model.dao.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeViewModel(private val context: Context) : ViewModel() {

    private val reviewDao = AppDatabase.getDatabase(context).reviewDao()
    private val firestore = FirebaseFirestore.getInstance()
    private val reviewsCollection = firestore.collection("reviews")

    fun getReviews(): Flow<List<Review>> = flow {
        val localReviews = withContext(Dispatchers.IO) { reviewDao.getAllReviews() }
        emit(localReviews)

        try {
            val snapshot = reviewsCollection.get().await()

            // שליפת הביקורות מ-Firestore ותוספת userId אם חסר
            val firestoreReviews = snapshot.toObjects(Review::class.java).map { review ->
                if (review.userId.isEmpty()) {
                    review.copy(userId = FirebaseAuth.getInstance().currentUser?.uid ?: "")
                } else review
            }

            emit(firestoreReviews)

            // שמירה של הביקורות ב-ROOM
            withContext(Dispatchers.IO) { reviewDao.insertReviews(firestoreReviews) }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun syncReviewsFromFirestore() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = reviewsCollection.get().await()
                val reviews = snapshot.toObjects(Review::class.java)
                reviewDao.insertReviews(reviews)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteReviewById(reviewId: String) {
        withContext(Dispatchers.IO) {
            reviewDao.deleteReviewById(reviewId)
        }
    }

}

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
