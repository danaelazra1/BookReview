package com.idz.bookreview.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idz.bookreview.R
import com.idz.bookreview.adapter.ReviewAdapter
import com.idz.bookreview.model.AppDatabase
import com.idz.bookreview.viewmodel.ReviewViewModel
import com.idz.bookreview.viewmodel.ReviewViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import android.widget.ImageButton

class MyReviewsFragment : Fragment() {

    private lateinit var reviewViewModel: ReviewViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_my_reviews, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reviewDao = AppDatabase.getDatabase(requireContext()).reviewDao()
        reviewViewModel = ViewModelProvider(this, ReviewViewModelFactory(reviewDao))
            .get(ReviewViewModel::class.java)

        recyclerView = view.findViewById(R.id.recyclerViewMyReviews) // 🔹 בדוק שה-ID נכון
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReviewAdapter(emptyList()) // יצירת אדפטר עם רשימה ריקה
        recyclerView.adapter = adapter

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "נא להתחבר כדי לראות את הביקורות שלך", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.loginFragment)
            return
        }

        // ✅ כפתור חזרה לפרופיל
        val btnBackToProfile: ImageButton = view.findViewById(R.id.btnBackToProfile)
        btnBackToProfile.setOnClickListener {
            findNavController().navigate(R.id.action_myReviewsFragment_to_profileFragment)
        }

        val userId = user.uid
        reviewViewModel.getReviewsByUser(userId).observe(viewLifecycleOwner) { reviews ->
            reviews?.let {
                adapter.updateReviews(it)
            }
        }
    }
}





