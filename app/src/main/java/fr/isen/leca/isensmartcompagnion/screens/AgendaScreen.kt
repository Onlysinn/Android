package fr.isen.leca.isensmartcompagnion.screens

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
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
import fr.isen.leca.isensmartcompagnion.api.RetrofitClient
import fr.isen.leca.isensmartcompagnion.models.Event
import fr.isen.leca.isensmartcompagnion.notifications.getNotificationState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AgendaScreen() {
    val context = LocalContext.current
    val selectedDate = remember { mutableStateOf(LocalDate.now()) }
    val displayedMonth = remember { mutableStateOf(YearMonth.now()) }
    val allSubscribedEvents = remember { mutableStateOf<List<Event>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }

    val calendarState = rememberCalendarState(
        startMonth = displayedMonth.value.minusMonths(12),
        endMonth = displayedMonth.value.plusMonths(12),
        firstVisibleMonth = displayedMonth.value,
        firstDayOfWeek = java.time.DayOfWeek.MONDAY
    )

    // Recharge des événements souscrits à chaque changement de mois
    LaunchedEffect(displayedMonth.value) {
        isLoading.value = true
        RetrofitClient.instance.getEvents().enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful) {
                    val list = response.body().orEmpty()
                    allSubscribedEvents.value = list.filter { ev ->
                        getNotificationState(context, ev.id)
                    }
                    Log.d("AgendaDebug", "Abonnés chargés (${allSubscribedEvents.value.size}): " +
                            allSubscribedEvents.value.joinToString { ev -> "${ev.id}@${ev.date}" })
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
        calendarState.animateScrollToMonth(displayedMonth.value)
    }

    // Filtrer les événements pour la date sélectionnée
    val eventsForSelectedDate by remember(selectedDate.value, allSubscribedEvents.value) {
        mutableStateOf(
            allSubscribedEvents.value.filter { ev ->
                val d = parseToLocalDate(ev.date)
                Log.d("AgendaDebug", "Parsing ${ev.date} -> $d")
                d == selectedDate.value
            }
        )
    }

    // Debug date selection
    LaunchedEffect(selectedDate.value) {
        Log.d("AgendaDebug", "Date sélectionnée: ${selectedDate.value}, événements trouvés: ${eventsForSelectedDate.size}")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        Text(
            "Agenda ISEN",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F)
        )
        Spacer(Modifier.height(16.dp))

        // Navigation mois
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton({ displayedMonth.value = displayedMonth.value.minusMonths(1) }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Mois précédent", tint = Color(0xFFD32F2F))
            }
            Text(
                text = displayedMonth.value.month
                    .getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())
                    .replaceFirstChar { it.uppercaseChar() } + " ${displayedMonth.value.year}",
                fontSize = 18.sp
            )
            IconButton({ displayedMonth.value = displayedMonth.value.plusMonths(1) }) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "Mois suivant", tint = Color(0xFFD32F2F))
            }
        }

        Spacer(Modifier.height(8.dp))

        // Calendrier
        HorizontalCalendar(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            state = calendarState,
            dayContent = { day ->
                val currentMonth = displayedMonth.value
                if (day.date.month == currentMonth.month && day.date.year == currentMonth.year) {
                    val isSelected = day.date == selectedDate.value
                    val hasEvent = allSubscribedEvents.value.any {
                        parseToLocalDate(it.date) == day.date
                    }
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(4.dp)
                            .background(
                                when {
                                    isSelected -> Color(0xFFD32F2F)
                                    hasEvent  -> Color(0xFFFFCDD2)
                                    else      -> Color(0xFFEEEEEE)
                                },
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable { selectedDate.value = day.date },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            day.date.dayOfMonth.toString(),
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }
                } else {
                    Spacer(Modifier.aspectRatio(1f).padding(4.dp))
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "Événements abonnés :",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        if (isLoading.value) {
            CircularProgressIndicator(Modifier.padding(top = 16.dp))
        } else {
            if (eventsForSelectedDate.isEmpty()) {
                Text("Aucun événement pour cette date.", Modifier.padding(top = 8.dp))
            } else {
                LazyColumn {
                    items(eventsForSelectedDate) { event ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Column(Modifier.padding(12.dp)) {
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

// Helper étendu pour parser divers formats de date
@RequiresApi(Build.VERSION_CODES.O)
fun parseToLocalDate(dateString: String): LocalDate? {
    // ISO_DATE (yyyy-MM-dd)
    runCatching { return LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE) }
    // ISO_DATE_TIME (yyyy-MM-dd'T'HH:mm:ss)
    runCatching { return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME).toLocalDate() }
    // ISO_OFFSET_DATE_TIME (yyyy-MM-dd'T'HH:mm:ssZ)
    runCatching { return OffsetDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate() }
    // Format français, ex. "24 septembre 2024"
    runCatching {
        val fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("fr"))
        return LocalDate.parse(dateString, fmt)
    }
    Log.w("AgendaDebug", "parseToLocalDate: format non reconnu pour '$dateString'")
    return null
}

// Réintroduction de getSubscribedEvents pour compatibilité avec MainActivity
fun getSubscribedEvents(context: Context, allEvents: List<Event>): List<Event> =
    allEvents.filter { ev ->
        getNotificationState(context, ev.id)
    }
