package com.todolist.app

import com.todolist.app.data.security.PasswordHasher
import com.todolist.app.domain.AuthValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthTest {

    @Test
    fun `hash confere apenas com a senha correta`() {
        val hashed = PasswordHasher.hash("minhaSenha123")
        assertTrue(PasswordHasher.verify("minhaSenha123", hashed.hash, hashed.salt))
        assertFalse(PasswordHasher.verify("outraSenha", hashed.hash, hashed.salt))
    }

    @Test
    fun `mesma senha gera hashes diferentes por causa do salt`() {
        val a = PasswordHasher.hash("123456")
        val b = PasswordHasher.hash("123456")
        assertNotEquals(a.hash, b.hash)
        assertNotEquals(a.salt, b.salt)
    }

    @Test
    fun `validacao de email`() {
        assertNull(AuthValidator.emailError("marina@email.com"))
        assertNull(AuthValidator.emailError("  Marina@Email.com "))
        assertNotNull(AuthValidator.emailError(""))
        assertNotNull(AuthValidator.emailError("marina"))
        assertNotNull(AuthValidator.emailError("marina@email"))
    }

    @Test
    fun `email e normalizado para minusculas sem espacos`() {
        assertEquals("marina@email.com", AuthValidator.normalizeEmail("  Marina@Email.COM "))
    }

    @Test
    fun `validacao de senha nome e confirmacao`() {
        assertNotNull(AuthValidator.passwordError("12345"))
        assertNull(AuthValidator.passwordError("123456"))
        assertNotNull(AuthValidator.nameError(" "))
        assertNotNull(AuthValidator.nameError("A"))
        assertNull(AuthValidator.nameError("Ana"))
        assertNotNull(AuthValidator.confirmError("123456", "123457"))
        assertNull(AuthValidator.confirmError("123456", "123456"))
    }
}
