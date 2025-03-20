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
        val db = Firebase.firestore

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        val user = auth.currentUser

        // הסתרת הניווט התחתון במסכי הכניסה
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNavigationView.visibility = if (destination.id == R.id.welcomeFragment ||
                destination.id == R.id.loginFragment ||
                destination.id == R.id.signupFragment) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        // ביצוע הניווט למסך המתאים רק אחרי שהניווט מוכן
        if (user == null) {
            // אם המשתמש לא מחובר, נוודא שהוא מועבר ל-WelcomeFragment
            navController.navigate(R.id.welcomeFragment)
        } else {
            val userId = user.uid
            val userRef = db.collection("users").document(userId)

            userRef.get().addOnSuccessListener { document ->
                if (!document.exists()) {
                    val userData = hashMapOf(
                        "id" to userId,
                        "email" to user.email,
                        "username" to "",
                        "profileImageUrl" to ""
                    )
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
}
