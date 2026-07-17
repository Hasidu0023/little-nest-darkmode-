package com.littlenest.nursery.ui.family

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentFamilyAlbumBinding
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.viewmodel.family.AlbumViewModel


class AlbumFragment : BaseFragment(R.layout.fragment_family_album) {

    private lateinit var binding: FragmentFamilyAlbumBinding
    private lateinit var adapter: AlbumAdapter
    private val viewModel: AlbumViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFamilyAlbumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI(view: View) {
        super.setupUI(view)

        val authToken = getToken() ?: return
        val apiKey = getApiKey()

        adapter = AlbumAdapter(
            requireContext(),
            baseUrl = getBaseUrl(),
            emptyList()) { position ->
            // Open slideshow when image clicked
            val images = viewModel.albumImages.value?.toTypedArray() ?: return@AlbumAdapter
            val action = AlbumFragmentDirections.actionAlbumToSlideshow(images, position)
            findNavController().navigate(action)
        }

        binding.recyclerAlbum.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = this@AlbumFragment.adapter
            setHasFixedSize(true)
        }

        viewModel.albumImages.observe(viewLifecycleOwner) { images ->
            adapter.updateData(images)
        }


        // Fetch album from backend using BaseFragment token + api key
        viewModel.fetchAlbum(authToken, apiKey)
    }
}
