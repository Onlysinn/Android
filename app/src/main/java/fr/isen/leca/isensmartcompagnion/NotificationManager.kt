package fr.isen.leca.isensmartcompagnion

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat


fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "event_channel",
            "Événements",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications pour les événements suivis"
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun sendEventNotification(context: Context, eventTitle: String) {
    val builder = NotificationCompat.Builder(context, "event_channel")
        .setSmallIcon(R.drawable.ic_bell_on) // Utilise ton icône
        .setContentTitle("Événement à venir")
        .setContentText("Vous avez un événement : $eventTitle")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    val notificationManager = NotificationManagerCompat.from(context)

    // ✅ Vérifie que la permission est accordée AVANT de notifier
    if (ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    } else {
        // 👇 Optionnel : log ou feedback utilisateur si tu veux
        // Toast.makeText(context, "Permission notifications manquante", Toast.LENGTH_SHORT).show()
    }
}
