package com.littlenest.nursery.ui.chat

import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.littlenest.nursery.R
import com.littlenest.nursery.ui.common.BaseFragment
import androidx.lifecycle.Observer

class UserListFragment : BaseFragment(R.layout.fragment_chat_user_list) {

    private lateinit var adapter: UserAdapter
    private val viewModel: UserListViewModel by viewModels()

    override fun setupUI(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvUsers)
        adapter = UserAdapter(emptyList(), baseUrl = getBaseUrl()) { user ->

            // Build ChatListItem object
            val chatItem = ChatListItem(
                type = "private",
                partnerId = user.id,
                partnerName = user.fullName,
                partnerProfilePicture = user.profilePicture ?: "",
                lastMessage = "",      // no messages yet
                createdAt = "",        // no timestamp yet
                isRead = true,         // default value
                messageId = 0
            )

            val action = UserListFragmentDirections.actionUserListToChatFragment(chatItem)
            findNavController().navigate(action)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Observe users
        viewModel.users.observe(viewLifecycleOwner, Observer { users ->
            adapter.setData(users)
        })

        // Observe errors (optional)
        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                //showToast("Error loading users: $it")
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        })

        // Load users via ViewModel
        val token = getToken().orEmpty()
        val apiKey = getApiKey().orEmpty()
        viewModel.loadUsers(token, apiKey)
    }
}

