package com.idz.bookreview.ui

import android.os.Bundle
import android.view.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idz.bookreview.R
import com.idz.bookreview.adapter.ReviewAdapter
import com.idz.bookreview.databinding.FragmentHomeBinding
import com.idz.bookreview.viewmodel.HomeViewModel
import com.idz.bookreview.viewmodel.HomeViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels { HomeViewModelFactory(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)  // שמירה על טעינת התפריט העליון (שלוש נקודות)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = ReviewAdapter(
            requireContext(),
            mutableListOf(),
            { review -> },
            { reviewId -> },
            { review ->
                viewModel.updateReviewLikeStatus(review)
            },
            "HomeFragment"
        )

        recyclerView.adapter = adapter

        // נטען את כל הביקורות מ-Firestore ומ-ROOM
        viewModel.reloadAllReviews()

        // הוספת observer ל-LiveData
        viewModel.reviewsLiveData.observe(viewLifecycleOwner) { reviews ->
            if (reviews.isNotEmpty()) {
                adapter.updateReviews(reviews) // עדכון התצוגה עם המידע החדש
            } else {
                Toast.makeText(requireContext(), "No reviews found. Please add a review.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
