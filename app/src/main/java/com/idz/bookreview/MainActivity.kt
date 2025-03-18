package com.idz.bookreview

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.cloudinary.android.MediaManager
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.NavController

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val config = hashMapOf(
            "df8odu4s4" to BuildConfig.CLOUDINARY_CLOUD_NAME,
            "336195937131691" to BuildConfig.CLOUDINARY_API_KEY,
            "SaBVAU7-nDyK21XJulN76xExl84" to BuildConfig.CLOUDINARY_API_SECRET
        )
        MediaManager.init(this, config)

        // הגדרת הניווט
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        if (navHostFragment != null) {
            navController = navHostFragment.navController
        } else {
            return
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null && savedInstanceState == null) {
            navController.navigate(R.id.loginFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNavigationView.visibility = if (
                destination.id == R.id.welcomeFragment ||
                destination.id == R.id.loginFragment ||
                destination.id == R.id.signupFragment
            ) View.GONE else View.VISIBLE
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment)
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
                R.id.addReviewFragment -> {
                    navController.popBackStack()
                    navController.navigate(R.id.addReviewFragment)
                    true
                }
                else -> false
            }
        }
        bottomNavigationView.setupWithNavController(navController)
    }
}










