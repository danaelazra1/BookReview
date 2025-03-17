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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.bookreview.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

class SignupFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val emailEditText = view.findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = view.findViewById<EditText>(R.id.confirmPasswordEditText)
        val usernameEditText = view.findViewById<EditText>(R.id.usernameEditText)
        val signupButton = view.findViewById<Button>(R.id.signupButton)
        val loginTextView = view.findViewById<TextView>(R.id.tvAlreadyHaveAccount)

        loginTextView.setOnClickListener {
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }

        signupButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
                Toast.makeText(requireContext(), "נא למלא את כל השדות", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "הסיסמאות לא תואמות", Toast.LENGTH_SHORT).show()
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
                                    Log.d("SignupFragment", "✅ User saved successfully in Firestore: $userId")
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("SignupFragment", " Failed to save user in Firestore", exception)
                                }
                        }
                    } else {
                        Log.e("SignupFragment", " Signup failed: ${task.exception?.message}")
                    }
                }

        }
    }
}
