package com.idz.bookreview.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.bookreview.R
import com.idz.bookreview.adapter.ReviewAdapter
import com.idz.bookreview.model.Review
import com.idz.bookreview.viewmodel.ReviewViewModel

class MyReviewsFragment : Fragment() {

    private lateinit var reviewViewModel: ReviewViewModel
    private lateinit var recyclerView: RecyclerView
    private val reviewsList = mutableListOf<Review>()
    private lateinit var reviewAdapter: ReviewAdapter
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_reviews, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        reviewViewModel = ViewModelProvider(this)[ReviewViewModel::class.java]

        reviewAdapter = ReviewAdapter(
            requireContext(),
            reviewsList,
            onEditClick = { review ->
                Toast.makeText(requireContext(), "Edit review clicked for ${review.title}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { reviewId ->
                val position = reviewsList.indexOfFirst { it.id == reviewId }
                if (position != -1) {
                    reviewViewModel.deleteReview(reviewId, requireContext())
                    reviewsList.removeAt(position)
                    reviewAdapter.notifyItemRemoved(position)
                }
            },
            onLikeClick = { review ->
                reviewViewModel.updateReviewLikeStatus(review)  // שולח את העדכון ל-ViewModel
            },
            sourceFragment = "MyReviewsFragment"
        )

        recyclerView.adapter = reviewAdapter

        reviewViewModel.reviewsLiveData.observe(viewLifecycleOwner) { reviews ->
            if (reviews != null && reviews.isNotEmpty()) {
                val userReviews = reviews.filter { it.userId == userId }
                reviewsList.clear()
                reviewsList.addAll(userReviews)
                reviewAdapter.notifyDataSetChanged()
            }
        }

        fetchUserReviews()  // טוען ביקורות משתמש מ-Firestore

        return view
    }

    private fun fetchUserReviews() {
        if (userId == null) return

        db.collection("reviews")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val newList = mutableListOf<Review>()
                for (document in documents) {
                    val review = document.toObject(Review::class.java).copy(id = document.id)
                    if (review != null) newList.add(review)
                }

                if (newList.isNotEmpty()) {
                    reviewsList.clear()
                    reviewsList.addAll(newList)
                    reviewAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load reviews", Toast.LENGTH_SHORT).show()
            }
    }
}
