package com.example.nutriscan.domain.repository

import com.example.nutriscan.domain.model.ChatMessage
import com.example.nutriscan.domain.model.Conversation
import com.example.nutriscan.domain.model.Nutritionist
import com.example.nutriscan.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

/**
 * Local, dummy consultation backend. Nutritionists are a static catalog;
 * conversations & messages are persisted in SQLDelight so they survive restarts.
 */
interface ConsultationRepository {

    /** Static catalog of available nutritionists. */
    fun getNutritionists(): List<Nutritionist>

    /** Look up a nutritionist by id. */
    fun getNutritionist(id: String): Nutritionist?

    /** Observe every conversation (used by the nutritionist dashboard). */
    fun observeConversations(): Flow<List<Conversation>>

    /** Observe conversations belonging to a specific user. */
    fun observeConversationsForUser(userName: String): Flow<List<Conversation>>

    /** Observe the messages inside a conversation, oldest first. */
    fun observeMessages(conversationId: Long): Flow<List<ChatMessage>>

    /** Fetch a single conversation (one-shot). */
    suspend fun getConversation(conversationId: Long): Conversation?

    /**
     * Return the existing conversation between [userName] and [nutritionist],
     * creating it (with a welcome message) if it does not exist yet.
     */
    suspend fun startOrGetConversation(nutritionist: Nutritionist, userName: String): Long

    /** Append a message authored by [sender]. */
    suspend fun sendMessage(conversationId: Long, sender: UserRole, content: String)
}
