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

        // אתחול Cloudinary עם BuildConfig כדי להשתמש בפרטי הגדרות מ-gradle.properties
        val config = hashMapOf(
            "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
            "api_key" to BuildConfig.CLOUDINARY_API_KEY,
            "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
        )
        MediaManager.init(this, config)

        // הגדרת הניווט
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        if (navHostFragment != null) {
            navController = navHostFragment.navController
        } else {
            return // אם ה-NavHostFragment לא נטען כראוי, לא להמשיך
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // ✅ בדיקה אם המשתמש מחובר ל-Firebase, אם לא → ניווט למסך התחברות
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            navController.navigate(R.id.loginFragment) // ✅ שולח את המשתמש לעמוד ההתחברות
        }

        // מסתיר או מציג את סרגל הכלים התחתון בהתאם ל-Fragment הנוכחי
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNavigationView.visibility = if (
                destination.id == R.id.welcomeFragment ||
                destination.id == R.id.loginFragment ||
                destination.id == R.id.signupFragment
            ) View.GONE else View.VISIBLE
        }

        // פתרון מרכזי לבעיה: שימוש נכון בניווט אוטומטי (setupWithNavController)
        bottomNavigationView.setupWithNavController(navController)
    }
}








