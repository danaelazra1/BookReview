package com.idz.bookreview.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.idz.bookreview.R

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)  // Only on the home page we load the 3-dot menu
    }
  }
