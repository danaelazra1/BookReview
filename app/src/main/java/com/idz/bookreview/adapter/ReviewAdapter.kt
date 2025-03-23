package com.idz.bookreview.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.idz.bookreview.R
import com.idz.bookreview.model.Review
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class ReviewAdapter(
    private val context: Context,
    private var reviews: MutableList<Review>,
    private val onEditClick: (Review) -> Unit,
    private val onDeleteClick: (String) -> Unit,
    private val sourceFragment: String  // מאיזה פרגמנט אנחנו מגיעים (HomeFragment או MyReviewsFragment)
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    fun updateReviews(newReviews: List<Review>) {
        reviews.clear()
        reviews.addAll(newReviews)
        notifyDataSetChanged()
    }

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookImageView: ImageView = itemView.findViewById(R.id.bookImageView)
        val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val bookTitleTextView: TextView = itemView.findViewById(R.id.bookTitleTextView)
        val bookAuthorTextView: TextView = itemView.findViewById(R.id.bookAuthorTextView)
        val reviewTextView: TextView = itemView.findViewById(R.id.reviewTextView)
        val editIcon: ImageView = itemView.findViewById(R.id.ic_edit)
        val deleteIcon: ImageView = itemView.findViewById(R.id.ic_trash)

        fun bind(review: Review) {
            userNameTextView.text = review.userName
            bookTitleTextView.text = review.title
            bookAuthorTextView.text = "Author: ${review.author}"
            reviewTextView.text = "Review: ${review.review}"
            dateTextView.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(review.timestamp))

            // טעינת תמונה באמצעות Picasso אם קיים URL תקין
            if (!review.imageUrl.isNullOrEmpty()) {
                Picasso.get().load(review.imageUrl).into(bookImageView)
            } else {
                bookImageView.setImageResource(R.drawable.ic_default_book)
            }

            // הצגת האייקונים רק אם אנחנו ב-MyReviewsFragment
            if (sourceFragment == "MyReviewsFragment" && review.userId == currentUserId) {
                editIcon.visibility = View.VISIBLE
                deleteIcon.visibility = View.VISIBLE

                // כפתור עריכה
                editIcon.setOnClickListener {
                    val bundle = Bundle().apply { putString("reviewId", review.id) }
                    it.findNavController().navigate(R.id.editReviewFragment, bundle)
                }

                // כפתור מחיקה
                deleteIcon.setOnClickListener {
                    onDeleteClick(review.id)
                }

            } else {
                // אם זה HomeFragment - לא נציג את האייקונים
                editIcon.visibility = View.GONE
                deleteIcon.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount(): Int = reviews.size
}
