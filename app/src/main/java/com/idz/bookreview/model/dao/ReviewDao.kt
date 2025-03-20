package com.idz.bookreview.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.idz.bookreview.model.Review

@Dao
interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    suspend fun getAllReviews(): List<Review>

    @Query("SELECT * FROM reviews WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getReviewsByUser(userId: String): List<Review>

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun deleteReview(id: Int)
}
