package com.idz.bookreview.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.idz.bookreview.R

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val myReviewsButton: Button = view.findViewById(R.id.myReviewsButton)
        val favoritesButton: Button = view.findViewById(R.id.favoritesButton) // ✅ כפתור חדש

        myReviewsButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_myReviewsFragment)
        }

        favoritesButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_favoritesFragment) // ✅ ניווט תקין
        }
    }
}



