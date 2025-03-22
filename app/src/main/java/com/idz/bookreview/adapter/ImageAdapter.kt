package com.idz.bookreview.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.idz.bookreview.R

class ImageAdapter(context: Context, private val imageUrls: List<String>) :
    ArrayAdapter<String>(context, 0, imageUrls) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val imageView = convertView as? ImageView
            ?: LayoutInflater.from(context).inflate(R.layout.item_image, parent, false) as ImageView
        Glide.with(context).load(getItem(position)).into(imageView)
        return imageView
    }
}