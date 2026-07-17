package com.littlenest.nursery.ui.chat

import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentChatListBinding
import com.littlenest.nursery.ui.common.BaseFragment
import androidx.navigation.fragment.findNavController

class ChatListFragment : BaseFragment(R.layout.fragment_chat_list) {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatListViewModel by viewModels()
    private lateinit var adapter: ChatListAdapter

    override fun setupUI(view: View) {
        _binding = FragmentChatListBinding.bind(view)
        val token = getToken() ?: return
        val apiKey = getApiKey() ?: return

        adapter = ChatListAdapter(emptyList(), baseUrl = getBaseUrl()) { chat ->
            if (chat.type == "group") {
                // group chat: pass groupId (Int) and groupName (String)
                val action = ChatListFragmentDirections
                    .actionChatListFragmentToGroupChatFragment(
                        chat.groupId!!,
                        chat.groupName ?: "Group"
                    )
                findNavController().navigate(action)
            } else {
                // private chat: pass ChatListItem object (must be Parcelable)
                val action = ChatListFragmentDirections
                    .actionChatListFragmentToChatFragment(chat)
                findNavController().navigate(action)
            }



            if (!chat.isRead) {
                //Log.d("thisharika", "msgid ${chat.messageId}")
                //viewModel.markMessageAsRead(token, apiKey, chat.messageId)
            }
        }

        binding.recyclerChats.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerChats.adapter = adapter

        viewModel.chatList.observe(viewLifecycleOwner) { chats ->
            adapter.setData(chats)
        }

        viewModel.loadChats(token, apiKey)

        //Add new chat (user-list fragment)
        binding.fabNewChat.setOnClickListener {
            findNavController().navigate(R.id.action_chatListFragment_to_userListFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
