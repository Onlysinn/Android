package fr.isen.leca.isensmartcompagnion.database

import androidx.room.*

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(chatMessage: ChatMessage)

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    suspend fun getAllMessages(): List<ChatMessage>

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessage(id: Int)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()
}
