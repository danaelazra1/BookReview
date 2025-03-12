package com.idz.bookreview.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.idz.bookreview.R

class AddReviewFragment : Fragment() {

    private lateinit var bookNameEditText: EditText
    private lateinit var bookDescriptionEditText: EditText
    private lateinit var reviewEditText: EditText
    private lateinit var sendReviewButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var bookImageView: ImageView

    private var imageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
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
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun uploadReview() {
        val bookName = bookNameEditText.text.toString()
        val bookDescription = bookDescriptionEditText.text.toString()
        val review = reviewEditText.text.toString()

        if (imageUri != null) {
            MediaManager.get().upload(imageUri)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val imageUrl = resultData["url"].toString()
                        Toast.makeText(requireContext(), "Image Uploaded!", Toast.LENGTH_SHORT).show()

                        // כאן תוסיפי קוד לשליחת הביקורת ל-Firestore, כולל ה- imageUrl
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Toast.makeText(requireContext(), "Upload failed: ${error?.description}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()
        } else {
            Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
        }
    }
}








