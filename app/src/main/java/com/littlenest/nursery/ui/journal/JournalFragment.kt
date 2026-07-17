package com.littlenest.nursery.ui.journal

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentFamilyJournalBinding
import com.littlenest.nursery.ui.common.BaseFragment
import com.littlenest.nursery.model.PostImage
import android.util.Log

class JournalFragment : BaseFragment(R.layout.fragment_family_journal) {

    private var _binding: FragmentFamilyJournalBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JournalViewModel by viewModels()
    private lateinit var adapter: JournalAdapter
    private lateinit var userRole: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFamilyJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI(view: View) {
        userRole = getUserRole() ?: "student"

        // ✅ Create adapter with delete & edit callbacks
        adapter = JournalAdapter(
            posts = emptyList(),
            baseUrl = getBaseUrl() + "/uploads",
            userRole = userRole,
            onDeleteClick = { post -> confirmDelete(post) },
            onEditClick = { post -> editPost(post) },
            onImageClick = { post, position ->

                // ✅ Convert ONLY this post images
                val images = post.images.map {
//                    val fullUrl = getBaseUrl() + "/uploads/" + it
//                    Log.d("SLIDESHOW", fullUrl)
                    PostImage(
                        postId = post.id,
                        imageUrl = it,
                        description = post.description ?: "",
                        createdAt = post.createdAt ?: ""
                    )
                }.toTypedArray()

                val bundle = Bundle().apply {
                    putParcelableArray("images", images)
                    putInt("position", position)
                }

                findNavController().navigate(R.id.action_global_slideshow, bundle)

                //val action = JournalFragmentDirections
//                    .actionNavFamilyJournalToSlideshow(images, position)

//                val action = JournalFragmentDirections
//                    .actionNavFamilyJournalToSlideshow(images, position)
//                findNavController().navigate(action)
            }
        )

        binding.recyclerViewJournal.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewJournal.adapter = adapter

        // ✅ Observe journal posts
        viewModel.journalPosts.observe(viewLifecycleOwner) { posts ->
            if (posts.isNullOrEmpty()) {
                binding.textNoPosts.visibility = View.VISIBLE
                binding.recyclerViewJournal.visibility = View.GONE
            } else {
                binding.textNoPosts.visibility = View.GONE
                binding.recyclerViewJournal.visibility = View.VISIBLE
                adapter.submitList(posts)
            }
        }

        // ✅ Load correct posts based on role
        if (userRole == "teacher") {
            viewModel.loadJournalPostsByTeacherGroup(getToken(), getApiKey())
        } else {
            viewModel.loadJournalPosts(getToken(), getApiKey())
        }

        // ✅ Floating Add Button (Teacher only)
        binding.JournalAddFloatingIcon.visibility =
            if (userRole == "teacher") View.VISIBLE else View.GONE

        binding.JournalAddFloatingIcon.setOnClickListener {
            findNavController()
                .navigate(R.id.action_navFamilyJournal_to_JournalAddFragment)
        }
    }

    // ---------------- DELETE POST ----------------
    private fun confirmDelete(post: JournalPost) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deletePost(
                    token = getToken(),
                    apiKey = getApiKey(),
                    postId = post.id,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show()
                        viewModel.loadJournalPostsByTeacherGroup(getToken(), getApiKey())
                    },
                    onError = {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ---------------- EDIT POST ----------------
    private fun editPost(post: JournalPost) {
        // Navigate to EditJournalFragment (or AddJournalFragment in edit mode)
//        val action = JournalFragmentDirections
//            .actionNavFamilyJournalToEditJournalFragment(postId = post.id)
//        findNavController().navigate(action)

        val bundle = Bundle().apply {
            putInt("postId", post.id)
        }

        findNavController().navigate(R.id.editJournalFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}