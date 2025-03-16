package com.idz.bookreview.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.bookreview.R
import com.idz.bookreview.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.idz.bookreview.model.AppDatabase

class SignupFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_signup, container, false)

        auth = FirebaseAuth.getInstance()
        database = AppDatabase.getDatabase(requireContext())

        val emailEditText = view.findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = view.findViewById<EditText>(R.id.confirmPasswordEditText)
        val usernameEditText = view.findViewById<EditText>(R.id.usernameEditText)
        val signupButton = view.findViewById<Button>(R.id.signupButton)
        val loginTextView = view.findViewById<TextView>(R.id.tvAlreadyHaveAccount)

        loginTextView.setOnClickListener {
            hideKeyboard(requireView())
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }

        signupButton.setOnClickListener {
            hideKeyboard(requireView())
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        firebaseUser?.let { user ->
                            val userId = user.uid
                            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

                            val newUser = hashMapOf(
                                "id" to userId,
                                "username" to username,
                                "email" to email,
                                "profileImageUrl" to ""
                            )

                            userRef.set(newUser)
                                .addOnSuccessListener {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        database.userDao().insertUser(
                                            User(id = userId, username = username, email = email)
                                        )
                                    }
                                    Toast.makeText(requireContext(), "Signup successful!", Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Failed to save user data", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        emailEditText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) hideKeyboard(v)
        }
        passwordEditText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) hideKeyboard(v)
        }
        confirmPasswordEditText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) hideKeyboard(v)
        }
        usernameEditText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) hideKeyboard(v)
        }

        return view
    }

    private fun hideKeyboard(view: View) {
        try {
            val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
