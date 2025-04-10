package fr.isen.leca.isensmartcompagnion.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.isen.leca.isensmartcompagnion.database.ChatDatabase
import fr.isen.leca.isensmartcompagnion.database.ChatMessage
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val chatDao = ChatDatabase.getDatabase(context).chatDao()
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    var coroutineScope = rememberCoroutineScope()
    // Charger les messages depuis la base de données
    LaunchedEffect(Unit) {
        chatMessages.addAll(chatDao.getAllMessages())
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // Titre "Historique des conversations"
        Text(
            text = "Historique des conversations",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Bouton pour effacer l'historique complet
        Button(

            onClick = {
                // Effacer tous les messages
                coroutineScope.launch {
                    chatDao.deleteAllMessages()
                    chatMessages.clear()  // Vide la liste en mémoire
                    Toast.makeText(context, "Historique effacé", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Effacer l'historique")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Affichage des messages de l'historique avec un bouton de suppression pour chaque couple
        LazyColumn(modifier = Modifier.fillMaxSize().weight(1F)) {
            items(chatMessages) { message ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color(0xFFEFEFEF), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Vous : ${message.question}",
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "Assistant : ${message.response}",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )

                    // Bouton pour supprimer un couple question/réponse spécifique
                    Spacer(modifier = Modifier.height(8.dp))
                    IconButton(
                        onClick = {
                            // Supprimer le message spécifique
                            coroutineScope.launch {
                                chatDao.deleteMessage(message.id)
                                chatMessages.remove(message)  // Supprime de la liste en mémoire
                                Toast.makeText(context, "Message supprimé", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}



