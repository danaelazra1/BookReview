package com.idz.bookreview.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.bookreview.MainActivity
import com.idz.bookreview.R
import com.idz.bookreview.model.dao.AppDatabase
import com.idz.bookreview.api.CloudinaryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import android.graphics.Bitmap
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide

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
    private lateinit var cameraIcon: ImageView
    private lateinit var deleteImageButton: Button



    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            profileImageView.setImageURI(it)
            uploadImageToCloudinary(it)
        }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            profileImageView.setImageBitmap(it)
            uploadBitmapToCloudinary(it)
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
        cameraIcon = view.findViewById(R.id.cameraIcon)
        deleteImageButton = view.findViewById(R.id.deleteImageButton)

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

        cameraIcon.setOnClickListener {
            showImagePickerDialog()
        }

        deleteImageButton.setOnClickListener {
            removeProfileImage()
        }

        return view
    }

    private fun loadUserData(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: ""
                    usernameEditText.setText(username)

                    val imageUrl = document.getString("profileImageUrl")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(imageUrl).into(profileImageView)
                        cameraIcon.visibility = View.GONE
                        deleteImageButton.visibility = View.VISIBLE
                    } else {
                        profileImageView.setImageResource(R.drawable.ic_profile)
                        cameraIcon.visibility = View.VISIBLE
                        deleteImageButton.visibility = View.GONE
                    }
                }
            }
    }

    private fun removeProfileImage() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)

        userRef.update("profileImageUrl", "")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile image removed", Toast.LENGTH_SHORT).show()
                profileImageView.setImageResource(R.drawable.ic_profile)
                cameraIcon.visibility = View.VISIBLE
                deleteImageButton.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error removing image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Choose from Gallery", "Take a Photo")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickImageLauncher.launch("image/*")
                    1 -> takePhotoLauncher.launch(null)
                }
            }
            .show()
    }

    private fun uploadImageToCloudinary(imageUri: Uri) {
        val contentResolver = requireContext().contentResolver
        val inputStream = contentResolver.openInputStream(imageUri)
        val requestBody = inputStream?.readBytes()?.let { RequestBody.create("image/*".toMediaType(), it) }

        requestBody?.let {
            val multipartBody = MultipartBody.Part.createFormData(
                "file",
                "profile_picture.jpg",
                it
            )

            lifecycleScope.launch {
                try {
                    val response = CloudinaryService.api.uploadImage(multipartBody)
                    val imageUrl = response.secureUrl
                    saveImageUrlToFirestore(imageUrl)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error uploading image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadBitmapToCloudinary(bitmap: Bitmap) {
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()
        val requestBody = RequestBody.create("image/jpeg".toMediaType(), byteArray)
        val multipartBody = MultipartBody.Part.createFormData("file", "profile_picture.jpg", requestBody)

        lifecycleScope.launch {
            try {
                val response = CloudinaryService.api.uploadImage(multipartBody)
                val imageUrl = response.secureUrl
                saveImageUrlToFirestore(imageUrl)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error uploading image", Toast.LENGTH_SHORT).show()
            }
        }
    }



private fun saveImageUrlToFirestore(imageUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)

        userRef.update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                Glide.with(this).load(imageUrl).into(profileImageView)
                cameraIcon.visibility = View.GONE
                deleteImageButton.visibility = View.VISIBLE
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error saving image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUsername(userId: String) {
        val newUsername = usernameEditText.text.toString().trim()

        if (newUsername.isBlank()) {
            Toast.makeText(requireContext(), "Username cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = db.collection("users").document(userId)
        userRef.update("username", newUsername)
            .addOnSuccessListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    val user = appDatabase.userDao().getUserById(userId)

                    if (user != null) {
                        user.username = newUsername
                        appDatabase.userDao().updateUser(user)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Username updated!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "User not found in Room!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error updating username in Firestore", Toast.LENGTH_SHORT).show()
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
        val userId = user.uid
        val userRef = db.collection("users").document(userId)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                userRef.update("profileImageUrl", "")
                    .addOnSuccessListener { println("Profile image removed") }
                    .addOnFailureListener { println("Failed to remove profile image: ${it.message}") }

                userRef.delete()
                    .addOnSuccessListener {
                        println("User deleted from Firestore: $userId")

                        lifecycleScope.launch(Dispatchers.IO) {
                            val userInRoom = appDatabase.userDao().getUserById(userId)
                            if (userInRoom != null) {
                                appDatabase.userDao().deleteUserById(userId)
                                println("User deleted from Room: $userId")
                            } else {
                                println("User not found in Room. Skipping Room deletion.")
                            }

                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "User deleted from database", Toast.LENGTH_SHORT).show()
                            }

                            user.delete().addOnCompleteListener { deleteTask ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (deleteTask.isSuccessful) {
                                        Toast.makeText(requireContext(), "User deleted successfully", Toast.LENGTH_SHORT).show()
                                        findNavController().navigate(R.id.action_profileFragment_to_welcomeFragment)
                                    } else {
                                        Toast.makeText(requireContext(), "Error: ${deleteTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }


                        }
                    }
                    .addOnFailureListener { e ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Error deleting user from Firestore", Toast.LENGTH_SHORT).show()
                            println("Failed to delete user from Firestore: ${e.message}")
                        }
                    }
            } catch (e: Exception) {
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error deleting user: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}
