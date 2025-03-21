package com.idz.bookreview.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.idz.bookreview.R
import com.idz.bookreview.model.Review
import com.idz.bookreview.viewmodel.ReviewViewModel
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream

class EditReviewFragment : Fragment() {

    private lateinit var viewModel: ReviewViewModel
    private lateinit var editTitle: EditText
    private lateinit var editAuthor: EditText
    private lateinit var editReview: EditText
    private lateinit var editImageView: ImageView
    private lateinit var uploadImageButton: Button
    private lateinit var saveButton: Button


    private var reviewId: String? = null
    private var selectedImageUri: Uri? = null
    private var isImageRemoved = false  // מעקב אחרי מחיקת תמונה

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val imageUri: Uri? = result.data!!.data
            if (imageUri != null) {
                selectedImageUri = imageUri
                isImageRemoved = false
                Picasso.get().load(imageUri).into(editImageView)
            }
        }
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater, container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View? {
        val view = inflater.inflate(R.layout.fragment_edit_review, container, false)

        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))
            .get(ReviewViewModel::class.java)

        editTitle = view.findViewById(R.id.editTitle)
        editAuthor = view.findViewById(R.id.editAuthor)
        editReview = view.findViewById(R.id.editReview)
        editImageView = view.findViewById(R.id.editImageView)
        uploadImageButton = view.findViewById(R.id.uploadImageButton)
        saveButton = view.findViewById(R.id.saveButton)

        reviewId = arguments?.getString("reviewId")
        reviewId?.let { viewModel.loadReviewById(it) }

        viewModel.review.observe(viewLifecycleOwner, Observer { review ->
            review?.let { updateUI(it) }
        })

        uploadImageButton.setOnClickListener { showImageOptions() }
        saveButton.setOnClickListener { saveReview() }

        return view
    }

    private fun updateUI(review: Review) {
        editTitle.setText(review.title)
        editAuthor.setText(review.author)
        editReview.setText(review.review)

        if (!review.imageUrl.isNullOrEmpty() && !isImageRemoved) {
            Picasso.get().load(review.imageUrl).into(editImageView)
            selectedImageUri = Uri.parse(review.imageUrl)
        } else {
            editImageView.setImageResource(R.drawable.ic_default_book)
        }
    }

    private fun showImageOptions() {
        val options = arrayOf("Gallery", "Camera", "Remove Image")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Image Option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> pickImageFromGallery()
                1 -> checkCameraPermission()
                2 -> removeImage()
            }
        }
        builder.show()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 102)
    }

    private fun removeImage() {
        selectedImageUri = null
        isImageRemoved = true
        editImageView.setImageResource(R.drawable.ic_default_book)
        Toast.makeText(requireContext(), "Image Removed", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == 102 && data != null) {
            val imageBitmap = data.extras?.get("data") as Bitmap
            val imageUri = saveImageToStorage(imageBitmap)
            if (imageUri != null) {
                selectedImageUri = imageUri
                Picasso.get().load(imageUri).into(editImageView)
            }
        }
    }

    private fun saveImageToStorage(bitmap: Bitmap): Uri? {
        val file = File(requireContext().cacheDir, "captured_image.jpg")
        return try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveReview() {
        if (reviewId == null) {
            Toast.makeText(requireContext(), "Error: No review ID found.", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedReview = Review(
            id = reviewId!!,
            userName = viewModel.review.value?.userName ?: "",
            title = editTitle.text.toString(),
            author = editAuthor.text.toString(),
            review = editReview.text.toString(),
            imageUrl = if (isImageRemoved) "" else (selectedImageUri?.toString() ?: viewModel.review.value?.imageUrl ?: ""),
            timestamp = System.currentTimeMillis()
        )

        if (selectedImageUri != null && !isImageRemoved) {
            // אם התמונה חדשה או עודכנה
            viewModel.saveReview(updatedReview, selectedImageUri.toString(), requireContext())
        } else {
            // אם התמונה נמחקה או לא נבחרה תמונה חדשה
            viewModel.saveReview(updatedReview, null, requireContext())
        }

        // מיד לאחר שמירה נעדכן את המסך הקודם
        findNavController().navigate(R.id.action_editReviewFragment_to_myReviewsFragment)
    }

}