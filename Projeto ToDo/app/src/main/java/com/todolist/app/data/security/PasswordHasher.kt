package com.todolist.app.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Gera e confere hashes de senha com PBKDF2-HMAC-SHA256 e salt aleatório.
 * A senha original não pode ser recuperada a partir do hash.
 */
object PasswordHasher {
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16

    data class Hashed(val hash: String, val salt: String)

    fun hash(password: String): Hashed {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        return Hashed(encode(derive(password, salt)), encode(salt))
    }

    fun verify(password: String, hash: String, salt: String): Boolean {
        val expected = Base64.getDecoder().decode(hash)
        val actual = derive(password, Base64.getDecoder().decode(salt))
        // Comparação em tempo constante, para não vazar informação pelo tempo de resposta.
        return MessageDigest.isEqual(expected, actual)
    }

    private fun derive(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun encode(bytes: ByteArray) = Base64.getEncoder().encodeToString(bytes)
}
