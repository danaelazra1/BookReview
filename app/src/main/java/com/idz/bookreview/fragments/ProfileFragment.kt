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
    private lateinit var logoutButton: Button

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
        logoutButton = view.findViewById(R.id.logoutButton)
        val myReviewsButton: Button = view.findViewById(R.id.myReviewsButton)
        val favoritesButton: Button = view.findViewById(R.id.favoritesButton)
        val deleteAccountButton: Button = view.findViewById(R.id.deleteAccountButton)

        loadUserData()

        myReviewsButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_myReviewsFragment)
        }

        favoritesButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_favoritesFragment)
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.welcomeFragment) // מעביר את המשתמש למסך Welcome
        }

        deleteAccountButton.setOnClickListener {
            deleteUserAccount()
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

    private fun deleteUserAccount() {
        val user = auth.currentUser

        user?.let {
            val userId = it.uid

            // מחיקת המשתמש ממסד הנתונים המקומי (Room)
            lifecycleScope.launch(Dispatchers.IO) {
                database.userDao().deleteUserById(userId)
            }

            // מחיקת הנתונים מ-Firestore
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .delete()
                .addOnSuccessListener {
                    Log.d("ProfileFragment", "User data deleted from Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileFragment", "Error deleting user data from Firestore", e)
                }

            // מחיקת החשבון מ-Firebase Authentication
            user.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("ProfileFragment", "User account deleted successfully")
                        findNavController().navigate(R.id.welcomeFragment) // חזרה למסך Welcome
                    } else {
                        Log.e("ProfileFragment", "Error deleting user account", task.exception)
                    }
                }
        }
    }


}




