package com.todolist.app.data.repository

import com.todolist.app.data.local.HolidayDao
import com.todolist.app.data.local.HolidayEntity
import com.todolist.app.data.remote.BrasilApiService
import com.todolist.app.data.remote.HolidayDto
import com.todolist.app.domain.model.Holiday
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.time.LocalDate

/**
 * Feriados nacionais: a tela sempre lê do banco local (funciona offline);
 * a rede só é usada para preencher o cache de um ano que ainda não foi baixado.
 */
class HolidayRepository(
    private val dao: HolidayDao,
    private val api: BrasilApiService,
    /** Tempo máximo que a tela espera pela BrasilAPI, incluindo a consulta de DNS. */
    private val deadlineMillis: Long = 15_000,
) {
    private val mutex = Mutex()

    /** Escopo próprio para a chamada de rede: se ela travar, quem espera desiste sem ficar preso a ela. */
    private val networkScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun observe(year: Int): Flow<List<Holiday>> =
        dao.observeYear(year).map { list -> list.map { Holiday(it.date, it.name) } }

    /** Baixa os feriados do ano se ainda não estiverem salvos. Retorna falha com mensagem amigável. */
    suspend fun refresh(year: Int, force: Boolean = false): Result<Unit> = mutex.withLock {
        try {
            if (!force && dao.countYear(year) > 0) return Result.success(Unit)
            val holidays = fetchWithDeadline(year).mapNotNull { dto ->
                // Ignora itens com data inválida em vez de falhar o ano inteiro.
                runCatching { LocalDate.parse(dto.date) }.getOrNull()?.let { HolidayEntity(it, year, dto.name) }
            }
            dao.replaceYear(year, holidays)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(HolidayException(messageFor(e), e))
        }
    }

    /**
     * O timeout do Ktor não interrompe a consulta de DNS (é uma chamada bloqueante do sistema):
     * com o DNS travado, a requisição ficava parada sem erro nem feriados na tela.
     * Aqui a espera tem um prazo garantido; a chamada travada é cancelada e descartada.
     */
    private suspend fun fetchWithDeadline(year: Int): List<HolidayDto> {
        val call = networkScope.async { api.getHolidays(year) }
        return try {
            withTimeout(deadlineMillis) { call.await() }
        } catch (e: TimeoutCancellationException) {
            call.cancel()
            throw DeadlineExceededException()
        }
    }

    class DeadlineExceededException : Exception("Tempo limite excedido ao consultar a BrasilAPI")

    class HolidayException(message: String, cause: Throwable) : Exception(message, cause)

    companion object {
        fun messageFor(e: Throwable): String = when (e) {
            is HttpRequestTimeoutException, is DeadlineExceededException -> "A BrasilAPI demorou para responder."
            is ClientRequestException -> "Feriados indisponíveis para este ano."
            is ServerResponseException -> "A BrasilAPI está fora do ar no momento."
            is SerializationException -> "Resposta inesperada da BrasilAPI."
            is IOException -> "Sem conexão com a internet."
            else -> "Não foi possível carregar os feriados."
        }
    }
}
