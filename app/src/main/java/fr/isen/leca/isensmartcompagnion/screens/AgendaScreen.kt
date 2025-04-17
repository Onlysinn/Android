package fr.isen.leca.isensmartcompagnion.screens

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.yearMonth
import fr.isen.leca.isensmartcompagnion.api.RetrofitClient
import fr.isen.leca.isensmartcompagnion.models.Event
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AgendaScreen() {
    val context = LocalContext.current
    val isLoading = remember { mutableStateOf(true) }
    val allSubscribedEvents = remember { mutableStateOf<List<Event>>(emptyList()) }
    val selectedDate = remember { mutableStateOf(LocalDate.now()) }

    val staticCourses = listOf(
        "Maths - 9h à 10h",
        "Informatique - 10h15 à 11h45",
        "Physique - 13h30 à 15h",
        "Anglais - 15h15 à 17h"
    )

    // Appel API
    LaunchedEffect(Unit) {
        RetrofitClient.instance.getEvents().enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful) {
                    val allEvents = response.body() ?: emptyList()
                    allSubscribedEvents.value = getSubscribedEvents(context, allEvents)
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

    val eventsForSelectedDate = remember(selectedDate.value, allSubscribedEvents.value) {
        allSubscribedEvents.value.filter { event ->
            try {
                LocalDate.parse(event.date, DateTimeFormatter.ISO_DATE) == selectedDate.value
            } catch (e: Exception) {
                false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text("Agenda ISEN", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))

        Spacer(modifier = Modifier.height(16.dp))

        // Calendrier interactif
        val currentMonth = remember { YearMonth.now() }
        val startMonth = remember { currentMonth.minusMonths(3) }
        val endMonth = remember { currentMonth.plusMonths(3) }

        HorizontalCalendar(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            state = rememberCalendarState(
                startMonth = startMonth,
                endMonth = endMonth,
                firstDayOfWeek = DayOfWeek.MONDAY
            ),
            dayContent = { day ->
                val isSelected = day.date == selectedDate.value
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp)
                        .background(
                            if (isSelected) Color(0xFFD32F2F) else Color(0xFFEEEEEE),
                            shape = MaterialTheme.shapes.small
                        )
                        .clickable { selectedDate.value = day.date },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.date.dayOfMonth.toString(),
                        color = if (isSelected) Color.White else Color.Black
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Cours du jour :", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        LazyColumn {
            items(staticCourses) { course ->
                Text(course, modifier = Modifier.padding(4.dp), fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Événements abonnés :", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)

        if (isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        } else {
            if (eventsForSelectedDate.isEmpty()) {
                Text("Aucun événement pour cette date.", modifier = Modifier.padding(top = 8.dp))
            } else {
                LazyColumn {
                    items(eventsForSelectedDate) { event ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(event.title, fontSize = 16.sp, color = Color.White)
                                Text(event.date, fontSize = 14.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getSubscribedEvents(context: Context, allEvents: List<Event>): List<Event> {
    val prefs = context.getSharedPreferences("event_preferences", Context.MODE_PRIVATE)
    return allEvents.filter { event -> prefs.getBoolean(event.id, false) }
}
