package com.idz.bookreview.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idz.bookreview.R
import com.idz.bookreview.adapter.ReviewAdapter
import com.idz.bookreview.model.Review
import com.idz.bookreview.model.dao.AppDatabase
import com.idz.bookreview.model.networking.FirebaseService
import com.idz.bookreview.viewmodel.ReviewViewModel
import com.idz.bookreview.viewmodel.ReviewViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class MyReviewsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewAdapter

    private val reviewViewModel: ReviewViewModel by activityViewModels {
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

        adapter = ReviewAdapter(
            reviews = emptyList(),
            onFavoriteClick = { review -> reviewViewModel.toggleFavorite(review) },
            onEditClick = { review -> showEditDialog(review) },
            onDeleteClick = { review -> reviewViewModel.deleteReview(review) },
            showEditOptions = true
        )

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

    private fun showEditDialog(review: Review) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_review, null)
        val titleEdit = dialogView.findViewById<EditText>(R.id.editBookTitle)
        val descEdit = dialogView.findViewById<EditText>(R.id.editBookDescription)
        val reviewEdit = dialogView.findViewById<EditText>(R.id.editReviewText)

        // קביעת הטקסטים הקיימים
        titleEdit.setText(review.bookTitle)
        descEdit.setText(review.bookDescription)
        reviewEdit.setText(review.reviewText)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Review")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedReview = review.copy(
                    bookTitle = titleEdit.text.toString().trim(),
                    bookDescription = descEdit.text.toString().trim(),
                    reviewText = reviewEdit.text.toString().trim()
                )
                reviewViewModel.updateReview(updatedReview)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}