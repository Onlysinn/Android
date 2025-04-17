package fr.isen.leca.isensmartcompagnion.api

import fr.isen.leca.isensmartcompagnion.models.Event
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface EventApiService {
    @GET("events.json")  // URL à partir de la base donnée
    fun getEvents(): Call<List<Event>>  // Retourne un map des événements avec l'ID comme clé
    @POST("events.json")
    fun postEvent(@Body event: Event): Call<Event>
}
