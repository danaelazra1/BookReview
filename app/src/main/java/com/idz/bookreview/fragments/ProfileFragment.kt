package com.idz.bookreview.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.bookreview.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import com.idz.bookreview.model.AppDatabase


class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: AppDatabase
    private lateinit var emailTextView: TextView
    private lateinit var usernameTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = AppDatabase.getDatabase(requireContext())

        emailTextView = view.findViewById(R.id.userEmailTextView)
        usernameTextView = view.findViewById(R.id.usernameEditText)
        val myReviewsButton: Button = view.findViewById(R.id.myReviewsButton)
        val favoritesButton: Button = view.findViewById(R.id.favoritesButton)

        loadUserData()

        myReviewsButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_myReviewsFragment)
        }

        favoritesButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_favoritesFragment)
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            // טעינת הנתונים מבסיס הנתונים המקומי (Room)
            lifecycleScope.launch(Dispatchers.IO) {
                val user = database.userDao().getUserById(userId)
                requireActivity().runOnUiThread {
                    if (user != null) {
                        emailTextView.text = user.email
                        usernameTextView.text = user.username
                    } else {
                        loadUserDataFromFirebase(userId)
                    }
                }
            }
        }
    }

    private fun loadUserDataFromFirebase(userId: String) {
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: "No username"
                    val email = document.getString("email") ?: "No email"

                    emailTextView.text = email
                    usernameTextView.text = username
                    Log.d("ProfileFragment", "Loaded from Firestore: $username, $email")
                } else {
                    emailTextView.text = "User not found"
                    usernameTextView.text = "User not found"
                }
            }
            .addOnFailureListener {
                emailTextView.text = "Failed to load"
                usernameTextView.text = "Failed to load"
                Log.e("ProfileFragment", "User document not found")
            }
    }

}




