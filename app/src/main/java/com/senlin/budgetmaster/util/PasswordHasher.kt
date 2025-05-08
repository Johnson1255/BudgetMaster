package com.senlin.budgetmaster.util

import java.security.MessageDigest
import java.nio.charset.StandardCharsets

object PasswordHasher {

    fun hashPassword(password: String, salt: String = "BudgetMasterSalt"): String {
        // In a real app, use a unique, randomly generated salt per user stored with their record.
        // For this example, a static salt is used for simplicity, which is NOT secure for production.
        val combined = salt + password
        val bytes = combined.toByteArray(StandardCharsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return bytesToHex(digest)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = "0123456789abcdef"[v ushr 4]
            hexChars[j * 2 + 1] = "0123456789abcdef"[v and 0x0F]
        }
        return String(hexChars)
    }

    // Consider adding a verifyPassword method if needed directly here,
    // though often verification happens by hashing the input and comparing.
    fun verifyPassword(providedPassword: String, hashedPasswordFromDb: String, salt: String = "BudgetMasterSalt"): Boolean {
        val hashedProvidedPassword = hashPassword(providedPassword, salt)
        return hashedProvidedPassword == hashedPasswordFromDb
    }
}
