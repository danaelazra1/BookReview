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
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.bookreview.R
import com.idz.bookreview.adapter.ReviewAdapter
import com.idz.bookreview.databinding.FragmentHomeBinding
import com.idz.bookreview.viewmodel.HomeViewModel
import com.idz.bookreview.viewmodel.HomeViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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

        val adapter = ReviewAdapter(requireContext(), mutableListOf(), { review ->
            // לא נוסיף פה שום דבר, כי לא ניתן לערוך מדף הבית
        }, { reviewId ->
            // לא נוסיף פה שום דבר, כי לא ניתן למחוק מדף הבית
        }, "HomeFragment")
        recyclerView.adapter = adapter


        lifecycleScope.launch {
            viewModel.getReviews().collect { reviews ->
                if (reviews.isNotEmpty()) {
                    applyEntranceAnimation(recyclerView)
                    adapter.updateReviews(reviews)
                } else {
                    Toast.makeText(requireContext(), "אין חיבור לאינטרנט. הצגת נתונים מקומית", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun applyEntranceAnimation(recyclerView: RecyclerView) {
        recyclerView.alpha = 0f
        recyclerView.translationY = 200f

        recyclerView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .start()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
