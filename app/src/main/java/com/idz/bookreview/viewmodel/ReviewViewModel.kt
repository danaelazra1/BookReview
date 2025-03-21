package com.idz.bookreview.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    private val _review = MutableLiveData<Review?>()  // אפשרנו ערך null
    val review: LiveData<Review?> get() = _review

    private val firestore = FirebaseFirestore.getInstance()
    private val roomDao = AppDatabase.getDatabase(application).reviewDao()
    private val user = FirebaseAuth.getInstance().currentUser

    private suspend fun getUserNameFromFirestore(userEmail: String): String {
        var userName = "Unknown User"
        try {
            val documentSnapshot = firestore.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .await()

            if (!documentSnapshot.isEmpty) {
                val userDoc = documentSnapshot.documents[0]
                userName = userDoc.getString("username") ?: "Unknown User"
            }
        } catch (e: Exception) {
            Log.e("ReviewViewModel", "Error fetching user data from Firestore: ${e.message}")
        }
        return userName
    }

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
                        } else {
                            Log.e("ReviewViewModel", "Review from Firestore is null.")
                        }
                    } else {
                        Log.e("ReviewViewModel", "No review found in Firestore with ID: $reviewId")
                    }
                }
            } catch (e: Exception) {
                Log.e("ReviewViewModel", "Failed to load review: ${e.message}")
            }
        }
    }

    fun saveReview(updatedReview: Review, imagePath: String? = null, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val userId = user?.uid ?: "unknown_user"

            if (imagePath != null && imagePath.isNotEmpty()) {
                val imageUrl = uploadImageToCloudinary(imagePath, context)
                if (imageUrl != null) {
                    // עדכון הביקורת עם הכתובת החדשה של התמונה
                    val updatedReviewWithImage = updatedReview.copy(imageUrl = imageUrl)
                    saveReviewToFirestoreAndRoom(updatedReviewWithImage, userId)
                } else {
                    Log.e("ReviewViewModel", "Image upload failed - saving without image.")
                    saveReviewToFirestoreAndRoom(updatedReview, userId)
                }
            } else {
                // אין תמונה חדשה, נמשיך לשמור את הביקורת כמו שהיא
                saveReviewToFirestoreAndRoom(updatedReview, userId)
            }
        }
    }


    private suspend fun uploadImageToCloudinary(imagePath: String, context: Context): String? {
        return try {
            val file = File(imagePath)
            if (!file.exists()) {
                Log.e("ReviewViewModel", "File not found: $imagePath")
                return null
            }

            val requestBody = file.asRequestBody()
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestBody)
            val response = CloudinaryService.api.uploadImage(filePart)

            Log.d("ReviewViewModel", "Image uploaded successfully: ${response.secureUrl}")
            response.secureUrl
        } catch (e: Exception) {
            Log.e("ReviewViewModel", "Failed to upload image to Cloudinary: ${e.message}")
            null
        }
    }


    private fun saveReviewToFirestoreAndRoom(updatedReview: Review, userId: String) {
        val reviewData = hashMapOf(
            "id" to updatedReview.id,
            "userId" to userId,
            "userName" to updatedReview.userName,
            "title" to updatedReview.title,
            "author" to updatedReview.author,
            "review" to updatedReview.review,
            "imageUrl" to updatedReview.imageUrl,  // מעדכן את התמונה החדשה או ריק אם אין תמונה
            "timestamp" to updatedReview.timestamp
        )

        firestore.collection("reviews").document(updatedReview.id)
            .set(reviewData)
            .addOnSuccessListener {
                _review.postValue(updatedReview)
                saveReviewLocally(updatedReview)
                Log.d("ReviewViewModel", "Review saved to Firestore successfully.")
            }
            .addOnFailureListener { exception ->
                Log.e("ReviewViewModel", "Failed to save review to Firestore: ${exception.message}")
            }
    }

    private fun saveReviewLocally(review: Review) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                roomDao.insertReview(review)
                Log.d("ReviewViewModel", "Review saved to Room successfully.")
            } catch (e: Exception) {
                Log.e("ReviewViewModel", "Failed to save review to Room: ${e.message}")
            }
        }
    }



}
