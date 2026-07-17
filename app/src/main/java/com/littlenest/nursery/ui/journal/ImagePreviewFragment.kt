package com.littlenest.nursery.ui.journal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.littlenest.nursery.R

class ImagePreviewFragment : Fragment(R.layout.fragment_image_preview) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val viewPager = view.findViewById<ViewPager2>(R.id.previewPager)

        val images = arguments?.getStringArrayList("images") ?: arrayListOf()
        val startPosition = arguments?.getInt("position") ?: 0
        val baseUrl = arguments?.getString("baseUrl") ?: ""

        // ✅ CLEAN adapter usage
        //val adapter = JournalImagePagerAdapter(images, baseUrl)
        // 🔥 FIX: pass onClick
        val adapter = JournalImagePagerAdapter(
            images = images,
            baseUrl = baseUrl,
            onClick = {
                // 🔥 OPTIONAL: tap image to close preview
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        )

        viewPager.adapter = adapter
        viewPager.setCurrentItem(startPosition, false)

        // Tap to close
        view.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
}