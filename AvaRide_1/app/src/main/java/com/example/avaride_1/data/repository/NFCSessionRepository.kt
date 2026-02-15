package com.example.avaride_1.data.repository

import android.util.Log
import com.example.avaride_1.domain.model.NFCSession
import com.example.avaride_1.domain.model.NFCSessionStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom

/**
 * Repository for NFC session management with Firebase backend.
 * Handles creation, validation, and completion of NFC unlock sessions.
 */
class NFCSessionRepository {

    companion object {
        private const val TAG = "NFCSessionRepository"
        private const val COLLECTION_NFC_SESSIONS = "nfc_sessions"
        private const val COLLECTION_UNLOCK_LOGS = "unlock_logs"
    }

    private val db = FirebaseFirestore.getInstance()
    private val sessionsCollection = db.collection(COLLECTION_NFC_SESSIONS)
    private val secureRandom = SecureRandom()

    /**
     * Create a new NFC session for vehicle unlock
     */
    suspend fun createSession(
        tripId: String,
        vehicleId: String,
        userId: String
    ): NFCSession {
        val sessionId = generateSessionId()
        val token = generateSecureToken()

        val session = NFCSession(
            sessionId = sessionId,
            tripId = tripId,
            vehicleId = vehicleId,
            userId = userId,
            token = token
        )

        try {
            // Store session in Firestore
            sessionsCollection.document(sessionId).set(
                mapOf(
                    "sessionId" to session.sessionId,
                    "tripId" to session.tripId,
                    "vehicleId" to session.vehicleId,
                    "userId" to session.userId,
                    "tokenHash" to hashToken(token), // Store hash, not raw token
                    "createdAt" to session.createdAt,
                    "expiresAt" to session.expiresAt,
                    "status" to session.status.name
                )
            ).await()

            Log.d(TAG, "Created NFC session: $sessionId for trip: $tripId")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to store session in Firestore", e)
            // Continue anyway - session can work locally
        }

        return session
    }

    /**
     * Validate an NFC session
     */
    suspend fun validateSession(sessionId: String): NFCSession? {
        return try {
            val doc = sessionsCollection.document(sessionId).get().await()

            if (!doc.exists()) {
                Log.w(TAG, "Session not found: $sessionId")
                return null
            }

            val expiresAt = doc.getLong("expiresAt") ?: 0
            if (System.currentTimeMillis() > expiresAt) {
                Log.w(TAG, "Session expired: $sessionId")
                updateSessionStatus(sessionId, NFCSessionStatus.EXPIRED)
                return null
            }

            val statusStr = doc.getString("status") ?: NFCSessionStatus.PENDING.name
            val status = NFCSessionStatus.valueOf(statusStr)

            if (status != NFCSessionStatus.PENDING) {
                Log.w(TAG, "Session not in pending state: $sessionId, status: $status")
                return null
            }

            NFCSession(
                sessionId = doc.getString("sessionId") ?: sessionId,
                tripId = doc.getString("tripId") ?: "",
                vehicleId = doc.getString("vehicleId") ?: "",
                userId = doc.getString("userId") ?: "",
                token = "", // Don't retrieve raw token
                createdAt = doc.getLong("createdAt") ?: 0,
                expiresAt = expiresAt,
                status = status
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to validate session", e)
            null
        }
    }

    /**
     * Mark session as completed (unlock successful)
     */
    suspend fun completeSession(sessionId: String, vehicleId: String): Boolean {
        return try {
            sessionsCollection.document(sessionId).update(
                mapOf(
                    "status" to NFCSessionStatus.COMPLETED.name,
                    "completedAt" to System.currentTimeMillis(),
                    "unlockedVehicleId" to vehicleId
                )
            ).await()

            // Log the unlock event
            logUnlockEvent(sessionId, vehicleId, true)

            Log.d(TAG, "Session completed: $sessionId")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to complete session", e)
            false
        }
    }

    /**
     * Mark session as failed
     */
    suspend fun failSession(sessionId: String, reason: String): Boolean {
        return try {
            sessionsCollection.document(sessionId).update(
                mapOf(
                    "status" to NFCSessionStatus.FAILED.name,
                    "failedAt" to System.currentTimeMillis(),
                    "failureReason" to reason
                )
            ).await()

            // Log the failed unlock attempt
            logUnlockEvent(sessionId, null, false, reason)

            Log.d(TAG, "Session failed: $sessionId - $reason")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to update session status", e)
            false
        }
    }

    /**
     * Update session status
     */
    private suspend fun updateSessionStatus(sessionId: String, status: NFCSessionStatus) {
        try {
            sessionsCollection.document(sessionId).update("status", status.name).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update session status", e)
        }
    }

    /**
     * Log unlock event for audit trail
     */
    private suspend fun logUnlockEvent(
        sessionId: String,
        vehicleId: String?,
        success: Boolean,
        failureReason: String? = null
    ) {
        try {
            val logId = "log_${System.currentTimeMillis()}_${generateShortId()}"

            db.collection(COLLECTION_UNLOCK_LOGS).document(logId).set(
                mapOf(
                    "logId" to logId,
                    "sessionId" to sessionId,
                    "vehicleId" to vehicleId,
                    "success" to success,
                    "failureReason" to failureReason,
                    "timestamp" to System.currentTimeMillis()
                )
            ).await()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to log unlock event", e)
        }
    }

    /**
     * Clean up expired sessions (can be called periodically)
     */
    suspend fun cleanupExpiredSessions() {
        try {
            val now = System.currentTimeMillis()
            val expiredDocs = sessionsCollection
                .whereLessThan("expiresAt", now)
                .whereEqualTo("status", NFCSessionStatus.PENDING.name)
                .get()
                .await()

            expiredDocs.documents.forEach { doc ->
                doc.reference.update("status", NFCSessionStatus.EXPIRED.name)
            }

            Log.d(TAG, "Cleaned up ${expiredDocs.size()} expired sessions")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup sessions", e)
        }
    }

    // Helper functions

    private fun generateSessionId(): String {
        val bytes = ByteArray(16)
        secureRandom.nextBytes(bytes)
        return "nfc_${bytes.joinToString("") { "%02x".format(it) }}"
    }

    private fun generateSecureToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun generateShortId(): String {
        val bytes = ByteArray(4)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hashToken(token: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(token.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}

