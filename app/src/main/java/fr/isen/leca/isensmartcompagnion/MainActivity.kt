package fr.isen.leca.isensmartcompagnion

import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import fr.isen.leca.isensmartcompagnion.ui.theme.ISENSmartCompagnionTheme

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

    Column(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "home", modifier = Modifier.weight(1f)) {
            composable("home") { MainScreen(navController) }
            composable("events") { EventsScreen(navController) }
            composable("agenda") { AgendaScreen(navController) }
            composable("history") { HistoryScreen(navController) }
            composable("eventDetail") { EventDetailScreen() }
        }

        BottomNavigationBar(navController = navController)
    }
}

@Composable
fun MainScreen(navController: NavController) {
    var question by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("Pose-moi une question !") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "ISEN", fontSize = 80.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
        Text(text = "Smart Companion", fontSize = 24.sp, modifier = Modifier.padding(top = 8.dp))
        Text(text = response, fontSize = 20.sp, modifier = Modifier.padding(top = 16.dp).weight(1F))

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
                .background(Color(0xFFEFEFEF), RoundedCornerShape(30.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = question,
                onValueChange = { question = it },
                textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, color = Color.Black),
                modifier = Modifier.weight(1f).background(Color.White, CircleShape).padding(16.dp),
                placeholder = { Text("Pose ta question...") }
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    Toast.makeText(context, "Question envoyée", Toast.LENGTH_SHORT).show()
                    response = "Vous avez demandé : $question"
                    question = ""
                },
                modifier = Modifier.size(48.dp).background(Color(0xFFD32F2F), CircleShape)
            ) {
                Image(painter = painterResource(id = R.drawable.ic_send), contentDescription = "Envoyer", modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun EventsScreen(navController: NavController) {
    val eventsList = listOf(
        "Soirée BDE",
        "Gala",
        "Journée de cohésion",
        "Hackathon ISEN",
        "Conférence IA",
        "Tournoi eSport",
        "Forum des entreprises"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Événements ISEN", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(eventsList) { event ->
                EventItem(eventName = event, onClick = { navController.navigate("eventDetail") })
            }
        }
    }
}

@Composable
fun EventItem(eventName: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Filled.Event, contentDescription = "Événement", tint = Color.White)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = eventName, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.White)
        }
    }
}

@Composable
fun EventDetailScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Détails de l'Événement", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Ici s'afficheront les détails de l'événement sélectionné.", fontSize = 18.sp)
    }
}

@Composable
fun AgendaScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Agenda", fontSize = 24.sp)
    }
}

@Composable
fun HistoryScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Historique", fontSize = 24.sp)
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar(
        contentColor = Color.White,
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        NavigationBarItem(
            selected = false, onClick = { navController.navigate("home") },
            icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "Accueil") },
            label = { Text("Accueil") }
        )
        NavigationBarItem(
            selected = false, onClick = { navController.navigate("events") },
            icon = { Icon(imageVector = Icons.Filled.Event, contentDescription = "Événements") },
            label = { Text("Événements") }
        )
        NavigationBarItem(
            selected = false, onClick = { navController.navigate("agenda") },
            icon = { Icon(imageVector = Icons.Filled.CalendarToday, contentDescription = "Agenda") },
            label = { Text("Agenda") }
        )
        NavigationBarItem(
            selected = false, onClick = { navController.navigate("history") },
            icon = { Icon(imageVector = Icons.Filled.History, contentDescription = "Historique") },
            label = { Text("Historique") }
        )
    }
}
