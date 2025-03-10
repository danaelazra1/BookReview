package com.idz.bookreview

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        if (auth.currentUser == null) {
            navController.navigate(R.id.welcomeFragment)

        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNavigationView.visibility = if (destination.id == R.id.welcomeFragment ||
                destination.id == R.id.loginFragment ||
                destination.id == R.id.signupFragment) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
        val db = Firebase.firestore
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val userRef = db.collection("users").document(userId)


        val userData = hashMapOf(
            "first" to "Ada",
            "last" to "Lovelace",
            "born" to 1815
        )

        userRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                userRef.set(userData)
                    .addOnSuccessListener {
                        Log.d("TAG", "User profile created successfully!")
                    }
                    .addOnFailureListener { e ->
                        Log.w("TAG", "Error creating user profile", e)
                    }
            }
        }


    }
}
