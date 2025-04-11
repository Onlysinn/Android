package fr.isen.leca.isensmartcompagnion.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.leca.isensmartcompagnion.api.RetrofitClient
import fr.isen.leca.isensmartcompagnion.models.Event
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun AgendaScreen() {
    val context = LocalContext.current
    val eventsList = remember { mutableStateOf<List<Event>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }

    val staticCourses = listOf(
        "Maths - 9h à 10h",
        "Informatique - 10h15 à 11h45",
        "Physique - 13h30 à 15h",
        "Anglais - 15h15 à 17h"
    )

    // Récupération des événements depuis l’API
    LaunchedEffect(Unit) {
        RetrofitClient.instance.getEvents().enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful) {
                    val allEvents = response.body() ?: emptyList()
                    val subscribed = getSubscribedEvents(context, allEvents)
                    eventsList.value = subscribed
                } else {
                    Toast.makeText(context, "Erreur serveur", Toast.LENGTH_SHORT).show()
                }
                isLoading.value = false
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                Toast.makeText(context, "Erreur réseau", Toast.LENGTH_SHORT).show()
                isLoading.value = false
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Agenda de l’étudiant",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F) // Rouge ISEN
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Cours du jour",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
            items(staticCourses) { course ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Text(
                        text = course,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            "Événements abonnés",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading.value) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (eventsList.value.isEmpty()) {
                Text(
                    "Aucun événement abonné.",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(8.dp),
                    color = Color.Gray
                )
            } else {
                LazyColumn {
                    items(eventsList.value) { event ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C)), // Rouge foncé ISEN
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = event.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = event.date,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Fonction utilitaire pour filtrer les événements abonnés depuis les SharedPreferences
fun getSubscribedEvents(context: Context, allEvents: List<Event>): List<Event> {
    val prefs = context.getSharedPreferences("event_preferences", Context.MODE_PRIVATE)
    return allEvents.filter { event ->
        prefs.getBoolean(event.id, false)
    }
}

fun getFullAgenda(context: Context, allEvents: List<Event>): List<String> {
    val staticCourses = listOf(
        "Maths - 9h à 10h",
        "Informatique - 10h15 à 11h45",
        "Physique - 13h30 à 15h",
        "Anglais - 15h15 à 17h"
    )

    val subscribedEvents = getSubscribedEvents(context, allEvents).map {
        "${it.title} - ${it.date}"
    }

    return staticCourses + subscribedEvents
}
