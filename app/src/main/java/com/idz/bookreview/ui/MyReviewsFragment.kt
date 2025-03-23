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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_reviews, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        reviewViewModel = ViewModelProvider(this)[ReviewViewModel::class.java]

        reviewViewModel.reviewsLiveData.observe(viewLifecycleOwner) { reviews ->
            if (reviews != null && reviews.isNotEmpty()) {
                reviewsList.clear()
                reviewsList.addAll(reviews)
                reviewAdapter.notifyDataSetChanged()
            }
        }

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
            sourceFragment = "MyReviewsFragment"  // אנחנו מגדירים כאן שזה בא מפרגמנט הביקורות שלי
        )

        recyclerView.adapter = reviewAdapter

        fetchUserReviews()  // טוען ביקורות משתמש

        return view
    }

    private fun fetchUserReviews() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("reviews")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val newList = mutableListOf<Review>()
                for (document in documents) {
                    val review = document.toObject(Review::class.java)?.copy(id = document.id)
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
