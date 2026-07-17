package com.littlenest.nursery.ui.chat

import android.util.Log
import com.littlenest.nursery.model.Message
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

object SocketManager {

    private var socket: Socket? = null

    // Store user token & API key if needed for auth
    var token: String? = null
    var apiKey: String? = null

    /** Initialize socket if not already connected */
    fun initSocket(baseUrl: String, token: String, apiKey: String): Socket {
        if (socket == null) {
            try {
                this.token = token
                this.apiKey = apiKey
                socket = IO.socket(baseUrl)

                socket?.on(Socket.EVENT_CONNECT) {
                    Log.d("SocketManager", "Socket connected")
                }

                socket?.on(Socket.EVENT_DISCONNECT) {
                    Log.d("SocketManager", "Socket disconnected")
                }

                socket?.connect()
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }
        }
        return socket!!
    }

    /** Get initialized socket, throw if not initialized */
    fun getSocket(): Socket {
        return socket ?: throw IllegalStateException("Socket not initialized. Call initSocket() first.")
    }

    /** Disconnect the socket safely */
    fun disconnect() {
        socket?.disconnect()
        socket = null
        Log.d("SocketManager", "Socket disconnected and cleared")
    }

    /**
     * Emit a new message to Socket.IO
     * @param message: Message object to emit
     * @param receiverId: Optional, emits to single user
     * @param groupId: Optional, emits to group room
     */
    fun emitMessage(message: Message, receiverId: Int? = null, groupId: Int? = null) {
        val socket = getSocket()
        val json = JSONObject().apply {
            put("id", message.id)
            put("senderId", message.senderId)
            put("receiverId", message.receiverId)
            put("groupId", message.groupId)
            put("messageText", message.messageText)
            put("fileUrl", message.fileUrl)
            put("createdAt", message.createdAt)
            put("updatedAt", message.updatedAt)
        }

        receiverId?.let {
            socket.emit("sendMessage", json)
            Log.d("SocketManager", "Sent message to user $receiverId")
        }

        groupId?.let {
            // group room naming should match your backend (e.g., "group_1")
            socket.emit("sendMessage", json)
            Log.d("SocketManager", "Sent message to group $groupId")
        }
    }
}
