package com.idz.bookreview

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()

        val userEmailTextView = view.findViewById<TextView>(R.id.userEmailTextView)
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)

        val user = auth.currentUser
        userEmailTextView.text = user?.email ?: "Guest"

        logoutButton.setOnClickListener {
            auth.signOut()
            requireActivity().finish()
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }

        return view
    }
}
