package com.littlenest.nursery.ui.family

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import com.littlenest.nursery.R
import com.littlenest.nursery.adapter.SlideshowAdapter
import com.littlenest.nursery.databinding.FragmentSlideshowBinding
import com.littlenest.nursery.model.PostImage
import com.littlenest.nursery.ui.common.BaseFragment
import android.util.Log
import com.littlenest.nursery.utils.ImageUtils

class SlideshowFragment : BaseFragment(R.layout.fragment_slideshow) {

    private lateinit var binding: FragmentSlideshowBinding
    private val args: SlideshowFragmentArgs by navArgs()
    private lateinit var adapter: SlideshowAdapter
    private lateinit var postImages: List<PostImage>
    private val PERMISSION_REQUEST_CODE = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startPosition = args.position
        //val baseUrl = getBaseUrl()
        postImages = args.images.toList()
        //Log.d("slideshow", "$postImages")

        // Setup ViewPager2
        adapter = SlideshowAdapter(postImages, uploadsBaseUrl = uploadsBaseUrl())
        binding.viewPagerSlideshow.adapter = adapter
        binding.viewPagerSlideshow.setCurrentItem(startPosition, false)

        // Swipe hint animation
        binding.tvSwipeHint.animate().alpha(0f).setDuration(1500).setStartDelay(3000).start()


        // Download button click
        binding.btnDownload.setOnClickListener {
            val currentPosition = binding.viewPagerSlideshow.currentItem
            val currentImage = postImages[currentPosition]
            val imageUrl = ImageUtils.resolveImageUrl(uploadsBaseUrl(), currentImage.imageUrl)
            //Log.d("DOWNLOAD_URLl-", imageUrl)
            checkPermissionAndDownload(imageUrl)
        }
    }

    private fun checkPermissionAndDownload(url: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ doesn't require storage permission
            downloadImage(url)
        } else {
            // Check for WRITE_EXTERNAL_STORAGE permission for Android 9 and below
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                downloadImage(url)
            }
        }
    }

    private fun downloadImage(imageUrl: String) {
        try {
            val fileName = imageUrl.substringAfterLast("/")

            val request = DownloadManager.Request(Uri.parse(imageUrl))
                .setTitle(fileName)
                .setDescription("Downloading image...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            val manager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)

            Toast.makeText(requireContext(), "Downloading file to the download..", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Handle permission result for Android 9 and below
    @Deprecated("Use Activity Result API instead", ReplaceWith("registerForActivityResult(...)"))
    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val currentPosition = binding.viewPagerSlideshow.currentItem
            val currentImage = postImages[currentPosition]
            //val imageUrl = "${getBaseUrl()}${currentImage.imageUrl}"
            val imageUrl = ImageUtils.resolveImageUrl(
                uploadsBaseUrl(),
                currentImage.imageUrl
            )
            downloadImage(imageUrl)
        } else {
            Toast.makeText(requireContext(), "Storage permission denied. Cannot download image.", Toast.LENGTH_SHORT).show()
        }
    }
}
