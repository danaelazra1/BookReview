package com.idz.bookreview.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.idz.bookreview.R

class WelcomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_welcome, container, false)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            findNavController().navigate(R.id.action_welcomeFragment_to_homeFragment)
            return view
        }


        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val signupButton = view.findViewById<Button>(R.id.signupButton)

        loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeFragment_to_loginFragment)
        }

        signupButton.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeFragment_to_signupFragment)
        }

        return view
    }
}
