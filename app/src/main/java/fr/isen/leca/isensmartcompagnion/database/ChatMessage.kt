package fr.isen.leca.isensmartcompagnion.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis()
)
