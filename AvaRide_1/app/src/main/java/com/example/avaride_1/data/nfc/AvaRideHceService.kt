package com.example.avaride_1.data.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import com.example.avaride_1.domain.model.NFCSession
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Host-based Card Emulation (HCE) Service for AvaRide Tap-to-Ride
 *
 * This service allows the phone to act as an NFC card when the vehicle's
 * NFC reader initiates contact. The vehicle sends a challenge, and the
 * phone responds with a signed proof of the valid booking.
 *
 * APDU Protocol:
 * 1. Vehicle sends SELECT AID command
 * 2. Phone responds with OK if AvaRide session is active
 * 3. Vehicle sends GET_CHALLENGE command
 * 4. Phone generates and stores nonce
 * 5. Vehicle sends VERIFY with its nonce
 * 6. Phone responds with HMAC signature proving valid booking
 * 7. Vehicle validates and unlocks
 *
 * Security Features:
 * - Challenge-response prevents replay attacks
 * - HMAC-SHA256 for cryptographic verification
 * - Session tokens expire after 5 minutes
 * - Requires explicit physical proximity (~4cm)
 */
class AvaRideHceService : HostApduService() {

    companion object {
        private const val TAG = "AvaRideHCE"

        // AvaRide Application ID (AID) - F0415641524944 = "AVARIDE" in hex
        private val AVARIDE_AID = byteArrayOf(
            0xF0.toByte(), 0x41.toByte(), 0x56.toByte(), 0x41.toByte(),
            0x52.toByte(), 0x49.toByte(), 0x44.toByte()
        )

        // APDU Commands
        private const val INS_SELECT = 0xA4.toByte()
        private const val INS_GET_CHALLENGE = 0x84.toByte()
        private const val INS_VERIFY = 0x20.toByte()
        private const val INS_GET_SESSION_INFO = 0xCA.toByte()

        // Status words
        private val SW_SUCCESS = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val SW_NO_SESSION = byteArrayOf(0x69.toByte(), 0x85.toByte())
        private val SW_SESSION_EXPIRED = byteArrayOf(0x69.toByte(), 0x83.toByte())
        private val SW_WRONG_DATA = byteArrayOf(0x6A.toByte(), 0x80.toByte())
        private val SW_UNKNOWN = byteArrayOf(0x6F.toByte(), 0x00.toByte())

        private const val HMAC_ALGORITHM = "HmacSHA256"

        // Singleton for session management across service instances
        @Volatile
        private var currentSession: NFCSession? = null
        private var lastChallenge: ByteArray? = null

        /**
         * Set the active NFC session for HCE
         */
        fun setSession(session: NFCSession?) {
            currentSession = session
            lastChallenge = null
            Log.d(TAG, "HCE Session ${if (session != null) "set: ${session.sessionId}" else "cleared"}")
        }

        /**
         * Get current session
         */
        fun getSession(): NFCSession? = currentSession

        /**
         * Check if there's a valid session
         */
        fun hasValidSession(): Boolean {
            val session = currentSession
            return session != null && !session.isExpired()
        }
    }

    private val secureRandom = SecureRandom()

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        Log.d(TAG, "Received APDU: ${commandApdu.toHexString()}")

        if (commandApdu.size < 4) {
            Log.w(TAG, "APDU too short")
            return SW_WRONG_DATA
        }

        val cla = commandApdu[0]
        val ins = commandApdu[1]
        val p1 = commandApdu[2]
        val p2 = commandApdu[3]

