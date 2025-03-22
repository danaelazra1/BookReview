package com.idz.bookreview.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idz.bookreview.R
import com.idz.bookreview.adapter.ReviewAdapter
import com.idz.bookreview.model.dao.AppDatabase
import com.idz.bookreview.viewmodel.ReviewViewModel
import com.idz.bookreview.viewmodel.ReviewViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.idz.bookreview.model.networking.FirebaseService // ודא שהייבוא קיים
import androidx.fragment.app.activityViewModels

class MyReviewsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewAdapter

    private val reviewViewModel: ReviewViewModel by activityViewModels<ReviewViewModel> {
        ReviewViewModelFactory(
            AppDatabase.getDatabase(requireContext()).reviewDao(),
            FirebaseService()
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_my_reviews, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewMyReviews)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ReviewAdapter(emptyList()) { review ->
            reviewViewModel.toggleFavorite(review) //  פונקציה שמוסיפה/מסירה ממועדפים
        }
        recyclerView.adapter = adapter

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "נא להתחבר כדי לראות את הביקורות שלך", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.loginFragment)
            return
        }

        val btnBackToProfile: ImageButton = view.findViewById(R.id.btnBackToProfile)
        btnBackToProfile.setOnClickListener {
            findNavController().navigate(R.id.action_myReviewsFragment_to_profileFragment)
        }

        val userId = user.uid
        reviewViewModel.getReviewsByUser(userId).observe(viewLifecycleOwner) { reviews ->
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