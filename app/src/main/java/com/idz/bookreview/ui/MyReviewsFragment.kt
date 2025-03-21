package com.idz.bookreview.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.bookreview.R
import com.idz.bookreview.adapter.ReviewAdapter
import com.idz.bookreview.model.Review

class MyReviewsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val reviewsList = mutableListOf<Review>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_reviews, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        fetchUserReviews()
        return view
    }

    private fun fetchUserReviews() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("reviews")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                reviewsList.clear()
                for (document in documents) {
                    val review = document.toObject(Review::class.java)?.copy(id = document.id)
                    if (review != null) reviewsList.add(review)
                }
                recyclerView.adapter = ReviewAdapter(reviewsList) { review ->
                    Toast.makeText(requireContext(), "Edit review clicked for ${review.title}", Toast.LENGTH_SHORT).show()
                }

            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load reviews", Toast.LENGTH_SHORT).show()
            }
    }
}