        return when (ins) {
            INS_SELECT -> handleSelect(commandApdu)
            INS_GET_CHALLENGE -> handleGetChallenge()
            INS_VERIFY -> handleVerify(commandApdu)
            INS_GET_SESSION_INFO -> handleGetSessionInfo()
            else -> {
                Log.w(TAG, "Unknown instruction: ${ins.toInt() and 0xFF}")
                SW_UNKNOWN
            }
        }
    }

    /**
     * Handle SELECT AID command
     */
    private fun handleSelect(apdu: ByteArray): ByteArray {
        // Check if selecting our AID
        if (apdu.size >= 5) {
            val lc = apdu[4].toInt() and 0xFF
            if (apdu.size >= 5 + lc) {
                val aid = apdu.copyOfRange(5, 5 + lc)

                if (aid.contentEquals(AVARIDE_AID)) {
                    Log.d(TAG, "AvaRide AID selected")

                    // Check if we have a valid session
                    val session = currentSession
                    return if (session != null && !session.isExpired()) {
                        // Return OK with session status
                        byteArrayOf(0x01) + SW_SUCCESS // 0x01 = session active
                    } else {
                        Log.w(TAG, "No valid session available")
                        SW_NO_SESSION
                    }
                }
            }
        }

        Log.w(TAG, "Invalid SELECT command")
        return SW_WRONG_DATA
    }

    /**
     * Handle GET_CHALLENGE - generate nonce for challenge-response
     */
    private fun handleGetChallenge(): ByteArray {
        val session = currentSession
        if (session == null || session.isExpired()) {
            Log.w(TAG, "No valid session for challenge")
            return if (session?.isExpired() == true) SW_SESSION_EXPIRED else SW_NO_SESSION
        }

        // Generate random challenge nonce
        val challenge = ByteArray(16)
        secureRandom.nextBytes(challenge)
        lastChallenge = challenge

        Log.d(TAG, "Generated challenge: ${challenge.toHexString()}")

        // Return challenge + timestamp + SW_SUCCESS
        val timestamp = System.currentTimeMillis()
        val timestampBytes = timestamp.toByteArray()

        return challenge + timestampBytes + SW_SUCCESS
    }

    /**
     * Handle VERIFY - validate vehicle's challenge and respond with signature
     */
    private fun handleVerify(apdu: ByteArray): ByteArray {
        val session = currentSession ?: return SW_NO_SESSION
        val challenge = lastChallenge ?: return SW_WRONG_DATA

        if (session.isExpired()) {
            return SW_SESSION_EXPIRED
        }

        // Extract vehicle's nonce from APDU
        if (apdu.size < 21) { // 4 header + 1 lc + 16 nonce
            return SW_WRONG_DATA
        }

        val vehicleNonce = apdu.copyOfRange(5, 21)

        // Generate signature: HMAC(token, challenge || vehicleNonce || tripId)
        val dataToSign = challenge + vehicleNonce + session.tripId.toByteArray()
        val signature = generateHmacSignature(dataToSign, session.token)

        Log.d(TAG, "Generated signature for trip: ${session.tripId}")

        // Build response: tripId (length-prefixed) + signature + SW_SUCCESS
        val tripIdBytes = session.tripId.toByteArray()
        val response = byteArrayOf(tripIdBytes.size.toByte()) +
                       tripIdBytes +
                       signature +
                       SW_SUCCESS

        // Clear challenge after use (one-time use)
        lastChallenge = null

        return response
    }

    /**
     * Handle GET_SESSION_INFO - return current session details
     */
    private fun handleGetSessionInfo(): ByteArray {
        val session = currentSession
        if (session == null || session.isExpired()) {
            return if (session?.isExpired() == true) SW_SESSION_EXPIRED else SW_NO_SESSION
        }

        // Return: vehicleId (length-prefixed) + tripId (length-prefixed) + expiry timestamp + SW_SUCCESS
        val vehicleIdBytes = session.vehicleId.toByteArray()
        val tripIdBytes = session.tripId.toByteArray()
        val expiryBytes = session.expiresAt.toByteArray()

        return byteArrayOf(vehicleIdBytes.size.toByte()) + vehicleIdBytes +
               byteArrayOf(tripIdBytes.size.toByte()) + tripIdBytes +
               expiryBytes +
               SW_SUCCESS
    }

    /**
     * Generate HMAC-SHA256 signature
     */
    private fun generateHmacSignature(data: ByteArray, key: String): ByteArray {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        val secretKey = SecretKeySpec(key.toByteArray(), HMAC_ALGORITHM)
        mac.init(secretKey)
        return mac.doFinal(data)
    }

    override fun onDeactivated(reason: Int) {
        val reasonStr = when (reason) {
            DEACTIVATION_LINK_LOSS -> "Link Loss"
            DEACTIVATION_DESELECTED -> "Deselected"
            else -> "Unknown"
        }
        Log.d(TAG, "HCE deactivated: $reasonStr")
    }

    // Extension functions
    private fun ByteArray.toHexString(): String =
        joinToString("") { "%02x".format(it) }

    private fun Long.toByteArray(): ByteArray {
        return ByteArray(8) { i ->
            (this shr (56 - i * 8)).toByte()
        }
    }
}

