# NFC Secure Tap-to-Ride Implementation

## Overview

AvaRide implements a secure NFC-based vehicle unlock system that replaces passive Bluetooth proximity detection with an explicit, cryptographically verified "physical handshake." This addresses the security vulnerabilities found in Tesla's PKES (Passive Keyless Entry and Start) system.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     AvaRide NFC Architecture                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────┐ │
│  │   Phone     │    │   Firebase  │    │   AV NFC Reader     │ │
│  │ (HCE Card)  │◄──►│   Backend   │◄──►│   (ISO-DEP Host)    │ │
│  └─────────────┘    └─────────────┘    └─────────────────────┘ │
│         │                  │                      │             │
│         │    1. Create Session                    │             │
│         │◄─────────────────┤                      │             │
│         │                  │                      │             │
│         │    2. Tap Phone to Vehicle              │             │
│         ├─────────────────────────────────────────►             │
│         │                  │                      │             │
│         │    3. Challenge-Response                │             │
│         │◄────────────────────────────────────────►             │
│         │                  │                      │             │
│         │    4. Validate & Unlock                 │             │
│         │                  ◄──────────────────────┤             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Components

### 1. NFCSession (Domain Model)
**File:** `domain/model/NFCSession.kt`

Represents a short-lived session token for secure unlock:
- `sessionId`: Unique identifier
- `tripId`: Associated trip
- `vehicleId`: Target vehicle
- `token`: HMAC key for signing
- `expiresAt`: 5-minute TTL
- `status`: PENDING → COMPLETED/FAILED/EXPIRED

### 2. NFCManager (Core Logic)
**File:** `data/nfc/NFCManager.kt`

Handles NFC operations:
- **Reader Mode**: Phone reads vehicle NFC tag
- **Session Management**: Create/validate sessions
- **Challenge-Response**: HMAC-SHA256 signing
- **Multi-tech Support**: NDEF, ISO-DEP, Mifare

Key methods:
```kotlin
fun startSession(tripId, vehicleId, userId): NFCSession
fun enableReaderMode(activity, onTagDetected)
fun disableReaderMode(activity)
fun getNfcStatus(): NFCStatus
```

### 3. AvaRideHceService (HCE Card Emulation)
**File:** `data/nfc/AvaRideHceService.kt`

Allows phone to act as NFC card for vehicle readers:
- **AID:** F0415641524944 ("AVARID" in hex)
- **APDU Commands:**
  - `SELECT`: Activate AvaRide applet
  - `GET_CHALLENGE`: Request nonce
  - `VERIFY`: Send signed proof
  - `GET_SESSION_INFO`: Query session details

### 4. NFCSessionRepository (Firebase)
**File:** `data/repository/NFCSessionRepository.kt`

Cloud persistence:
- Store session tokens (hashed)
- Log unlock events
- Audit trail for security

### 5. NFCUnlockViewModel (UI State)
**File:** `presentation/viewmodel/NFCUnlockViewModel.kt`

UI state management:
- Initialize unlock session
- Coordinate reader mode
- Handle results

### 6. SecureNFCUnlockScreen (UI)
**File:** `presentation/screens/nfc/SecureNFCUnlockScreen.kt`

User interface:
- Session countdown timer
- NFC waves animation
- Success/error states
- QR fallback option

### 7. QRUnlockScreen (Fallback)
**File:** `presentation/screens/nfc/QRUnlockScreen.kt`

For devices without NFC:
- Generate QR with session token
- Vehicle camera scans QR
- Same security model

## Security Features

### 1. Challenge-Response Protocol
```
┌────────┐                              ┌────────┐
│ Phone  │                              │Vehicle │
└───┬────┘                              └───┬────┘
    │                                       │
    │  1. SELECT AID                        │
    │◄──────────────────────────────────────┤
    │                                       │
    │  2. OK (session active)               │
    ├──────────────────────────────────────►│
    │                                       │
    │  3. GET_CHALLENGE                     │
    │◄──────────────────────────────────────┤
    │                                       │
    │  4. Nonce + Timestamp                 │
    ├──────────────────────────────────────►│
    │                                       │
    │  5. VERIFY(vehicle_nonce)             │
    │◄──────────────────────────────────────┤
    │                                       │
    │  6. HMAC(token, challenge||nonce||tripId)
    ├──────────────────────────────────────►│
    │                                       │
    │  7. Validate → Unlock                 │
    │                                       │
```

