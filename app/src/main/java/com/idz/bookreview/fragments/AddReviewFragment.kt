package com.idz.bookreview.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.bookreview.R
import com.squareup.picasso.Picasso

class AddReviewFragment : Fragment() {

    private lateinit var bookNameEditText: EditText
    private lateinit var bookDescriptionEditText: EditText
    private lateinit var reviewEditText: EditText
    private lateinit var sendReviewButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var bookImageView: ImageView

    private var imageUri: Uri? = null
    private val db = FirebaseFirestore.getInstance() // חיבור ל-Firestore

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(requireContext(), "Permission denied!", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri = result.data?.data
            bookImageView.setImageURI(imageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_review, container, false)

        bookNameEditText = view.findViewById(R.id.bookNameEditText)
        bookDescriptionEditText = view.findViewById(R.id.bookDescriptionEditText)
        reviewEditText = view.findViewById(R.id.reviewEditText)
        sendReviewButton = view.findViewById(R.id.sendReviewButton)
        selectImageButton = view.findViewById(R.id.selectImageButton)
        bookImageView = view.findViewById(R.id.bookImageView)

        selectImageButton.setOnClickListener {
            selectImageFromGallery()
        }

        sendReviewButton.setOnClickListener {
            uploadReview()
        }

        return view
    }

    private fun selectImageFromGallery() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openGallery()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun uploadReview() {
        val bookName = bookNameEditText.text.toString().trim()
        val bookDescription = bookDescriptionEditText.text.toString().trim()
        val reviewText = reviewEditText.text.toString().trim()

        if (bookName.isEmpty() || reviewText.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        if (imageUri != null) {
            MediaManager.get().upload(imageUri)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val imageUrl = resultData["url"].toString()
                        Toast.makeText(requireContext(), "Upload successful!", Toast.LENGTH_SHORT).show()

                        Picasso.get().load(imageUrl).into(bookImageView)

                        saveReviewToFirestore(userId, bookName, bookDescription, reviewText, imageUrl)
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Toast.makeText(requireContext(), "Upload failed: ${error?.description}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()
        } else {
            saveReviewToFirestore(userId, bookName, bookDescription, reviewText, null)
        }
    }

    private fun saveReviewToFirestore(userId: String, bookTitle: String, bookDescription: String, reviewText: String, imageUrl: String?) {
        val reviewData = hashMapOf(
            "userId" to userId,  // שמירת userId כדי שהביקורת תהיה משויכת למשתמש
            "bookTitle" to bookTitle,
            "bookDescription" to bookDescription,
            "reviewText" to reviewText,
            "imageUrl" to (imageUrl ?: ""),
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance().collection("reviews")
            .add(reviewData)
            .addOnSuccessListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Review added successfully!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Failed to save review", Toast.LENGTH_SHORT).show()
                }
            }
    }

}












