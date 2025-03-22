package com.idz.bookreview.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.bookreview.R
import com.idz.bookreview.model.Review
import com.idz.bookreview.model.dao.AppDatabase
import com.idz.bookreview.viewmodel.ReviewViewModel
import com.idz.bookreview.viewmodel.ReviewViewModelFactory
import kotlinx.coroutines.launch
import java.util.UUID
import com.idz.bookreview.model.networking.FirebaseService
import android.util.Log

class AddReviewFragment : Fragment() {

    companion object {
        private const val GALLERY_REQUEST_CODE = 101
    }

    private lateinit var bookNameEditText: EditText
    private lateinit var bookDescriptionEditText: EditText
    private lateinit var reviewEditText: EditText
    private lateinit var sendReviewButton: Button
    private lateinit var openGalleryButton: ImageButton
    private lateinit var bookImageView: ImageView
    private lateinit var progressBar: ProgressBar

    private var imageUri: Uri? = null
    private var uploadedImageUrl: String? = null
    private val db = FirebaseFirestore.getInstance()

    private val reviewViewModel: ReviewViewModel by activityViewModels {
        ReviewViewModelFactory(
            AppDatabase.getDatabase(requireContext()).reviewDao(),
            FirebaseService()
        )
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
        openGalleryButton = view.findViewById<ImageButton>(R.id.openGalleryButton)
        bookImageView = view.findViewById(R.id.bookImageView)
        progressBar = view.findViewById(R.id.progressBar)

        openGalleryButton.setOnClickListener {
            openGallery()
        }

        sendReviewButton.setOnClickListener {
            uploadReview()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.data

            if (imageUri != null) {
                // ✅ מציג מיד את התמונה שנבחרה בגלריה
                bookImageView.setImageURI(imageUri)
                bookImageView.visibility = View.VISIBLE

                openGalleryButton.visibility = View.GONE

                uploadImageToCloudinary(imageUri!!)
            } else {
                Toast.makeText(requireContext(), "שגיאה בבחירת תמונה", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun uploadImageToCloudinary(imageUri: Uri) {
        progressBar.visibility = View.VISIBLE
        MediaManager.get().upload(imageUri)
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val imageUrl = resultData["url"].toString()
                    uploadedImageUrl = imageUrl
                    Glide.with(requireContext()).load(imageUrl).into(bookImageView)
                    bookImageView.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    progressBar.visibility = View.GONE
                    Log.e("Cloudinary", "Upload error: ${error?.description}")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
            }).dispatch()
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
        saveReview(userId, bookName, bookDescription, reviewText, uploadedImageUrl)
    }

    private fun saveReview(userId: String, bookName: String, bookDescription: String, reviewText: String, imageUrl: String?) {
        val newReview = Review(
            id = UUID.randomUUID().toString(),
            bookTitle = bookName,
            bookDescription = bookDescription,
            reviewText = reviewText,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis(),
            userId = userId
        )

        lifecycleScope.launch {
            reviewViewModel.addReview(newReview)
            clearInputFields()
            sendReviewButton.isEnabled = true
            Toast.makeText(requireContext(), "הביקורת נוספה בהצלחה!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearInputFields() {
        bookNameEditText.text.clear()
        bookDescriptionEditText.text.clear()
        reviewEditText.text.clear()
        uploadedImageUrl = null
        bookImageView.setImageResource(android.R.drawable.ic_menu_camera)
        bookImageView.visibility = View.GONE
    }
}

