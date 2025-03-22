package com.idz.bookreview.model.networking

import com.google.firebase.firestore.FirebaseFirestore
import com.idz.bookreview.model.Review
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseService {
    private val db = FirebaseFirestore.getInstance()
    private val reviewsCollection = db.collection("reviews")

    suspend fun addReviewToFirestore(review: Review): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                reviewsCollection.document(review.id).set(review).await()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun updateReviewInFirestore(review: Review) {
        FirebaseFirestore.getInstance()
            .collection("reviews")
            .document(review.id)
            .set(review)
    }

    fun deleteReviewFromFirestore(reviewId: String) {
        FirebaseFirestore.getInstance()
            .collection("reviews")
            .document(reviewId)
            .delete()
    }

}