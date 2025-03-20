package com.idz.bookreview.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
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
            var imageUrl: String? = null
            val userId = user?.uid ?: "unknown_user"

            // שליפה של שם המשתמש מתוך Firestore
            val userName = getUserNameFromFirestore(user?.email ?: "")

            if (imageUri != null) {
                if (imageUri.scheme == "content" || imageUri.scheme == "file") {
                    try {
                        val file = File(imageUri.path!!)
                        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

                        val response = CloudinaryService.api.uploadImage(part)
                        imageUrl = response.secureUrl
                        println("✔️ Image uploaded successfully: $imageUrl")
                    } catch (e: Exception) {
                        println("❌ Error uploading image to Cloudinary: ${e.message}")
                    }
                } else if (imageUri.scheme == "http" || imageUri.scheme == "https") {
                    try {
                        println("🔍 Trying to download image from URL: $imageUri")
                        val connection = java.net.URL(imageUri.toString()).openConnection()
                        connection.connectTimeout = 10000
                        connection.readTimeout = 10000
                        connection.doInput = true

                        val inputStream = connection.getInputStream()
                        if (inputStream != null) {
                            val byteArray = inputStream.readBytes()
                            inputStream.close()

                            if (byteArray.isNotEmpty()) {
                                val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), byteArray)
                                val part = MultipartBody.Part.createFormData("file", "image_from_api.jpg", requestBody)

                                val response = CloudinaryService.api.uploadImage(part)
                                imageUrl = response.secureUrl
                                println("✔️ Image uploaded from URL successfully to Cloudinary: $imageUrl")
                            } else {
                                println("❌ The byte array is empty. Failed to download image data.")
                            }
                        } else {
                            println("❌ Failed to open InputStream from URL.")
                        }
                    } catch (e: Exception) {
                        println("❌ Error uploading image from URL to Cloudinary: ${e.message}")
                    }
                }
            }

            // שמירת הביקורת ב-Firestore
            val reviewData = hashMapOf(
                "userId" to userId,
                "userName" to userName,  // משתמש בשם שהתקבל מ-Firestore
                "title" to title,
                "author" to author,
                "review" to review,
                "imageUrl" to imageUrl,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("reviews")
                .add(reviewData)
                .addOnSuccessListener { println("Review successfully saved to Firestore!") }
                .addOnFailureListener { e -> println("Error saving review: ${e.message}") }

            // שמירת הביקורת ב-Room Database
            val localReview = Review(
                userId = userId,
                userName = userName,
                bookTitle = title,
                author = author,
                reviewText = review,
                imageUrl = imageUrl,
                timestamp = System.currentTimeMillis()
            )

            try {
                reviewDao.insertReview(localReview)
                println("Review successfully saved to Room Database!")
            } catch (e: Exception) {
                println("Error saving review to Room Database: ${e.message}")
            }
        }
    }

    fun updateUserName() {
        loadUserName()  // מחדש את שם המשתמש לאחר שינוי
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