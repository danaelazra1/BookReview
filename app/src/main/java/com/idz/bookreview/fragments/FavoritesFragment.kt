package com.idz.bookreview.fragments

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
import android.widget.ImageButton
import androidx.navigation.fragment.findNavController
import com.idz.bookreview.model.networking.FirebaseService

class FavoritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewAdapter

    private val reviewViewModel: ReviewViewModel by activityViewModels {
        ReviewViewModelFactory(
            AppDatabase.getDatabase(requireContext()).reviewDao(),
            FirebaseService() // ✅ עכשיו הוא מועבר כמו שצריך
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewFavorites)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // ✅ הוספת כפתור חזרה לפרופיל
        val btnBackToProfile: ImageButton = view.findViewById(R.id.btnBackToProfile)
        btnBackToProfile.setOnClickListener {
            findNavController().navigate(R.id.action_favoritesFragment_to_profileFragment)
        }

        // ✅ הגדרת ה-Adapter עם פונקציה להוספה/הסרה ממועדפים
        adapter = ReviewAdapter(emptyList()) { review ->
            reviewViewModel.toggleFavorite(review)
        }
        recyclerView.adapter = adapter

        // ✅ טעינת הביקורות המועדפות
        reviewViewModel.favoriteReviews.observe(viewLifecycleOwner) { reviews ->
            val updatedReviews = reviews.map { review ->
                review.copy(
                    bookDescription = review.bookDescription ?: "תיאור לא זמין",
                    imageUrl = review.imageUrl ?: "https://example.com/default-image.jpg"
                )
            }
            if (updatedReviews.isEmpty()) {
                Toast.makeText(requireContext(), "אין ביקורות מועדפות", Toast.LENGTH_SHORT).show()
            }
            adapter.updateReviews(updatedReviews)
        }
    }
}




