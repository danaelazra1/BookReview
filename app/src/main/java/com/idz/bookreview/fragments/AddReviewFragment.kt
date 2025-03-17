package com.idz.bookreview.fragments

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
import com.idz.bookreview.model.AppDatabase
import com.idz.bookreview.viewmodel.ReviewViewModel
import com.idz.bookreview.viewmodel.ReviewViewModelFactory
import kotlinx.coroutines.launch
import java.util.UUID
import com.idz.bookreview.model.networking.FirebaseService
import android.util.Log
import java.net.URL
import android.graphics.BitmapFactory
import androidx.appcompat.app.AlertDialog
import java.io.IOException
import org.json.JSONObject
import org.json.JSONException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.idz.bookreview.model.CloudinaryResponse
import com.idz.bookreview.model.CloudinaryImage
import com.idz.bookreview.network.CloudinaryApi
import com.idz.bookreview.adapter.ImageAdapter

class AddReviewFragment : Fragment() {

    private lateinit var bookNameEditText: EditText
    private lateinit var bookDescriptionEditText: EditText
    private lateinit var reviewEditText: EditText
    private lateinit var sendReviewButton: Button
    private lateinit var selectFromCloudButton: Button
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
        selectFromCloudButton = view.findViewById(R.id.selectFromCloudButton)
        bookImageView = view.findViewById(R.id.bookImageView)
        progressBar = view.findViewById(R.id.progressBar)

        selectFromCloudButton.setOnClickListener {
            fetchUploadedImages()
        }

        sendReviewButton.setOnClickListener {
            uploadReview()
        }
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

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            bookImageView.setImageURI(imageUri)
            uploadImageToCloudinary(imageUri!!)
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

    private fun clearInputFields() {
        bookNameEditText.text.clear()
        bookDescriptionEditText.text.clear()
        reviewEditText.text.clear()
        uploadedImageUrl = null
        bookImageView.setImageResource(android.R.drawable.ic_menu_camera)
        bookImageView.visibility = View.GONE
    }

    private fun parseCloudinaryResponse(json: String): List<String> {
        val imageUrls = mutableListOf<String>()
        try {
            val jsonObject = JSONObject(json)
            val resourcesArray = jsonObject.getJSONArray("resources")
            for (i in 0 until resourcesArray.length()) {
                val resource = resourcesArray.getJSONObject(i)
                val secureUrl = resource.getString("secure_url")
                imageUrls.add(secureUrl)
            }
        } catch (e: JSONException) {
            Log.e("Cloudinary", "Error parsing JSON: ${e.message}")
        }
        return imageUrls
    }

    private fun fetchUploadedImages() {
        val interceptor = okhttp3.Interceptor { chain ->
            val credentials = okhttp3.Credentials.basic("799164549199668", "Qp3H41L2RLVJ9X8ivR6z2PM45XM")
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", credentials)
                .build()
            chain.proceed(newRequest)
        }

        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.cloudinary.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(CloudinaryApi::class.java)

        api.getImages().enqueue(object : Callback<CloudinaryResponse> {
            override fun onResponse(call: Call<CloudinaryResponse>, response: Response<CloudinaryResponse>) {
                if (!response.isSuccessful || response.body() == null) {
                    Log.e("Cloudinary", "Failed to fetch images: ${response.code()}")
                    return
                }

                val imageUrls = response.body()!!.resources.map { it.secureUrl }
                requireActivity().runOnUiThread {
                    showCloudinaryImagePicker(imageUrls)
                }
            }

            override fun onFailure(call: Call<CloudinaryResponse>, t: Throwable) {
                Log.e("Cloudinary", "Error fetching images: ${t.message}")
            }
        })
    }

    private fun showCloudinaryImagePicker(imageUrls: List<String>) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Image From Cloudinary")

        val listView = ListView(requireContext())
        val adapter = ImageAdapter(requireContext(), imageUrls)
        listView.adapter = adapter
        builder.setView(listView)

        builder.setNegativeButton("ביטול", null)

        val dialog = builder.create()

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedImageUrl = imageUrls[position]
            uploadedImageUrl = selectedImageUrl
            Glide.with(requireContext()).load(selectedImageUrl).into(bookImageView)
            bookImageView.visibility = View.VISIBLE
            dialog.dismiss()  // סוגר את החלון לאחר הבחירה
        }

        dialog.show()
    }


}




