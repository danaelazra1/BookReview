package com.idz.bookreview.model.dao


import androidx.lifecycle.LiveData
import androidx.room.*
import com.idz.bookreview.model.Review

@Dao
interface ReviewDao {

    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    fun getAllReviews(): LiveData<List<Review>>

    @Query("SELECT * FROM reviews WHERE userId = :userId ORDER BY timestamp DESC")
    fun getReviewsByUser(userId: String): LiveData<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    @Update
    suspend fun updateReview(review: Review)

    @Delete
    suspend fun deleteReview(review: Review)

    @Query("SELECT * FROM reviews WHERE isFavorite = 1")
    fun getFavoriteReviewsLive(): LiveData<List<Review>>

    @Query("UPDATE reviews SET isFavorite = 1 WHERE id = :reviewId")
    suspend fun addToFavorites(reviewId: String)

    @Query("UPDATE reviews SET isFavorite = 0 WHERE id = :reviewId")
    suspend fun removeFromFavorites(reviewId: String)
}
