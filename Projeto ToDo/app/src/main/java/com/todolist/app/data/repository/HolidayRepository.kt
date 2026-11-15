package com.todolist.app.data.repository

import com.todolist.app.data.local.HolidayDao
import com.todolist.app.data.local.HolidayEntity
import com.todolist.app.data.remote.BrasilApiService
import com.todolist.app.domain.model.Holiday
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
) {
    private val mutex = Mutex()

    fun observe(year: Int): Flow<List<Holiday>> =
        dao.observeYear(year).map { list -> list.map { Holiday(it.date, it.name) } }

    /** Baixa os feriados do ano se ainda não estiverem salvos. Retorna falha com mensagem amigável. */
    suspend fun refresh(year: Int, force: Boolean = false): Result<Unit> = mutex.withLock {
        try {
            if (!force && dao.countYear(year) > 0) return Result.success(Unit)
            val holidays = api.getHolidays(year).mapNotNull { dto ->
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

    class HolidayException(message: String, cause: Throwable) : Exception(message, cause)

    companion object {
        fun messageFor(e: Throwable): String = when (e) {
            is HttpRequestTimeoutException -> "A BrasilAPI demorou para responder."
            is ClientRequestException -> "Feriados indisponíveis para este ano."
            is ServerResponseException -> "A BrasilAPI está fora do ar no momento."
            is SerializationException -> "Resposta inesperada da BrasilAPI."
            is IOException -> "Sem conexão com a internet."
            else -> "Não foi possível carregar os feriados."
        }
    }
}
