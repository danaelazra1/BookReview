package com.idz.bookreview.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.idz.bookreview.model.Review

@Dao
interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<Review>)

    @Update
    suspend fun updateReview(review: Review)  // פונקציית עדכון חדשה

    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    suspend fun getAllReviews(): List<Review>

    @Query("SELECT * FROM reviews WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getReviewsByUser(userId: String): List<Review>


    @Query("SELECT * FROM reviews WHERE isLiked = 1 ORDER BY timestamp DESC")
    suspend fun getLikedReviews(): List<Review>

    @Query("SELECT * FROM reviews WHERE id = :id")
    suspend fun getReviewById(id: String): Review?  // פונקציה לשליפת ביקורת בודדת לפי ID

    @Query("DELETE FROM reviews WHERE id = :reviewId")
    suspend fun deleteReviewById(reviewId: String)
}
