package fr.isen.leca.isensmartcompagnion

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import fr.isen.leca.isensmartcompagnion.models.Event
import fr.isen.leca.isensmartcompagnion.notifications.getNotificationState
import fr.isen.leca.isensmartcompagnion.notifications.saveNotificationState


class EventDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Demande la permission pour les notifications si nécessaire
        checkAndRequestPermission(this)

        // Crée le canal de notifications (à faire une seule fois)
        createNotificationChannel(this)

        val event: Event? = intent.getParcelableExtra("event")

        setContent {
            event?.let {
                EventDetailScreen(event = it, context = this)
            }
        }
    }

    // Gestion de la réponse à la permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission de notifications accordée", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission de notifications refusée", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun EventDetailScreen(event: Event, context: Context) {
    var isSubscribed by remember { mutableStateOf(getNotificationState(context, event.id)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = event.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Date: ${event.date}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Light
        )
        Text(
            text = "Lieu: ${event.location}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Light
        )
        Text(
            text = "Catégorie: ${event.category}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Light
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = event.description,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Icône de cloche
        IconButton(onClick = {
            isSubscribed = !isSubscribed
            saveNotificationState(context, event.id, isSubscribed)

            if (isSubscribed) {
                Toast.makeText(context, "Abonné aux notifications!", Toast.LENGTH_SHORT).show()

                // Envoie de notification après 10 secondes
                Handler(Looper.getMainLooper()).postDelayed({
                    sendEventNotification(context, event.title)
                }, 10_000L)

            } else {
                Toast.makeText(context, "Désabonné des notifications.", Toast.LENGTH_SHORT).show()
            }
        }) {
            val icon = if (isSubscribed) {
                painterResource(id = R.drawable.ic_bell_on)
            } else {
                painterResource(id = R.drawable.ic_bell_off)
            }
            Icon(painter = icon, contentDescription = "Notification", modifier = Modifier.size(24.dp))
        }
    }
}

// Fonction pour demander la permission POST_NOTIFICATIONS si Android 13+
fun checkAndRequestPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }
    }
}
