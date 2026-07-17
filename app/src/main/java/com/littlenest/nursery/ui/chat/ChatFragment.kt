package com.littlenest.nursery.ui.chat

import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.littlenest.nursery.R
import com.littlenest.nursery.databinding.FragmentChatBinding
import com.littlenest.nursery.model.Message
import com.littlenest.nursery.ui.common.BaseFragment
import com.github.dhaval2404.imagepicker.ImagePicker
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import androidx.navigation.fragment.findNavController
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class ChatFragment : BaseFragment(R.layout.fragment_chat) {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val chatViewModel: ChatViewModel by viewModels()
    private val args: ChatFragmentArgs by navArgs() // ✅ Safe Args
    private lateinit var adapter: MessageAdapter
    private var selectedFile: Uri? = null

    override fun setupUI(view: View) {
        _binding = FragmentChatBinding.bind(view)
        val chat = args.chat
        val partnerId = chat.partnerId
        val partnerName = chat.partnerName
        val partnerImage = chat.partnerProfilePicture

        Log.d("partnerimage", "$partnerImage")

        // Setup toolbar
        val toolbar = binding.chatToolbar
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Inflate a custom layout inside the toolbar
        val customView = layoutInflater.inflate(R.layout.toolbar_chat_header, null)
        val avatarView = customView.findViewById<ImageView>(R.id.ivPartnerAvatar)
        val nameView = customView.findViewById<TextView>(R.id.tvPartnerName)

        nameView.text = partnerName

        if (!partnerImage.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(getBaseUrl() + partnerImage)
                .placeholder(R.drawable.avatar_placeholder)
                .circleCrop()
                .into(avatarView)
        } else {
            avatarView.setImageResource(R.drawable.avatar_placeholder)
        }
        toolbar.addView(customView)



        // RecyclerView setup
        adapter = MessageAdapter(
            baseUrl = getBaseUrl(),
            currentUserId = userId()
        )
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMessages.adapter = adapter

        // Load conversation
        chatViewModel.loadConversation(
            partnerId = partnerId,
            token = getToken().orEmpty(),
            apiKey = getApiKey()
        )

        // Observe messages
        chatViewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.setMessages(messages)
            if (messages.isNotEmpty()) {
                binding.rvMessages.scrollToPosition(messages.size - 1)
            }
        }

        // Initialize Socket.IO
        SocketManager.initSocket(getBaseUrl(), getToken().orEmpty(), getApiKey())

        // Listen to incoming messages
        val socket = SocketManager.getSocket()
        socket.on("receiveMessage") { args ->
            val json = args[0] as? org.json.JSONObject
            json?.let {
                val msg = Message(
                    id = it.optInt("id"),
                    senderId = it.optInt("senderId"),
                    receiverId = it.optInt("receiverId"),
                    groupId = null,
                    messageText = it.optString("messageText"),
                    fileUrl = it.optString("fileUrl"),
                    isRead = it.optBoolean("isRead"),
                    createdAt = it.optString("createdAt"),
                    updatedAt = it.optString("updatedAt"),
                )
                requireActivity().runOnUiThread {
                    chatViewModel.addMessage(msg)
                }
            }
        }

        // Send button
        binding.btnSend.setOnClickListener {
            val messageText = binding.etMessage.text?.toString()?.trim()
            if (!messageText.isNullOrEmpty() || selectedFile != null) {
                sendMessage(partnerId, messageText)
            }
            binding.etMessage.text?.clear()
        }

        // Attach file button
        binding.btnAttach.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .createIntent { intent ->
                    imagePickerLauncher.launch(intent)
                }
        }
    }

    private fun sendMessage(partnerId: Int, messageText: String?) {
        var filePart: MultipartBody.Part? = null

        selectedFile?.let { uri ->
            val file = File(uri.path!!)
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            filePart = MultipartBody.Part.createFormData("file", file.name, requestBody)
        }

        chatViewModel.sendMessage(
            token = getToken().orEmpty(),
            apiKey = getApiKey(),
            receiverId = partnerId,
            messageText = messageText,
            filePart = filePart
        )

        selectedFile = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        SocketManager.disconnect()
    }

    private val imagePickerLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (uri != null) {
            selectedFile = uri
            Toast.makeText(requireContext(), "File selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Hide global toolbar
        requireActivity().findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        // Show it again when leaving chat
        requireActivity().findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.visibility = View.VISIBLE
    }
}
