package com.idz.bookreview

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var isHomeFragment = false  // כדי לדעת אם אנחנו בעמוד הבית

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        val db = Firebase.firestore

        // הגדרת צבע הסטטוס בר לחום בהיר שקפקף
        window.statusBarColor = ContextCompat.getColor(this, R.color.transparent_brown)

        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            isHomeFragment = destination.id == R.id.homeFragment

            // מציג את ה-Tollbar רק בדף הבית
            if (isHomeFragment) {
                toolbar.visibility = View.VISIBLE
            } else {
                toolbar.visibility = View.GONE
            }

            invalidateOptionsMenu()

            bottomNavigationView.visibility = when (destination.id) {
                R.id.welcomeFragment, R.id.loginFragment, R.id.signupFragment -> View.GONE
                else -> View.VISIBLE
            }
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    if (!isHomeFragment) {
                        navController.navigate(R.id.homeFragment)
                    }
                    true
                }
                R.id.addReviewFragment -> {
                    navController.navigate(R.id.addReviewFragment)
                    true
                }
                R.id.searchFragment -> {
                    navController.navigate(R.id.searchFragment)
                    true
                }
                R.id.profileFragment -> {
                    navController.navigate(R.id.profileFragment)
                    true
                }
                else -> false
            }
        }

        val user = auth.currentUser
        if (user == null) {
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
                        .addOnSuccessListener { Log.d("TAG", "User profile created successfully!") }
                        .addOnFailureListener { e -> Log.w("TAG", "Error creating user profile", e) }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        if (isHomeFragment) {
            menuInflater.inflate(R.menu.home_menu, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        return when (item.itemId) {
            R.id.menu_my_reviews -> {
                navController.navigate(R.id.myReviewsFragment)
                true
            }
            R.id.menu_liked_reviews -> {
                navController.navigate(R.id.likedReviewsFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
