package fr.isen.leca.isensmartcompagnion.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.leca.isensmartcompagnion.EventDetailActivity
import fr.isen.leca.isensmartcompagnion.api.RetrofitClient
import fr.isen.leca.isensmartcompagnion.models.Event
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun EventsScreen() {
    val eventsList = remember { mutableStateOf<List<Event>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Appel API avec callback
    LaunchedEffect(Unit) {
        val calledEvents = RetrofitClient.instance.getEvents()
        calledEvents.enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful) {
                    eventsList.value = response.body() ?: emptyList()
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
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(text = "Événements ISEN", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading.value) {
            CircularProgressIndicator()
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(eventsList.value) { event ->
                    EventItem(event = event, onClick = {
                        val intent = Intent(context, EventDetailActivity::class.java).apply {
                            putExtra("event", event)
                        }
                        context.startActivity(intent)
                    })
                }
            }
        }
    }
}

@Composable
fun EventItem(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick), // Rendre le card cliquable
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F))  // Fond rouge
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = event.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White  // Texte en blanc
            )
            Text(
                text = event.date,
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = Color.White  // Texte en blanc
            )
        }
    }
}