### 2. Replay Attack Prevention
- One-time nonces
- Timestamp validation
- Session tokens expire after 5 minutes

### 3. Man-in-the-Middle Protection
- HMAC-SHA256 signatures
- Token never transmitted in clear
- Physical proximity required (~4cm)

### 4. Token Security
- Generated with SecureRandom (32 bytes)
- Stored hashed in Firebase
- Never logged or displayed

## Testing

### With NFC-Enabled Device
1. Enable NFC in Settings
2. Run app, book a ride
3. Navigate to NFC unlock screen
4. Tap phone to any NFC tag (credit card, transit card, etc.)
5. Watch secure unlock process

### With Emulator
- Use `Screen.NFCUnlock.route` (basic demo mode)
- Use "Skip for Demo" button

### Testing HCE
To test HCE with external NFC reader:
1. Install app on NFC-enabled device
2. Book a ride to activate session
3. Use external reader to send SELECT AID `F0415641524944`
4. Observe APDU responses

## Configuration

### AndroidManifest.xml
```xml
<!-- Permission -->
<uses-permission android:name="android.permission.NFC" />

<!-- Feature (optional) -->
<uses-feature android:name="android.hardware.nfc" android:required="false" />
<uses-feature android:name="android.hardware.nfc.hce" android:required="false" />

<!-- HCE Service -->
<service
    android:name=".data.nfc.AvaRideHceService"
    android:exported="true"
    android:permission="android.permission.BIND_NFC_SERVICE">
    <intent-filter>
        <action android:name="android.nfc.cardemulation.action.HOST_APDU_SERVICE" />
    </intent-filter>
    <meta-data
        android:name="android.nfc.cardemulation.host_apdu_service"
        android:resource="@xml/avaride_hce_service" />
</service>
```

### AID Configuration (xml/avaride_hce_service.xml)
```xml
<host-apdu-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:description="@string/hce_service_description"
    android:requireDeviceUnlock="true">
    <aid-group android:category="other">
        <aid-filter android:name="F0415641524944" />
    </aid-group>
</host-apdu-service>
```

## Navigation

### Routes
- `Screen.NFCUnlock` - Basic NFC (backwards compatible)
- `Screen.SecureNFCUnlock` - Secure with session management
- `Screen.QRUnlock` - QR fallback

### Usage
```kotlin
// Navigate to secure NFC unlock
navController.navigate(
    Screen.SecureNFCUnlock.createRoute(
        tripId = "trip_123",
        vehicleId = "AV-001",
        userId = "user_456"
    )
)
```

## Production Considerations

### 1. Backend Integration
Replace simulated validation with real API calls:
```kotlin
// In NFCManager.validateChallengeResponse()
val response = vehicleApi.validateUnlock(
    vehicleId = vehicleId,
    signature = signature,
    sessionId = session.sessionId
)
return response.isValid
```

### 2. Vehicle Integration
AV must implement:
- ISO-DEP NFC reader
- AvaRide APDU protocol
- Secure key storage for signature verification

### 3. Key Management
For production:
- Use Android Keystore for private keys
- Implement key rotation
- Consider asymmetric (RSA/ECDSA) for stronger security

### 4. Monitoring
- Log all unlock attempts
- Alert on suspicious patterns
- Rate limiting per session

## File Summary

| File | Purpose |
|------|---------|
| `domain/model/NFCSession.kt` | Session data model |
| `data/nfc/NFCManager.kt` | Core NFC operations |
| `data/nfc/AvaRideHceService.kt` | HCE card emulation |
| `data/repository/NFCSessionRepository.kt` | Firebase persistence |
| `presentation/viewmodel/NFCUnlockViewModel.kt` | UI state management |
| `presentation/screens/nfc/SecureNFCUnlockScreen.kt` | Secure unlock UI |
| `presentation/screens/nfc/QRUnlockScreen.kt` | QR fallback UI |
| `presentation/screens/nfc/NFCUnlockScreen.kt` | Basic unlock UI |
| `res/xml/avaride_hce_service.xml` | HCE AID configuration |
| `res/values/strings.xml` | NFC-related strings |

