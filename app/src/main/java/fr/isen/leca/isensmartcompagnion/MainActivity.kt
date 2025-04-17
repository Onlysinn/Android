package fr.isen.leca.isensmartcompagnion

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import fr.isen.leca.isensmartcompagnion.api.RetrofitClient
import fr.isen.leca.isensmartcompagnion.database.ChatDatabase
import fr.isen.leca.isensmartcompagnion.database.ChatMessage
import fr.isen.leca.isensmartcompagnion.models.Event
import fr.isen.leca.isensmartcompagnion.models.Gemini
import fr.isen.leca.isensmartcompagnion.screens.AgendaScreen
import fr.isen.leca.isensmartcompagnion.screens.EventsScreen
import fr.isen.leca.isensmartcompagnion.screens.HistoryScreen
import fr.isen.leca.isensmartcompagnion.screens.getSubscribedEvents
import fr.isen.leca.isensmartcompagnion.ui.theme.ISENSmartCompagnionTheme
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ISENSmartCompagnionTheme {
                NavigationApp()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") { MainScreen() }
                composable("events") { EventsScreen() }
                composable("agenda") { AgendaScreen() }
                composable("history") { HistoryScreen() }
            }
        }
    }
}

@Composable
fun MainScreen() {
    var question by remember { mutableStateOf("") }
    val responses = remember { mutableStateListOf<String>() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = ChatDatabase.getDatabase(context)
    val chatDao = database.chatDao()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "ISEN",
            fontSize = 80.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F)
        )
        Text(
            "Smart Companion",
            fontSize = 24.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(responses) { response ->
                Text(
                    text = response,
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = question,
                onValueChange = { question = it },
                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                modifier = Modifier.weight(1f),
                placeholder = { Text("Pose ta question...") }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Bouton d'envoi plus ergonomique
            IconButton(
                onClick = {
                    val trimmed = question.trim()
                    if (trimmed.isNotEmpty()) {
                        Toast.makeText(context, "Question envoyée", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch {
                            val response = Gemini.getResponse(trimmed)
                            responses.add("Vous : $trimmed")
                            responses.add("Assistant : $response")
                            chatDao.insertMessage(ChatMessage(question = trimmed, response = response))
                            question = ""
                        }
                    } else {
                        Toast.makeText(context, "Veuillez poser une question valide", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFD32F2F), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Envoyer",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Button(
            onClick = {
                Toast.makeText(context, "Résumé de l’agenda en cours...", Toast.LENGTH_SHORT).show()
                RetrofitClient.instance.getEvents().enqueue(object : Callback<List<Event>> {
                    override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                        if (response.isSuccessful) {
                            val allEvents = response.body() ?: emptyList()
                            val subscribedEvents = getSubscribedEvents(context, allEvents)
                            val staticCourses = listOf(
                                "Maths - 9h à 10h",
                                "Informatique - 10h15 à 11h45",
                                "Physique - 13h30 à 15h",
                                "Anglais - 15h15 à 17h"
                            )
                            val summary = buildString {
                                append("Voici ton agenda du jour :\n\n")
                                append("📚 Cours :\n")
                                staticCourses.forEach { append("- $it\n") }
                                append("\n📅 Événements abonnés :\n")
                                if (subscribedEvents.isEmpty()) append("Aucun événement.\n")
                                else subscribedEvents.forEach { append("- ${it.title} (${it.date})\n") }
                            }
                            coroutineScope.launch {
                                val aiResponse = Gemini.getResponse(summary)
                                responses.add("Résumé IA : $aiResponse")
                                chatDao.insertMessage(ChatMessage(question = "Résumé agenda", response = aiResponse))
                            }
                        } else {
                            Toast.makeText(context, "Erreur lors de la récupération des événements", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                        Toast.makeText(context, "Erreur réseau", Toast.LENGTH_SHORT).show()
                    }
                })
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("📖 Résumer mon agenda")
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("home") },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Accueil") },
            label = { Text("Accueil") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("events") },
            icon = { Icon(Icons.Filled.Event, contentDescription = "Événements") },
            label = { Text("Événements") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("agenda") },
            icon = { Icon(Icons.Filled.CalendarToday, contentDescription = "Agenda") },
            label = { Text("Agenda") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("history") },
            icon = { Icon(Icons.Filled.History, contentDescription = "Historique") },
            label = { Text("Historique") }
        )
    }
}
