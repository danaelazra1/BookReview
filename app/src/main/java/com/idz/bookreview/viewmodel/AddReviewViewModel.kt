
package com.idz.bookreview.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.bookreview.api.CloudinaryService
import com.idz.bookreview.model.Review
import com.idz.bookreview.model.dao.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

class AddReviewViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val reviewDao = AppDatabase.getDatabase(application).reviewDao()
    private val user = FirebaseAuth.getInstance().currentUser

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _imageUploadCompleted = MutableLiveData<Boolean>()
    val imageUploadCompleted: LiveData<Boolean> get() = _imageUploadCompleted


    init {
        loadUserName()
    }

    private fun loadUserName() {
        viewModelScope.launch(Dispatchers.IO) {
            val userName = getUserNameFromFirestore(user?.email ?: "")
            _userName.postValue(userName)
        }
    }



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
            println("Error fetching user data from Firestore: ${e.message}")
        }
        return userName
    }

    fun saveReview(title: String, author: String, review: String, imageUri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = user?.uid ?: "unknown_user"
            val userName = getUserNameFromFirestore(user?.email ?: "")
            val timestamp = System.currentTimeMillis()
            val reviewId = firestore.collection("reviews").document().id

            val reviewData = hashMapOf(
                "id" to reviewId,
                "userId" to userId,
                "userName" to userName,
                "title" to title,
                "author" to author,
                "review" to review,
                "imageUrl" to null,
                "timestamp" to timestamp,
                "favoritedByUsers" to ArrayList<String>()
            )

            firestore.collection("reviews")
                .document(reviewId)
                .set(reviewData)
                .addOnSuccessListener { Log.d("AddReviewViewModel", "Review successfully saved to Firestore!") }
                .addOnFailureListener { e -> Log.e("AddReviewViewModel", "Error saving review: ${e.message}") }

            val localReview = Review(
                id = reviewId,
                userId = userId,
                userName = userName,
                title = title,
                author = author,
                review = review,
                imageUrl = null,
                timestamp = timestamp,
                favoritedByUsers = emptyList()
            )

            try {
                reviewDao.insertReview(localReview)
                Log.d("AddReviewViewModel", "Review successfully saved to Room Database!")
            } catch (e: Exception) {
                Log.e("AddReviewViewModel", "Error saving review to Room Database: ${e.message}")
            }

            if (imageUri != null) {
                uploadImageToCloudinary(imageUri, reviewId, localReview)
            }
        }
    }

    private fun uploadImageToCloudinary(imageUri: Uri, reviewId: String, localReview: Review) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var imageUrl: String? = null

                if (imageUri.scheme == "http" || imageUri.scheme == "https") {
                    val inputStream = java.net.URL(imageUri.toString()).openStream()
                    val file = File.createTempFile("tempImage", ".jpg", getApplication<Application>().cacheDir)
                    file.outputStream().use { outputStream -> inputStream.copyTo(outputStream) }

                    val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                    val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

                    imageUrl = withContext(Dispatchers.IO) {
                        CloudinaryService.api.uploadImage(part).secureUrl
                    }

                    file.delete()
                } else if (imageUri.scheme == "content" || imageUri.scheme == "file") {
                    val file = File(imageUri.path!!)
                    val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                    val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

                    imageUrl = withContext(Dispatchers.IO) {
                        CloudinaryService.api.uploadImage(part).secureUrl
                    }
                }

                if (imageUrl != null) {
                    firestore.collection("reviews")
                        .document(reviewId)
                        .update("imageUrl", imageUrl)
                        .addOnSuccessListener { _imageUploadCompleted.postValue(true) }
                    val updatedReview = localReview.copy(imageUrl = imageUrl)
                    reviewDao.updateReview(updatedReview)
                    Log.d("AddReviewViewModel", "Review imageUrl updated successfully in Room Database!")
                }

            } catch (e: Exception) {
                Log.e("AddReviewViewModel", "Error uploading image to Cloudinary: ${e.message}")
            }
        }
    }


    fun updateUserName() {
        loadUserName()
    }

    private fun getBytesFromUri(context: Context, uri: Uri): ByteArray? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val buffer = ByteArrayOutputStream()
            val bytes = ByteArray(1024)
            var read: Int

            while (inputStream?.read(bytes).also { read = it ?: -1 } != -1) {
                buffer.write(bytes, 0, read)
            }

            buffer.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
