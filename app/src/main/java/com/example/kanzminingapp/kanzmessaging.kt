package com.example.kanzminingapp

import androidx.compose.runtime.mutableStateListOf

data class KanzChatMessage(
    val productName: String,
    val message: String,
    val isFromUser: Boolean
)

object KanzChatStore {
    val messages = mutableStateListOf<KanzChatMessage>()

    fun sendMessage(productName: String, message: String) {
        val cleanedMessage = message.trim()

        if (productName.isNotBlank() && cleanedMessage.isNotBlank()) {
            messages.add(
                KanzChatMessage(
                    productName = productName,
                    message = cleanedMessage,
                    isFromUser = true
                )
            )
        }
    }
}