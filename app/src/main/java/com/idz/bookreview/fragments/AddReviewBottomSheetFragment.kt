package com.idz.bookreview.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.idz.bookreview.R

class AddReviewBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_review_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etBookName = view.findViewById<EditText>(R.id.etBookName)
        val etBookDescription = view.findViewById<EditText>(R.id.etBookDescription)
        val etBookReview = view.findViewById<EditText>(R.id.etBookReview)
        val btnSubmitReview = view.findViewById<Button>(R.id.btn_submit_review)

        val db = Firebase.firestore

        btnSubmitReview.setOnClickListener {
            val bookName = etBookName.text.toString().trim()
            val bookDescription = etBookDescription.text.toString().trim()
            val bookReview = etBookReview.text.toString().trim()

            if (bookName.isNotEmpty() && bookDescription.isNotEmpty() && bookReview.isNotEmpty()) {
                val reviewData = hashMapOf(
                    "bookName" to bookName,
                    "bookDescription" to bookDescription,
                    "bookReview" to bookReview,
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("reviews")
                    .add(reviewData)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Review added successfully!", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to add review: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


