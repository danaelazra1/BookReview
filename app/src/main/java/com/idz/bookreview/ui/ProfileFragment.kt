package com.idz.bookreview.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.bookreview.MainActivity
import com.idz.bookreview.R
import com.idz.bookreview.model.User
import com.idz.bookreview.model.dao.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var appDatabase: AppDatabase
    private lateinit var userEmailTextView: TextView
    private lateinit var usernameEditText: EditText
    private lateinit var saveUsernameButton: Button
    private lateinit var logoutButton: Button
    private lateinit var deleteAccountButton: Button
    private lateinit var profileImageView: ImageView

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            profileImageView.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater, container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        appDatabase = AppDatabase.getDatabase(requireContext())

        profileImageView = view.findViewById(R.id.profileImageView)
        userEmailTextView = view.findViewById(R.id.userEmailTextView)
        usernameEditText = view.findViewById(R.id.usernameEditText)
        saveUsernameButton = view.findViewById(R.id.saveUsernameButton)
        logoutButton = view.findViewById(R.id.logoutButton)
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton)

        val user = auth.currentUser
        userEmailTextView.text = user?.email ?: "Guest"

        user?.let { loadUserData(it.uid) }

        saveUsernameButton.setOnClickListener {
            user?.uid?.let { userId -> updateUsername(userId) }
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            requireActivity().finish()
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }

        deleteAccountButton.setOnClickListener {
            showDeleteAccountDialog()
        }

        profileImageView.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        return view
    }

    private fun loadUserData(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: ""
                    usernameEditText.setText(username)
                }
            }
    }

    private fun updateUsername(userId: String) {
        val newUsername = usernameEditText.text.toString().trim()
        if (newUsername.isEmpty()) {
            Toast.makeText(requireContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val user = appDatabase.userDao().getUserById(userId)
                println("Checking Room: Retrieved user = $user")

                if (user != null) {
                    user.username = newUsername
                    appDatabase.userDao().updateUser(user)
                    println("Updated user in Room: $user")
                } else {
                    println("User not found in Room. Inserting new user...")
                    appDatabase.userDao().insertUser(User(id = userId, username = newUsername, email = auth.currentUser?.email ?: ""))
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Username updated!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteAccount() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount() {
        val user = auth.currentUser ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userInRoom = appDatabase.userDao().getUserById(user.uid)
                println("Checking Room before deletion: $userInRoom")

                if (userInRoom != null) {
                    appDatabase.userDao().deleteUserById(user.uid)
                    println("User deleted from Room: ${user.uid}")
                } else {
                    println("User not found in Room. Skipping Room deletion.")
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "User deleted from database", Toast.LENGTH_SHORT).show()
                }

                user.delete().addOnCompleteListener { deleteTask ->
                    if (deleteTask.isSuccessful) {
                        Toast.makeText(requireContext(), "User deleted successfully", Toast.LENGTH_SHORT).show()
                        requireActivity().finish()
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                    } else {
                        Toast.makeText(requireContext(), "Error: ${deleteTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error deleting user: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
