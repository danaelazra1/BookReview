package com.idz.bookreview.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idz.bookreview.R
import com.idz.bookreview.adapter.ReviewAdapter
import com.idz.bookreview.model.AppDatabase
import com.idz.bookreview.viewmodel.ReviewViewModel
import com.idz.bookreview.viewmodel.ReviewViewModelFactory
import com.idz.bookreview.model.networking.FirebaseService

class HomeFragment : Fragment() {
    private val reviewViewModel: ReviewViewModel by activityViewModels {
        ReviewViewModelFactory(
            AppDatabase.getDatabase(requireContext()).reviewDao(),
            FirebaseService()
        )
    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewReviews)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ReviewAdapter(emptyList()) { review ->
            reviewViewModel.toggleFavorite(review)
            Toast.makeText(requireContext(), "עודכן במועדפים!", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

        reviewViewModel.allReviews.observe(viewLifecycleOwner) { reviews ->
            val updatedReviews = reviews.map { review ->
                review.copy(
                    bookDescription = review.bookDescription ?: "תיאור לא זמין",
                    imageUrl = review.imageUrl ?: "https://example.com/default-image.jpg"
                )
            }
            adapter.updateReviews(updatedReviews)
        }
    }
}





