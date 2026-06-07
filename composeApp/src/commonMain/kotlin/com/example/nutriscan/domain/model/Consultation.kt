package com.example.nutriscan.domain.model

/**
 * A persisted conversation thread between the local user and a nutritionist.
 */
data class Conversation(
    val id: Long,
    val nutritionistId: String,
    val nutritionistName: String,
    val nutritionistSpecialty: String,
    val userName: String,
    val createdAt: Long,
    val lastMessage: String,
    val lastMessageAt: Long
)

/**
 * A single chat message. [sender] is the role that authored the message.
 */
data class ChatMessage(
    val id: Long,
    val conversationId: Long,
    val sender: UserRole,
    val content: String,
    val timestamp: Long
)
