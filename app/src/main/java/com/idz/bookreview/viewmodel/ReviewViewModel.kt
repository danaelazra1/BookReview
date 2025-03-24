package com.idz.bookreview.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.bookreview.api.CloudinaryService
import com.idz.bookreview.model.Review
import com.idz.bookreview.model.dao.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ReviewViewModel(application: Application) : AndroidViewModel(application) {

    private val _review = MutableLiveData<Review?>()
    val review: LiveData<Review?> get() = _review

    private val _reviewsLiveData = MutableLiveData<MutableList<Review>>()
    val reviewsLiveData: LiveData<MutableList<Review>> get() = _reviewsLiveData

    private val firestore = FirebaseFirestore.getInstance()
    private val roomDao = AppDatabase.getDatabase(application).reviewDao()
    private val user = FirebaseAuth.getInstance().currentUser


    fun loadReviewById(reviewId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val localReview = roomDao.getReviewById(reviewId)
                if (localReview != null) {
                    _review.postValue(localReview)
                    Log.d("ReviewViewModel", "Review loaded from Room successfully.")
                } else {
                    val document = firestore.collection("reviews").document(reviewId).get().await()
                    if (document.exists()) {
                        val firestoreReview = document.toObject(Review::class.java)
                        if (firestoreReview != null) {
                            _review.postValue(firestoreReview)
                            saveReviewLocally(firestoreReview)
                            Log.d("ReviewViewModel", "Review loaded from Firestore successfully.")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ReviewViewModel", "Failed to load review: ${e.message}")
            }
        }
    }

    fun saveReview(updatedReview: Review, imagePath: String? = null, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val userId = updatedReview.userId.takeIf { it.isNotEmpty() } ?: user?.uid ?: "unknown_user"
            val reviewToSave = updatedReview.copy(userId = userId)

            if (imagePath != null && imagePath.isNotEmpty()) {
                val imageUrl = uploadImageToCloudinary(imagePath, context)
                if (imageUrl != null) {
                    val updatedReviewWithImage = reviewToSave.copy(imageUrl = imageUrl)
                    saveReviewToFirestoreAndRoom(updatedReviewWithImage)
                } else {
                    saveReviewToFirestoreAndRoom(reviewToSave)
                }
            } else {
                saveReviewToFirestoreAndRoom(reviewToSave)
            }
        }
    }

    private suspend fun uploadImageToCloudinary(imagePath: String, context: Context): String? {
        return try {
            val file = File(imagePath)
            if (!file.exists()) return null

            val requestBody = file.asRequestBody()
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestBody)
            val response = CloudinaryService.api.uploadImage(filePart)

            response.secureUrl
        } catch (e: Exception) {
            Log.e("ReviewViewModel", "Failed to upload image to Cloudinary: ${e.message}")
            null
        }
    }

    private fun saveReviewToFirestoreAndRoom(review: Review) {
        val reviewData = hashMapOf(
            "id" to review.id,
            "userId" to review.userId,
            "userName" to review.userName,
            "title" to review.title,
            "author" to review.author,
            "review" to review.review,
            "imageUrl" to review.imageUrl,
            "timestamp" to review.timestamp,
            "isLiked" to review.isLiked  // שמירת המידע על הלייק
        )

        firestore.collection("reviews").document(review.id)
            .set(reviewData)
            .addOnSuccessListener {
                viewModelScope.launch(Dispatchers.IO) {
                    roomDao.updateReview(review)
                }
                Log.d("ReviewViewModel", "Review saved successfully.")
            }
            .addOnFailureListener { exception ->
                Log.e("ReviewViewModel", "Failed to save review: ${exception.message}")
            }
    }

    private fun saveReviewLocally(review: Review) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existingReview = roomDao.getReviewById(review.id)
                if (existingReview != null) {
                    roomDao.updateReview(review)
                } else {
                    roomDao.insertReview(review)
                }
            } catch (e: Exception) {
                Log.e("ReviewViewModel", "Failed to save/update review in Room: ${e.message}")
            }
        }
    }

    fun deleteReview(reviewId: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val reviewSnapshot = firestore.collection("reviews").document(reviewId).get().await()
                val reviewUserId = reviewSnapshot.getString("userId")

                if (reviewUserId == user?.uid) {
                    firestore.collection("reviews").document(reviewId).delete().await()
                    roomDao.deleteReviewById(reviewId)

                    CoroutineScope(Dispatchers.Main).launch {
                        val currentList = _reviewsLiveData.value ?: mutableListOf()
                        val updatedList = currentList.filter { it.id != reviewId }.toMutableList()
                        _reviewsLiveData.value = updatedList
                        Toast.makeText(context, "Review deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ReviewViewModel", "Error deleting review: ${e.message}")
            }
        }
    }


    fun reloadAllReviews() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 🔥 שליפת כל הביקורות מ-ROOM
                val reviewsFromRoom = roomDao.getAllReviews().toMutableList()
                _reviewsLiveData.postValue(reviewsFromRoom)

                // 🔥 שליפת ביקורות מ-Firestore ועדכון ה-ROOM
                val snapshot = firestore.collection("reviews").get().await()
                val firestoreReviews = snapshot.toObjects(Review::class.java)

                // 🔥 שמירה על הסטטוס של ה-isLiked שנמצא ב-ROOM
                for (firestoreReview in firestoreReviews) {
                    val localReview = reviewsFromRoom.find { it.id == firestoreReview.id }
                    if (localReview != null) {
                        firestoreReview.isLiked = localReview.isLiked  // שומר את הערך הנכון מ-ROOM
                    }
                }

                // 🔥 עדכון ה-ROOM עם המידע המעודכן מ-Firestore
                roomDao.insertReviews(firestoreReviews)

                // 🔥 עדכון ה-LiveData עם הנתונים המעודכנים
                _reviewsLiveData.postValue(roomDao.getAllReviews().toMutableList())

            } catch (e: Exception) {
                Log.e("ReviewViewModel", "Failed to reload reviews: ${e.message}")
            }
        }
    }

    fun updateReviewLikeStatus(review: Review) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 🔥 עדכון ב-Firestore
                firestore.collection("reviews").document(review.id)
                    .update("isLiked", review.isLiked)
                    .addOnSuccessListener {
                        Log.d("ReviewViewModel", "Review updated in Firestore successfully.")

                        // 🔥 עדכון ב-ROOM
                        viewModelScope.launch(Dispatchers.IO) {
                            roomDao.updateReview(review)

                            val currentList = _reviewsLiveData.value?.toMutableList() ?: mutableListOf()
                            val index = currentList.indexOfFirst { it.id == review.id }
                            if (index != -1) {
                                currentList[index] = review
                                _reviewsLiveData.postValue(currentList)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("ReviewViewModel", "Failed to update review in Firestore: ${e.message}")
                    }

            } catch (e: Exception) {
                Log.e("ReviewViewModel", "Failed to update like status: ${e.message}")
            }
        }
    }

}
