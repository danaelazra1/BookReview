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
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid


    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookImageView: ImageView = itemView.findViewById(R.id.bookImageView)
        val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val bookTitleTextView: TextView = itemView.findViewById(R.id.bookTitleTextView)
        val bookAuthorTextView: TextView = itemView.findViewById(R.id.bookAuthorTextView)
        val reviewTextView: TextView = itemView.findViewById(R.id.reviewTextView)
        val editIcon: ImageView = itemView.findViewById(R.id.ic_edit)
        val deleteIcon: ImageView = itemView.findViewById(R.id.ic_trash)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        // קביעת הערכים של הטקסטים
        holder.userNameTextView.text = review.userName
        holder.bookTitleTextView.text = "${review.title}"
        holder.bookAuthorTextView.text = "Author: ${review.author}"
        holder.reviewTextView.text = "Review: ${review.review}"

        // עיצוב התאריך
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.dateTextView.text = dateFormat.format(Date(review.timestamp))

        // הצגת התמונה
        if (!review.imageUrl.isNullOrEmpty()) {
            Picasso.get().load(review.imageUrl).into(holder.bookImageView)
        } else {
            holder.bookImageView.setImageResource(R.drawable.ic_default_book)
        }

        if (review.userId == currentUserId) {
            holder.editIcon.visibility = View.VISIBLE
            holder.deleteIcon.visibility = View.VISIBLE

            // מעבר למסך העריכה
            holder.editIcon.setOnClickListener { view ->
                val bundle = Bundle().apply { putString("reviewId", review.id) }
                view.findNavController().navigate(R.id.editReviewFragment, bundle)
            }

            // מחיקת הביקורת מיידית
            holder.deleteIcon.setOnClickListener {
                onDeleteClick(review.id)
            }
        } else {
            holder.editIcon.visibility = View.GONE
            holder.deleteIcon.visibility = View.GONE
        }


    }

    override fun getItemCount(): Int = reviews.size
}
