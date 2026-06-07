package com.example.nutriscan.data.local.entity

import com.example.nutriscan.data.local.ChatMessageEntity
import com.example.nutriscan.data.local.ConversationEntity
import com.example.nutriscan.domain.model.ChatMessage
import com.example.nutriscan.domain.model.Conversation
import com.example.nutriscan.domain.model.UserRole

object ChatMapper {

    fun ConversationEntity.toDomain(): Conversation = Conversation(
        id                    = id,
        nutritionistId        = nutritionist_id,
        nutritionistName      = nutritionist_name,
        nutritionistSpecialty = nutritionist_specialty,
        userName              = user_name,
        createdAt             = created_at,
        lastMessage           = last_message,
        lastMessageAt         = last_message_at
    )

    fun ChatMessageEntity.toDomain(): ChatMessage = ChatMessage(
        id             = id,
        conversationId = conversation_id,
        sender         = UserRole.fromString(sender),
        content        = content,
        timestamp      = timestamp
    )
}
