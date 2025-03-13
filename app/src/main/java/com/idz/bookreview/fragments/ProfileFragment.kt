package com.idz.bookreview.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.bookreview.R

class ProfileFragment : Fragment() {

    private lateinit var emailTextView: TextView
    private lateinit var usernameTextView: TextView
    private lateinit var usernameEditText: EditText
    private lateinit var saveUsernameButton: Button
    private lateinit var myReviewsButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailTextView = view.findViewById(R.id.userEmailTextView )
        usernameTextView = view.findViewById(R.id.usernameEditText )
        usernameEditText = view.findViewById(R.id.usernameEditText )
        saveUsernameButton = view.findViewById(R.id.saveUsernameButton)
        myReviewsButton = view.findViewById(R.id.myReviewsButton)

        loadUserData()

        saveUsernameButton.setOnClickListener {
            saveUsername()
        }

        myReviewsButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_myReviewsFragment)
        }
    }

    private fun loadUserData() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userEmail = user.email ?: "No Email"
            val userId = user.uid

            emailTextView.text = userEmail // ✅ מציג את האימייל

            val db = FirebaseFirestore.getInstance()
            val usersRef = db.collection("users").document(userId)

            usersRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: "No Name"
                    usernameTextView.text = username // ✅ מציג את שם המשתמש
                } else {
                    usernameTextView.text = "No Name" // ✅ ברירת מחדל אם אין שם
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "שגיאה בטעינת הנתונים", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "אין משתמש מחובר", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUsername() {
        val user = FirebaseAuth.getInstance().currentUser
        val newUsername = usernameEditText.text.toString().trim()

        if (user != null && newUsername.isNotEmpty()) {
            val userId = user.uid
            val db = FirebaseFirestore.getInstance()

            val userData = hashMapOf("username" to newUsername)

            db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "שם המשתמש נשמר בהצלחה!", Toast.LENGTH_SHORT).show()
                    usernameTextView.text = newUsername // ✅ מעדכן את התצוגה מיד
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "שגיאה בשמירת שם המשתמש", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "נא להכניס שם משתמש תקין", Toast.LENGTH_SHORT).show()
        }
    }
}

