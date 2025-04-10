package fr.isen.leca.isensmartcompagnion.notifications

import android.content.Context
import android.content.SharedPreferences

private const val PREF_NAME = "event_preferences"

fun saveNotificationState(context: Context, eventId: String, isSubscribed: Boolean) {
    val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(eventId, isSubscribed).apply()
}

fun getNotificationState(context: Context, eventId: String): Boolean {
    val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(eventId, false)
}
