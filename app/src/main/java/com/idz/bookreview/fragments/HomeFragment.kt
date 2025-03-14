package com.idz.bookreview.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idz.bookreview.R
import com.idz.bookreview.adapter.ReviewAdapter
import com.idz.bookreview.model.AppDatabase
import com.idz.bookreview.viewmodel.ReviewViewModel
import com.idz.bookreview.viewmodel.ReviewViewModelFactory
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var reviewViewModel: ReviewViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewReviews)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // חיבור ה-Adapter לפני טעינת הנתונים
        adapter = ReviewAdapter(emptyList())
        recyclerView.adapter = adapter
        Log.d("HomeFragment", "Adapter connected in onCreateView()")

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reviewDao = AppDatabase.getDatabase(requireContext()).reviewDao()
        reviewViewModel = ViewModelProvider(this, ReviewViewModelFactory(reviewDao))
            .get(ReviewViewModel::class.java)

        Log.d("HomeFragment", "onViewCreated triggered - Fetching all reviews.")
        fetchAllReviews()
    }

    private fun fetchAllReviews() {
        Log.d("Firestore", "Fetching reviews from database...")

        reviewViewModel.allReviews.observe(viewLifecycleOwner) { reviews ->
            if (reviews == null) {
                Log.e("Firestore", "reviews is NULL")
            } else if (reviews.isEmpty()) {
                Log.d("Firestore", "No reviews found in database.")
                Toast.makeText(requireContext(), "אין ביקורות זמינות", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("Firestore", "Fetched ${reviews.size} reviews")
                adapter.updateReviews(reviews)
            }
        }
    }
}





