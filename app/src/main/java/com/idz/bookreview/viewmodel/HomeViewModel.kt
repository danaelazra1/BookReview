package com.idz.bookreview.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
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
    private val user = FirebaseAuth.getInstance().currentUser

    private val _reviewsLiveData = MutableLiveData<MutableList<Review>>()
    val reviewsLiveData: LiveData<MutableList<Review>> get() = _reviewsLiveData

    fun getReviews(): Flow<List<Review>> = flow {
        val localReviews = reviewDao.getAllReviews()
        emit(localReviews)

        try {
            val snapshot = reviewsCollection.get().await()
            val firestoreReviews = snapshot.toObjects(Review::class.java).map { review ->
                if (review.userId.isEmpty()) {
                    review.copy(userId = FirebaseAuth.getInstance().currentUser?.uid ?: "")
                } else review
            }
            emit(firestoreReviews)
            reviewDao.insertReviews(firestoreReviews)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun reloadAllReviews() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // טוען את כל הביקורות מ-ROOM
                val reviewsFromRoom = reviewDao.getAllReviews().toMutableList()
                _reviewsLiveData.postValue(reviewsFromRoom)

                // טוען את הביקורות מ-Firestore
                val snapshot = reviewsCollection.get().await()
                val firestoreReviews = snapshot.toObjects(Review::class.java)

                // שומר את סטטוס הלייק מ-ROOM
                firestoreReviews.forEach { firestoreReview ->
                    val localReview = reviewsFromRoom.find { it.id == firestoreReview.id }
                    if (localReview != null) {
                        firestoreReview.isLiked = localReview.isLiked  // שמירה על סטטוס הלייק
                    }
                }

                // עדכון ה-ROOM עם הביקורות מ-Firestore
                reviewDao.insertReviews(firestoreReviews)

                // עדכון ה-LiveData עם הנתונים המעודכנים
                _reviewsLiveData.postValue(reviewDao.getAllReviews().toMutableList())

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to reload reviews: ${e.message}")
            }
        }
    }


    fun updateReviewLikeStatus(review: Review) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // עדכון הסטטוס ב-ROOM
                reviewDao.updateReview(review)

                // עדכון ב-Firestore
                reviewsCollection.document(review.id)
                    .update("isLiked", review.isLiked)
                    .addOnFailureListener { e ->
                        Log.e("HomeViewModel", "Failed to update review in Firestore: ${e.message}")
                    }

                // לאחר העדכון ב-ROOM וב-Firestore, עדכן את ה-LiveData
                reloadAllReviews()

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to update like status: ${e.message}")
            }
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
