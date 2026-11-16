package com.todolist.app

import com.todolist.app.data.local.HolidayDao
import com.todolist.app.data.local.HolidayEntity
import com.todolist.app.data.remote.BrasilApiService
import com.todolist.app.data.repository.HolidayRepository
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

/** Testa o consumo da BrasilAPI com um servidor simulado (MockEngine) e um banco falso em memória. */
class HolidayRepositoryTest {

    private class FakeHolidayDao : HolidayDao() {
        val rows = MutableStateFlow<List<HolidayEntity>>(emptyList())
        override fun observeYear(year: Int): Flow<List<HolidayEntity>> = rows.map { r -> r.filter { it.year == year } }
        override suspend fun countYear(year: Int) = rows.value.count { it.year == year }
        override suspend fun deleteYear(year: Int) { rows.value = rows.value.filterNot { it.year == year } }
        override suspend fun insertAll(holidays: List<HolidayEntity>) { rows.value = rows.value + holidays }
    }

    private val json = """
        [{"date":"2026-11-15","name":"Proclamação da República","type":"national"},
         {"date":"2026-12-25","name":"Natal","type":"national","extra":"ignorado"}]
    """.trimIndent()

    private fun repo(dao: HolidayDao, engine: MockEngine) = HolidayRepository(dao, BrasilApiService(engine))

    @Test
    fun `baixa salva e expoe os feriados do ano`() = runBlocking {
        var calls = 0
        val engine = MockEngine { request ->
            calls++
            assertEquals("/api/feriados/v1/2026", request.url.encodedPath)
            respond(json, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val dao = FakeHolidayDao()
        val repository = repo(dao, engine)

        assertTrue(repository.refresh(2026).isSuccess)
        val holidays = repository.observe(2026).first()
        assertEquals(2, holidays.size)
        assertEquals(LocalDate.of(2026, 11, 15), holidays.first().date)
        assertEquals("Proclamação da República", holidays.first().name)

        // Ano já em cache: não chama a rede de novo.
        assertTrue(repository.refresh(2026).isSuccess)
        assertEquals(1, calls)
    }

    @Test
    fun `erro do servidor vira mensagem amigavel e nao apaga o cache`() = runBlocking {
        val dao = FakeHolidayDao().apply { rows.value = listOf(HolidayEntity(LocalDate.of(2026, 1, 1), 2026, "Ano novo")) }
        val result = repo(dao, MockEngine { respondError(HttpStatusCode.InternalServerError) }).refresh(2026, force = true)
        assertTrue(result.isFailure)
        assertEquals("A BrasilAPI está fora do ar no momento.", result.exceptionOrNull()?.message)
        assertEquals(1, dao.rows.value.size)
    }

    @Test
    fun `ano inexistente na api e sem internet`() = runBlocking {
        val notFound = repo(FakeHolidayDao(), MockEngine { respondError(HttpStatusCode.NotFound) }).refresh(1800)
        assertEquals("Feriados indisponíveis para este ano.", notFound.exceptionOrNull()?.message)

        val offline = repo(FakeHolidayDao(), MockEngine { throw IOException("sem rede") }).refresh(2026)
        assertEquals("Sem conexão com a internet.", offline.exceptionOrNull()?.message)
    }

    @Test
    fun `chamada travada (como DNS sem resposta) desiste no prazo com mensagem`() = runBlocking {
        // Thread.sleep bloqueia a thread e ignora o cancelamento, como a consulta de DNS do sistema.
        val stuck = MockEngine { Thread.sleep(3_000); respond(json, HttpStatusCode.OK) }
        val repository = HolidayRepository(FakeHolidayDao(), BrasilApiService(stuck), deadlineMillis = 300)

        val start = System.currentTimeMillis()
        val result = repository.refresh(2026)
        val elapsed = System.currentTimeMillis() - start

        assertEquals("A BrasilAPI demorou para responder.", result.exceptionOrNull()?.message)
        assertTrue("esperou ${elapsed}ms", elapsed < 2_000)
    }
}
