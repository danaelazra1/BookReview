package com.idz.bookreview.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.bookreview.R
import com.idz.bookreview.model.Review
import com.idz.bookreview.model.AppDatabase
import com.idz.bookreview.viewmodel.ReviewViewModel
import com.idz.bookreview.viewmodel.ReviewViewModelFactory
import kotlinx.coroutines.launch
import java.util.UUID

class AddReviewFragment : Fragment() {

    private lateinit var bookNameEditText: EditText
    private lateinit var bookDescriptionEditText: EditText
    private lateinit var reviewEditText: EditText
    private lateinit var sendReviewButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var bookImageView: ImageView

    private var imageUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()

    private val reviewViewModel: ReviewViewModel by activityViewModels {
        ReviewViewModelFactory(AppDatabase.getDatabase(requireContext()).reviewDao())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_add_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "אין משתמש מחובר! חוזרים להתחברות...", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.loginFragment)
            return
        }

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
    }

    private fun selectImageFromGallery() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 1001)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1002)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1002 && resultCode == RESULT_OK) {
            imageUri = data?.data
            bookImageView.setImageURI(imageUri)
        }
    }

    private fun uploadReview() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "יש להתחבר לפני הוספת ביקורת", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.loginFragment)
            return
        }

        val bookName = bookNameEditText.text.toString().trim()
        val bookDescription = bookDescriptionEditText.text.toString().trim()
        val reviewText = reviewEditText.text.toString().trim()
        val userId = user.uid

        if (bookName.isEmpty() || reviewText.isEmpty()) {
            Toast.makeText(requireContext(), "נא למלא את כל השדות", Toast.LENGTH_SHORT).show()
            return
        }

        sendReviewButton.isEnabled = false

        if (imageUri != null) {
            MediaManager.get().upload(imageUri)
                .callback(object : UploadCallback {
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val imageUrl = resultData["url"].toString()
                        saveReview(userId, bookName, bookDescription, reviewText, imageUrl)
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        if (isAdded) {
                            Toast.makeText(requireContext(), "שגיאה בהעלאת תמונה: ${error?.description}", Toast.LENGTH_SHORT).show()
                        }
                        sendReviewButton.isEnabled = true
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                }).dispatch()
        } else {
            saveReview(userId, bookName, bookDescription, reviewText, null)
        }
    }

    private fun saveReview(userId: String, bookName: String, bookDescription: String, reviewText: String, imageUrl: String?) {
        val newReview = Review(
            id = UUID.randomUUID().toString(),
            bookTitle = bookName,
            reviewText = reviewText,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis(),
            userId = userId
        )

        lifecycleScope.launch {
            reviewViewModel.addReview(newReview)
            saveReviewToFirestore(newReview)

            clearInputFields()

            sendReviewButton.isEnabled = true

            Toast.makeText(requireContext(), "הביקורת נוספה בהצלחה!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearInputFields() {
        bookNameEditText.text.clear()
        bookDescriptionEditText.text.clear() // ✅ מנקה את תיאור הספר
        reviewEditText.text.clear()
        imageUri = null
        bookImageView.setImageResource(android.R.drawable.ic_menu_camera) // ✅ מחזיר את אייקון המצלמה
    }

    private fun saveReviewToFirestore(review: Review) {
        db.collection("reviews").document(review.id)
            .set(review)
            .addOnSuccessListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "הביקורת נשמרה ב-Firestore", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "שגיאה בשמירת ביקורת: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}


















