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
            "favoritedByUsers" to ArrayList(review.favoritedByUsers)
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
}
