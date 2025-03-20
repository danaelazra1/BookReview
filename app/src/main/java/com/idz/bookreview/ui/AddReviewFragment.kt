package com.idz.bookreview.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.idz.bookreview.R
import com.idz.bookreview.viewmodel.AddReviewViewModel
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class AddReviewFragment : Fragment() {

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val CAPTURE_IMAGE_REQUEST = 2
    }

    private lateinit var bookTitleEditText: EditText
    private lateinit var bookAuthorEditText: EditText
    private lateinit var reviewEditText: EditText
    private lateinit var saveReviewButton: Button
    private lateinit var addImageButton: Button
    private lateinit var bookImageView: ImageView
    private lateinit var userNameTextView: TextView
    private val viewModel: AddReviewViewModel by viewModels()

    private var selectedImageUri: Uri? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_review, container, false)

        bookTitleEditText = view.findViewById(R.id.bookTitleEditText)
        bookAuthorEditText = view.findViewById(R.id.bookAuthorEditText)
        reviewEditText = view.findViewById(R.id.reviewEditText)
        saveReviewButton = view.findViewById(R.id.saveReviewButton)
        addImageButton = view.findViewById(R.id.addImageButton)
        bookImageView = view.findViewById(R.id.bookImageView)
        userNameTextView = view.findViewById(R.id.userNameTextView)


        viewModel.userName.observe(viewLifecycleOwner) { userName ->
            userNameTextView.text = userName
        }
        addImageButton.setOnClickListener { showImageSourceDialog() }

        saveReviewButton.setOnClickListener {
            saveReview()
        }

        viewModel.updateUserName()

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Bundle>("imageData")
            ?.observe(viewLifecycleOwner) { bundle ->
                val imageUrl = bundle.getString("imageUrl")
                if (!imageUrl.isNullOrEmpty()) {
                    selectedImageUri = Uri.parse(imageUrl)
                    Picasso.get().load(imageUrl).into(bookImageView)
                }
            }

        return view
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Camera", "Gallery", "Search from API")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Image Source")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> checkCameraPermission()
                1 -> openGallery()
                2 -> openBookImageSearch()
            }
        }
        builder.show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> openCamera()

            shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {
                Toast.makeText(requireContext(), "Camera permission is needed to take pictures.", Toast.LENGTH_SHORT).show()
            }

            else -> requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Camera not available.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun openBookImageSearch() {
        findNavController().navigate(R.id.action_addReviewFragment_to_searchFragment)
    }

    private fun saveReview() {
        val bookTitle = bookTitleEditText.text.toString()
        val bookAuthor = bookAuthorEditText.text.toString()
        val reviewText = reviewEditText.text.toString()

        if (bookTitle.isNotBlank() && bookAuthor.isNotBlank() && reviewText.isNotBlank()) {
            viewModel.saveReview(bookTitle, bookAuthor, reviewText, selectedImageUri)
            Toast.makeText(requireContext(), "Review saved successfully!", Toast.LENGTH_SHORT).show()
            clearInputs()
        } else {
            Toast.makeText(requireContext(), "Please fill all fields.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearInputs() {
        bookTitleEditText.text.clear()
        bookAuthorEditText.text.clear()
        reviewEditText.text.clear()
        bookImageView.setImageResource(android.R.drawable.ic_menu_report_image)
        selectedImageUri = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    selectedImageUri = data?.data
                    Picasso.get().load(selectedImageUri).into(bookImageView)
                }
                CAPTURE_IMAGE_REQUEST -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    selectedImageUri = saveImageToStorage(imageBitmap)
                    Picasso.get().load(selectedImageUri).into(bookImageView)
                }
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
}