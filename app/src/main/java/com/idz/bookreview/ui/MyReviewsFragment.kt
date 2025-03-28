package com.idz.bookreview.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.bookreview.R
import com.idz.bookreview.adapter.ReviewAdapter
import com.idz.bookreview.model.Review
import com.idz.bookreview.viewmodel.HomeViewModel
import com.idz.bookreview.viewmodel.HomeViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyReviewsFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
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

        homeViewModel = ViewModelProvider(this, HomeViewModelFactory(requireContext()))[HomeViewModel::class.java]

        reviewAdapter = ReviewAdapter(
            requireContext(),
            reviewsList,
            onEditClick = { review ->
                Toast.makeText(requireContext(), "Edit review clicked for ${review.title}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { reviewId ->
                lifecycleScope.launch {
                    homeViewModel.deleteReviewById(reviewId)
                }
            },

            onLikeClick = { review ->
                homeViewModel.updateReviewLikeStatus(review)
            },
            sourceFragment = "MyReviewsFragment"
        )

        recyclerView.adapter = reviewAdapter

        homeViewModel.reviewsLiveData.observe(viewLifecycleOwner) { reviews ->
            reviewsList.clear()

            if (reviews != null && reviews.isNotEmpty()) {
                val userReviews = reviews.filter { it.userId == userId }
                reviewsList.addAll(userReviews)
            }

            reviewAdapter.notifyDataSetChanged()
        }


        homeViewModel.reloadAllReviews()

        return view
    }
}
