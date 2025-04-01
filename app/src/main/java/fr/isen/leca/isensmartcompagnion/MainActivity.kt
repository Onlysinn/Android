package fr.isen.leca.isensmartcompagnion

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import fr.isen.leca.isensmartcompagnion.database.ChatDatabase
import fr.isen.leca.isensmartcompagnion.database.ChatMessage
import fr.isen.leca.isensmartcompagnion.models.Gemini
import fr.isen.leca.isensmartcompagnion.screens.AgendaScreen
import fr.isen.leca.isensmartcompagnion.screens.EventsScreen
import fr.isen.leca.isensmartcompagnion.screens.HistoryScreen
import fr.isen.leca.isensmartcompagnion.ui.theme.ISENSmartCompagnionTheme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ISENSmartCompagnionTheme {
                NavigationApp()
            }
        }
    }
}

@Composable
fun NavigationApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) } // Ajout de la barre de navigation dans le Scaffold
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) { // Gestion des paddings pour éviter que le contenu ne soit caché par la bottomBar
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
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ISEN",
            fontSize = 80.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F)
        )

        Text(
            text = "Smart Companion",
            fontSize = 24.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Liste des réponses de l'IA
        LazyColumn(modifier = Modifier.fillMaxSize().weight(1F)) {
            items(responses) { response ->
                Text(
                    text = response,
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 4.dp)
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
                textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, color = Color.Black),
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                placeholder = { Text("Pose ta question...") }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_send),
                contentDescription = "Envoyer",
                modifier = Modifier.clickable {
                    val trimmedQuestion = question.trim()

                    if (trimmedQuestion.isNotEmpty()) {
                        Toast.makeText(context, "Question envoyée", Toast.LENGTH_SHORT).show()

                        // Appel à Gemini pour récupérer la réponse
                        coroutineScope.launch {
                            val response = Gemini.getResponse(trimmedQuestion)
                            responses.add("Vous : $trimmedQuestion") // Ajoute la question de l'utilisateur
                            responses.add("Assistant : $response") // Ajoute la réponse de l'IA

                            // Sauvegarde la question et la réponse dans la base de données
                            val chatMessage = ChatMessage(
                                question = trimmedQuestion,
                                response = response
                            )
                            chatDao.insertMessage(chatMessage)

                            // Réinitialisation du champ de saisie
                            question = ""
                        }
                    } else {
                        Toast.makeText(context, "Veuillez poser une question valide", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar(
        contentColor = Color.White,
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("home") },
            icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "Accueil") },
            label = { Text("Accueil") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("events") },
            icon = { Icon(imageVector = Icons.Filled.Event, contentDescription = "Événements") },
            label = { Text("Événements") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("agenda") },
            icon = { Icon(imageVector = Icons.Filled.CalendarToday, contentDescription = "Agenda") },
            label = { Text("Agenda") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("history") },
            icon = { Icon(imageVector = Icons.Filled.History, contentDescription = "Historique") },
            label = { Text("Historique") }
        )
    }
}
