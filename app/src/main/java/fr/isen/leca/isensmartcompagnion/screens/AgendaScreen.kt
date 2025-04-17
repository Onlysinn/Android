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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

    // Etats pour ajout d'événement
    var showDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newLocation by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }

    val calendarState = rememberCalendarState(
        startMonth = displayedMonth.value.minusMonths(12),
        endMonth = displayedMonth.value.plusMonths(12),
        firstVisibleMonth = displayedMonth.value,
        firstDayOfWeek = java.time.DayOfWeek.MONDAY
    )

    LaunchedEffect(displayedMonth.value) {
        isLoading.value = true
        RetrofitClient.instance.getEvents().enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful) {
                    val list = response.body().orEmpty()
                    allSubscribedEvents.value = list.filter { getNotificationState(context, it.id) }
                    Log.d("AgendaDebug", "Chargé ${allSubscribedEvents.value.size} abonnements")
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

    val eventsForSelectedDate by remember(selectedDate.value, allSubscribedEvents.value) {
        mutableStateOf(
            allSubscribedEvents.value.filter { ev -> parseToLocalDate(ev.date) == selectedDate.value }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text("Agenda ISEN", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { displayedMonth.value = displayedMonth.value.minusMonths(1) }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Mois précédent", tint = Color(0xFFD32F2F))
            }
            Text(
                text = displayedMonth.value.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())
                    .replaceFirstChar { it.uppercaseChar() } + " ${displayedMonth.value.year}",
                fontSize = 18.sp
            )
            IconButton(onClick = { displayedMonth.value = displayedMonth.value.plusMonths(1) }) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "Mois suivant", tint = Color(0xFFD32F2F))
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalCalendar(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            state = calendarState,
            dayContent = { day ->
                if (day.date.month == displayedMonth.value.month && day.date.year == displayedMonth.value.year) {
                    val isSelected = day.date == selectedDate.value
                    val hasEvent = allSubscribedEvents.value.any { parseToLocalDate(it.date) == day.date }
                    Box(
                        Modifier.aspectRatio(1f).padding(4.dp)
                            .background(
                                when {
                                    isSelected -> Color(0xFFD32F2F)
                                    hasEvent -> Color(0xFFFFCDD2)
                                    else -> Color(0xFFEEEEEE)
                                }, shape = MaterialTheme.shapes.small
                            )
                            .clickable { selectedDate.value = day.date }, Alignment.Center
                    ) {
                        Text(day.date.dayOfMonth.toString(), color = if (isSelected) Color.White else Color.Black)
                    }
                } else Spacer(Modifier.aspectRatio(1f).padding(4.dp))
            }
        )

        Spacer(Modifier.height(16.dp))
        Text("Événements abonnés :", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

        if (isLoading.value) {
            CircularProgressIndicator(Modifier.padding(top = 16.dp))
        } else {
            if (eventsForSelectedDate.isEmpty()) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Aucun événement pour cette date.", Modifier.padding(top = 8.dp))
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { showDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Ajouter", tint = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text("Ajouter un événement", color = Color.White)
                    }
                }
            } else {
                LazyColumn {
                    items(eventsForSelectedDate) { ev ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F))) {
                            Column(Modifier.padding(12.dp)) {
                                Text(ev.title, fontSize = 16.sp, color = Color.White)
                                Text(ev.date, fontSize = 14.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nouvel événement") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Titre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newLocation,
                        onValueChange = { newLocation = it },
                        label = { Text("Lieu") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val created = Event(
                        id = UUID.randomUUID().toString(),
                        title = newTitle,
                        date = selectedDate.value.toString(),
                        location = newLocation,
                        category = "",
                        description = newDescription
                    )
                    RetrofitClient.instance.postEvent(created).enqueue(object : Callback<Event> {
                        override fun onResponse(call: Call<Event>, response: Response<Event>) {
                            if (response.isSuccessful) Toast.makeText(context, "Événement ajouté", Toast.LENGTH_SHORT).show()
                            else Toast.makeText(context, "Erreur ajout événement", Toast.LENGTH_SHORT).show()
                            showDialog = false
                            displayedMonth.value = displayedMonth.value // refresh
                        }
                        override fun onFailure(call: Call<Event>, t: Throwable) {
                            Toast.makeText(context, "Erreur réseau", Toast.LENGTH_SHORT).show()
                            showDialog = false
                        }
                    })
                }) { Text("Enregistrer") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Annuler") }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun parseToLocalDate(dateString: String): LocalDate? {
    runCatching { return LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE) }
    runCatching { return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME).toLocalDate() }
    runCatching { return OffsetDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate() }
    runCatching {
        val fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("fr"))
        return LocalDate.parse(dateString, fmt)
    }
    Log.w("AgendaDebug", "parseToLocalDate: format non reconnu pour '$dateString'")
    return null
}

fun getSubscribedEvents(context: Context, allEvents: List<Event>): List<Event> =
    allEvents.filter { getNotificationState(context, it.id) }
