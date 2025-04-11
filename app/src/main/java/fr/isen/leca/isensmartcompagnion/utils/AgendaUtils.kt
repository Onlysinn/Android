package fr.isen.leca.isensmartcompagnion.utils

import android.content.Context
import fr.isen.leca.isensmartcompagnion.models.Event

fun getStaticCourses(): List<String> {
    return listOf(
        "Maths - 9h à 10h",
        "Informatique - 10h15 à 11h45",
        "Physique - 13h30 à 15h",
        "Anglais - 15h15 à 17h"
    )
}

fun getSubscribedEvents(context: Context, allEvents: List<Event>): List<Event> {
    val prefs = context.getSharedPreferences("event_preferences", Context.MODE_PRIVATE)
    return allEvents.filter { event -> prefs.getBoolean(event.id, false) }
}

fun summarizeAgenda(courses: List<String>, events: List<Event>): String {
    val courseSummary = if (courses.isEmpty()) "Aucun cours prévu." else "Cours du jour :\n" + courses.joinToString("\n")
    val eventSummary = if (events.isEmpty()) "Aucun événement à venir." else "Événements à venir :\n" + events.joinToString("\n") {
        "- ${it.title} (${it.date})"
    }

    return "$courseSummary\n\n$eventSummary"
}
