package com.idz.bookreview.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idz.bookreview.R
import com.idz.bookreview.adapter.ReviewAdapter
import com.idz.bookreview.model.Review
import com.idz.bookreview.viewmodel.HomeViewModel
import com.idz.bookreview.viewmodel.HomeViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class LikedReviewsFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var recyclerView: RecyclerView
    private val likedReviewsList = mutableListOf<Review>()
    private lateinit var reviewAdapter: ReviewAdapter
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_liked_reviews, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        homeViewModel = ViewModelProvider(this, HomeViewModelFactory(requireContext()))[HomeViewModel::class.java]

        reviewAdapter = ReviewAdapter(
            requireContext(),
            likedReviewsList,
            onEditClick = {},
            onDeleteClick = {},
            onLikeClick = { review ->
                currentUserId?.let { userId ->
                    val updatedLikes = review.favoritedByUsers.toMutableList()
                    updatedLikes.remove(userId)
                    val updatedReview = review.copy(favoritedByUsers = updatedLikes)

                    homeViewModel.updateReviewLikeStatus(updatedReview)
                    removeReviewFromList(updatedReview)
                }
            },
            sourceFragment = "LikedReviewsFragment"
        )

        recyclerView.adapter = reviewAdapter

        homeViewModel.reviewsLiveData.observe(viewLifecycleOwner) { reviews ->
            currentUserId?.let { userId ->
                val likedReviews = reviews.filter { it.favoritedByUsers.contains(userId) }
                likedReviewsList.clear()
                likedReviewsList.addAll(likedReviews)
                reviewAdapter.notifyDataSetChanged()
            }
        }

        homeViewModel.reloadAllReviews()

        return view
    }

    private fun removeReviewFromList(review: Review) {
        likedReviewsList.remove(review)
        reviewAdapter.notifyDataSetChanged()
    }
}
