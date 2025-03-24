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
import com.idz.bookreview.viewmodel.ReviewViewModel

class LikedReviewsFragment : Fragment() {

    private lateinit var reviewViewModel: ReviewViewModel
    private lateinit var recyclerView: RecyclerView
    private val likedReviewsList = mutableListOf<Review>()
    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_liked_reviews, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        reviewViewModel = ViewModelProvider(this)[ReviewViewModel::class.java]

        reviewAdapter = ReviewAdapter(
            requireContext(),
            likedReviewsList,
            onEditClick = {},   // ביקורות לייק לא ניתן לערוך
            onDeleteClick = {},  // ביקורות לייק לא ניתן למחוק
            onLikeClick = { review ->
                review.isLiked = false
                reviewViewModel.updateReviewLikeStatus(review)
                removeReviewFromList(review)
            },
            sourceFragment = "LikedReviewsFragment"
        )

        recyclerView.adapter = reviewAdapter

        reviewViewModel.reviewsLiveData.observe(viewLifecycleOwner) { reviews ->
            val likedReviews = reviews.filter { it.isLiked }
            likedReviewsList.clear()
            likedReviewsList.addAll(likedReviews)
            reviewAdapter.notifyDataSetChanged()
        }

        reviewViewModel.reloadAllReviews()

        return view
    }

    private fun removeReviewFromList(review: Review) {
        likedReviewsList.remove(review)
        reviewAdapter.notifyDataSetChanged()
    }
}
