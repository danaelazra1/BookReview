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
import java.io.Serializable

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
                val snapshot = reviewsCollection.get().await()
                val firestoreReviews = snapshot.toObjects(Review::class.java)

                if (firestoreReviews.isNotEmpty()) {
                    Log.d("HomeViewModel", "Reviews loaded from Firestore successfully.")
                    reviewDao.insertReviews(firestoreReviews)
                    _reviewsLiveData.postValue(firestoreReviews.toMutableList())
                } else {
                    Log.e("HomeViewModel", "No reviews found in Firestore.")
                }
            } catch (e: Exception) {
                Log.e(
                    "HomeViewModel",
                    "Failed to load reviews from Firestore. Loading from ROOM instead. Error: ${e.message}"
                )

                try {
                    val reviewsFromRoom = reviewDao.getAllReviews().toMutableList()
                    _reviewsLiveData.postValue(reviewsFromRoom)
                } catch (roomError: Exception) {
                    Log.e("HomeViewModel", "Failed to load reviews from ROOM: ${roomError.message}")
                }
            }
        }
    }

    fun updateReviewLikeStatus(review: Review) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = user?.uid ?: return@launch
                Log.d("HomeViewModel", "🔍 User ID: $userId")
                Log.d("HomeViewModel", "🔥 Current Likes: ${review.favoritedByUsers}")

                // שימוש ברשימה המעודכנת שהגיע מ-ReviewAdapter, בלי לשנות אותה כאן!
                val updatedLikes = review.favoritedByUsers.toMutableList()

                review.favoritedByUsers = updatedLikes
                Log.d(
                    "HomeViewModel",
                    "✅ Updated Review Object Before Sending to Firestore: $review"
                )

                val reviewData = hashMapOf(
                    "id" to review.id,
                    "userId" to review.userId,
                    "userName" to review.userName,
                    "title" to review.title,
                    "author" to review.author,
                    "review" to review.review,
                    "imageUrl" to review.imageUrl,
                    "timestamp" to review.timestamp,
                    "favoritedByUsers" to ArrayList(review.favoritedByUsers)
                )

                Log.d("HomeViewModel", "📤 Data Being Sent to Firestore: $reviewData")

                reviewsCollection.document(review.id)
                    .set(reviewData)
                    .addOnSuccessListener {
                        Log.d(
                            "HomeViewModel",
                            "🔥 Firestore Updated Successfully for Review ID: ${review.id}"
                        )
                        viewModelScope.launch(Dispatchers.IO) {
                            try {
                                Log.d(
                                    "HomeViewModel",
                                    "📦 Review Updated Successfully in ROOM with favoritedByUsers: ${review.favoritedByUsers}"
                                )
                            } catch (e: Exception) {
                                Log.e(
                                    "HomeViewModel",
                                    "❌ Error saving review to ROOM: ${e.message}"
                                )
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("HomeViewModel", "❌ Firestore Update Failed: ${e.message}")
                    }

            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ Error in updateReviewLikeStatus: ${e.message}")
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
            try {
                // מחיקת הביקורת מה-ROOM
                reviewDao.deleteReviewById(reviewId)

                // מחיקת הביקורת מ-Firestore
                reviewsCollection.document(reviewId).delete().await()
                Log.d("HomeViewModel", "Review deleted successfully from Firestore and Room.")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to delete review: ${e.message}")
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
}
