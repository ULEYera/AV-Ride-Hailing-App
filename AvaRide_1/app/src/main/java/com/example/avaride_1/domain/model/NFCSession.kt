package com.example.avaride_1.domain.model

/**
 * Represents an NFC session token used for secure vehicle unlock handshake.
 * This token is short-lived and scoped to a single trip.
 */
data class NFCSession(
    val sessionId: String,
    val tripId: String,
    val vehicleId: String,
    val userId: String,
    val token: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + TOKEN_TTL_MS,
    val status: NFCSessionStatus = NFCSessionStatus.PENDING
) {
    companion object {
        const val TOKEN_TTL_MS = 5 * 60 * 1000L // 5 minutes
    }

    fun isExpired(): Boolean = System.currentTimeMillis() > expiresAt

    fun isValid(): Boolean = !isExpired() && status == NFCSessionStatus.PENDING
}

enum class NFCSessionStatus {
    PENDING,      // Token generated, waiting for tap
    VALIDATING,   // Tap detected, validating
    COMPLETED,    // Successfully unlocked
    EXPIRED,      // Token expired
    FAILED        // Validation failed
}

/**
 * Result of an NFC unlock attempt
 */
sealed class NFCUnlockResult {
    data class Success(
        val vehicleId: String,
        val sessionId: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : NFCUnlockResult()

    data class Error(
        val code: NFCErrorCode,
        val message: String
    ) : NFCUnlockResult()
}

enum class NFCErrorCode {
    NFC_NOT_AVAILABLE,
    NFC_DISABLED,
    SESSION_EXPIRED,
    INVALID_TOKEN,
    VEHICLE_MISMATCH,
    NETWORK_ERROR,
    UNKNOWN_ERROR
}

/**
 * Challenge-response data for secure NFC handshake
 */
data class NFCChallengeResponse(
    val nonce: String,
    val timestamp: Long,
    val tripId: String,
    val signature: String
)

