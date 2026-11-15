package com.todolist.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Formato de cada item retornado por GET /api/feriados/v1/{ano}. */
@Serializable
data class HolidayDto(
    val date: String,
    val name: String,
    val type: String = "national",
)

/**
 * Cliente da BrasilAPI (gratuita, sem chave): https://brasilapi.com.br/docs#tag/Feriados-Nacionais
 * O engine pode ser trocado nos testes por um MockEngine.
 */
class BrasilApiService(engine: HttpClientEngine = Android.create()) {

    private val client = HttpClient(engine) {
        // Respostas fora de 2xx viram exceção (ClientRequestException / ServerResponseException).
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000
            connectTimeoutMillis = 10_000
        }
        defaultRequest { url("https://brasilapi.com.br/api/") }
    }

    suspend fun getHolidays(year: Int): List<HolidayDto> = client.get("feriados/v1/$year").body()
}
