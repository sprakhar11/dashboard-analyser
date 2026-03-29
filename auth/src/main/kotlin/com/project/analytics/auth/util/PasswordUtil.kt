package com.project.analytics.auth.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class PasswordUtil(
    @Value("\${auth.password.secret-key}") private val secretKey: String
) {

    private val random = SecureRandom()

    /**
     * Hash a password with a random salt using HMAC-SHA256.
     * Output format: "salt_hex:hmac_hex"
     */
    fun hash(password: String): String {
        val salt = ByteArray(16)
        random.nextBytes(salt)
        val saltHex = salt.toHex()
        val hmacHex = hmac(password, saltHex)
        return "$saltHex:$hmacHex"
    }

    /**
     * Verify a password against a stored hash.
     * Supports both new format "salt:hmac" and legacy plain SHA-256 hex.
     */
    fun verify(password: String, storedHash: String): Boolean {
        return if (storedHash.contains(":")) {
            // New format: salt:hmac
            val (saltHex, expectedHmac) = storedHash.split(":", limit = 2)
            val actualHmac = hmac(password, saltHex)
            constantTimeEquals(expectedHmac, actualHmac)
        } else {
            // Legacy format: plain SHA-256 (for existing users)
            val legacyHash = legacySha256(password)
            constantTimeEquals(storedHash, legacyHash)
        }
    }

    private fun hmac(password: String, saltHex: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        val data = "$saltHex:$password".toByteArray(Charsets.UTF_8)
        return mac.doFinal(data).toHex()
    }

    private fun legacySha256(password: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        return digest.digest(password.toByteArray(Charsets.UTF_8)).toHex()
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
