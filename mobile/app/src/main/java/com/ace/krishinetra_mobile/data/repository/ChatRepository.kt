package com.ace.krishinetra_mobile.data.repository

import com.ace.krishinetra_mobile.data.local.ChatMessageDao
import com.ace.krishinetra_mobile.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val dao: ChatMessageDao) {
    fun getAllMessages(): Flow<List<ChatMessage>> = dao.getAllMessages()

    suspend fun sendMessage(text: String, isUser: Boolean) {
        dao.insertMessage(ChatMessage(text = text, isUser = isUser))
    }

    suspend fun deleteAllMessages() {
        dao.deleteAllMessages()
    }
}
