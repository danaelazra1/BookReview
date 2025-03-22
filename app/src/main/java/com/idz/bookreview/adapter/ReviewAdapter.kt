package com.idz.bookreview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.idz.bookreview.R
import com.idz.bookreview.model.Review
import com.squareup.picasso.Picasso

class ReviewAdapter(
    private var reviewList: List<Review>,
    private val onFavoriteClick: (Review) -> Unit  // פונקציה ללחיצה על לב
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bookTitle: TextView = view.findViewById(R.id.bookTitleTextView)
        val bookDescription: TextView = view.findViewById(R.id.bookDescriptionTextView)
        val reviewText: TextView = view.findViewById(R.id.reviewTextView)
        val bookImage: ImageView = view.findViewById(R.id.bookImageView)
        val favoriteButton: ImageView = view.findViewById(R.id.favoriteButton)  // כפתור לב
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]

        holder.bookTitle.text = review.bookTitle
        holder.bookDescription.text = review.bookDescription
        holder.reviewText.text = review.reviewText

        if (!review.imageUrl.isNullOrEmpty()) {
            Picasso.get().load(review.imageUrl).into(holder.bookImage)
        } else {
            holder.bookImage.setImageResource(R.drawable.ic_placeholder)
        }

        // עדכון מצב הלב
        holder.favoriteButton.setImageResource(
            if (review.isFavorite) R.drawable.ic_check else R.drawable.ic_heart_outline
        )

        holder.favoriteButton.setOnClickListener {
            onFavoriteClick(review)
        }
    }

    override fun getItemCount(): Int = reviewList.size

    fun updateReviews(newReviews: List<Review>) {
        reviewList = newReviews
        notifyDataSetChanged()
    }
}