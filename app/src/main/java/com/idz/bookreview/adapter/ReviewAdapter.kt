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
    private val onLikeClick: (Review) -> Unit,
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
        val likeIcon: ImageView = itemView.findViewById(R.id.likeIcon)

        fun bind(review: Review) {
            userNameTextView.text = review.userName
            bookTitleTextView.text = review.title
            bookAuthorTextView.text = "Author: ${review.author}"
            reviewTextView.text = "Review: ${review.review}"
            dateTextView.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(review.timestamp))

            if (!review.imageUrl.isNullOrEmpty()) {
                Picasso.get().load(review.imageUrl).into(bookImageView)
            } else {
                bookImageView.setImageResource(R.drawable.ic_default_book)
            }

            if (sourceFragment == "MyReviewsFragment") {
                // אם אנחנו בדף "MyReviewsFragment", הסתר את אייקון הלייק
                likeIcon.visibility = View.GONE
            } else {
                // אם אנחנו לא בדף "MyReviewsFragment", הצג את אייקון הלייק
                likeIcon.visibility = View.VISIBLE

                // 🔥 הגדרת האייקון לפי מצב הלייק
                if (review.isLiked) {
                    likeIcon.setImageResource(R.drawable.ic_heart_filled)
                } else {
                    likeIcon.setImageResource(R.drawable.ic_heart_outline)
                }

                // עדכון ה-UI של הלייק כאשר יש לחיצה עליו
                likeIcon.setOnClickListener {
                    review.isLiked = !review.isLiked  // שינוי מצב הלייק
                    // עדכון מיידי של האייקון כדי לראות את השינוי ב-UI
                    if (review.isLiked) {
                        likeIcon.setImageResource(R.drawable.ic_heart_filled)
                    } else {
                        likeIcon.setImageResource(R.drawable.ic_heart_outline)
                    }
                    onLikeClick(review)  // שולח את העדכון ל-ViewModel
                }
            }

            // בדיקה אם אנחנו בדף "MyReviewsFragment" ומציגים רק את האייקונים של העריכה והמחיקה
            if (sourceFragment == "MyReviewsFragment" && review.userId == currentUserId) {
                editIcon.visibility = View.VISIBLE
                deleteIcon.visibility = View.VISIBLE

                editIcon.setOnClickListener {
                    val bundle = Bundle().apply { putString("reviewId", review.id) }
                    it.findNavController().navigate(R.id.editReviewFragment, bundle)
                }

                deleteIcon.setOnClickListener {
                    onDeleteClick(review.id)
                }
            } else {
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
        val review = reviews[position]
        holder.bind(review)

        holder.likeIcon.setOnClickListener {
            review.isLiked = !review.isLiked  // שינוי מצב הלייק

            // עדכון מיידי של האייקון כדי לראות את השינוי ב-UI
            if (review.isLiked) {
                holder.likeIcon.setImageResource(R.drawable.ic_heart_filled)
            } else {
                holder.likeIcon.setImageResource(R.drawable.ic_heart_outline)
            }

            // 🔥 עדכון ה-ViewModel עם המידע החדש
            onLikeClick(review)
        }
    }




    override fun getItemCount(): Int = reviews.size
}
