package com.khanabooklite.app.domain.manager

import org.mindrot.jbcrypt.BCrypt

object AuthManager {

    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(12))
    }

    fun verifyPassword(password: String, hash: String): Boolean {
        return try {
            BCrypt.checkpw(password, hash)
        } catch (e: Exception) {
            false // Fallback for old SHA-256 hashes if any existed, though migration would be needed for a real prod app
        }
    }
}
