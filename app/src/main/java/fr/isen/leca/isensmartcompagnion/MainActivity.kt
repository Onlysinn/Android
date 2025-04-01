package fr.isen.leca.isensmartcompagnion

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
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
import fr.isen.leca.isensmartcompagnion.models.Event
import fr.isen.leca.isensmartcompagnion.screens.AgendaScreen
import fr.isen.leca.isensmartcompagnion.screens.EventsScreen
import fr.isen.leca.isensmartcompagnion.screens.HistoryScreen
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
    var response by remember { mutableStateOf("Pose-moi une question !") }
    val context = LocalContext.current

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

        Text(
            text = response,
            fontSize = 20.sp,
            modifier = Modifier.padding(top = 16.dp).weight(1F),
            color = Color.Black
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0xFFEFEFEF), RoundedCornerShape(30.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = question,
                onValueChange = { question = it },
                textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, color = Color.Black),
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White, CircleShape)
                    .padding(16.dp),
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
                Image(
                    painter = painterResource(id = R.drawable.ic_send),
                    contentDescription = "Envoyer",
                    modifier = Modifier.fillMaxSize()
                )
            }
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
