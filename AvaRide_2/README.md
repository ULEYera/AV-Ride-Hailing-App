# AvaRide - Autonomous Vehicle Ride-Hailing App

## 🎨 Design Philosophy: "Zero-UI" & Apple Intelligence Aesthetic

AvaRide is a futuristic autonomous vehicle ride-hailing application built with **Jetpack Compose** for Android, inspired by Apple's Intelligence design language. The app embraces:

- **Anticipatory Design**: AI-powered destination prediction using Google Gemini
- **Minimalist Interface**: Glowing mesh gradients, frosted glass effects, and fluid animations
- **Conversational Flow**: Setup and interactions feel like talking to an intelligent assistant
- **Environmental Control**: In-ride dashboard for temperature, lighting, and music

---

## 🏗️ Architecture Overview

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **AI Engine**: Google Gemini API (Predictive destination modeling)
- **Navigation**: Navigation Compose
- **State Management**: ViewModel + StateFlow (MVVM pattern)
- **Hardware Integration**: 
  - NFC (Vehicle unlock)
  - ARCore (Wayfinding - placeholder)
  - Location Services

### Project Structure
```
com.example.avaride_1/
├── data/
│   └── remote/
│       └── GeminiPredictiveService.kt    # AI prediction service
├── domain/
│   └── model/                             # Data models
│       ├── Destination.kt
│       ├── Vehicle.kt
│       ├── Ride.kt
│       └── UserContext.kt
├── presentation/
│   ├── components/                        # Reusable UI components
│   │   ├── GlowingMeshGradient.kt        # Animated background
│   │   ├── FrostedGlassCard.kt           # Glassmorphism effects
│   │   └── PulsatingOrb.kt               # Siri-style animation
│   ├── navigation/
│   │   └── AppState.kt                    # Navigation states
│   ├── screens/
│   │   ├── onboarding/                    # Setup flow
│   │   ├── home/                          # Predictive home screen
│   │   ├── inride/                        # In-ride controls
│   │   ├── summary/                       # Ride completion
│   │   └── settings/                      # Control Center overlay
│   └── AvaRideApp.kt                      # Main navigation
├── ui/theme/
│   └── Theme.kt                           # Apple Intelligence colors
└── MainActivity.kt
```

---

## 🚀 Setup Instructions

### 1. Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 24+ (minSdk 24, targetSdk 36)
- Google Gemini API key

### 2. Get Gemini API Key
1. Go to [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Create a new API key
3. Copy the key

### 3. Configure API Key
Add to `gradle.properties` (in project root):
```properties
GEMINI_API_KEY=your_api_key_here
```

**Important**: Add `gradle.properties` to `.gitignore` to keep your key secure!

### 4. Sync Gradle
Open the project in Android Studio and sync Gradle dependencies.

### 5. Run the App
Select a device/emulator and click Run (Shift+F10)

---

## 📱 Application Flow

### 1. **Onboarding** - "The Conversation"
- Welcome screen with pulsating orb
- Name input with floating glass card
- Payment method setup (Apple Pay recommended)
- Biometric authentication setup
- Completion celebration

**Design**: Conversational, step-by-step flow with smooth transitions

### 2. **Predictive Home Screen** - "The Magic State"
**Logic**:
```
Current Location + Time + History → Gemini AI → Predicted Destination
```

**UI**:
- Single pulsating glass card: "Heading Home?"
- Ride details: Vehicle type, ETA, price
- One-tap booking button
- Search pill for manual entry

**Key Feature**: High-confidence predictions (>70%) show green indicator

### 3. **Booking Flow** (Simulated)
- Vehicle assignment animation
- Real-time vehicle tracking visualization (Google Maps SDK)

### 4. **In-Ride Experience** - "The Remote Control" (Sensor & Cloud Demo)
**UI Components**:
- Large progress ring (journey completion %)
- Destination name and ETA
- **Environment Controls** (Firestore Sync):
  - Temperature slider (16-28°C)
  - Mood lighting selector (Warm/Cool/Ambient/Off)
- **Sensor Data** (Simulated):
  - Accelerometer/Gyroscope data visualization (if enabled)

**Design**: Calm, minimalist dashboard with frosted glass controls

### 5. **Settings** - "Control Center Overlay"
**Access**: Tap profile avatar (top-right)

**Design**: iOS Control Center-inspired grid layout
- **Cards**: Home, Work
- **Quick Settings**: Location Services

---

## 🎨 Visual Design System

### Colors (iOS-inspired)
```kotlin
Primary: #0A84FF    // iOS Blue
Secondary: #5E5CE6  // iOS Purple  
Success: #30D158    // iOS Green
Error: #FF3B30      // iOS Red
Warning: #FF9500    // iOS Orange
```

### Typography
- Large headings: **28-48sp, Bold**
- Body text: **16sp, Regular**
- Captions: **13-14sp, Medium**

### Effects
1. **Glassmorphism**: `Color.White.copy(alpha = 0.08-0.15f)` with blur
2. **Mesh Gradients**: Animated radial gradients with slow movement
3. **Animations**: Spring physics, 600-1000ms durations
4. **Corners**: Large radii (16-28dp) for modern feel

---

## 🧠 Gemini AI Integration

### Prediction Prompt Format
```
You are an intelligent transportation assistant. Predict where the user is likely heading.

Current Context:
- Location: [User's current address]
- Day: [Monday-Sunday]
- Time: [Hour in 24h format]
- Recent trips: [List of recent destinations]
- Saved locations: [Home, Work, etc.]

Respond ONLY in JSON format:
{
  "destination_name": "Name",
  "address": "Full address",
  "confidence": 0.85,
  "reasoning": "One sentence"
}
```

### Implementation
See `GeminiPredictiveService.kt` for full implementation with error handling and parsing.

---

## 🔧 Key Components Explained

### 1. GlowingMeshGradient
Creates an animated background with moving gradient circles:
```kotlin
@Composable
fun GlowingMeshGradient(
    colors: List<Color> = iOS_gradient_colors
)
```
- Uses infinite animation
- 20-second loop with reverse repeat
- Multiple overlapping radial gradients

### 2. FrostedGlassCard
Glassmorphism container for content:
```kotlin
@Composable
fun FrostedGlassCard(
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
)
```
- Blur effect background
- Semi-transparent white overlay
- 24dp corner radius

### 3. PulsatingOrb
Siri-style animated orb:
```kotlin
@Composable
fun PulsatingOrb(color: Color = iOS_blue)
```
- Scale animation (0.8f to 1.2f)
- Alpha animation (0.4f to 0.9f)
- Three concentric circles

### 4. InRideControlView
Environment control dashboard:
- **Temperature**: IconButton +/- controls
- **Lighting**: Grid of mode chips with icons
- **Music**: Integration placeholder
- **Progress Ring**: Canvas-based circular progress

---

## 🔮 Future Enhancements

### Phase 2 (ARCore Integration)
- [ ] AR wayfinding with 3D path rendering
- [ ] AR vehicle identification with floating tags
- [ ] Camera-based pickup assistance

### Phase 3 (NFC & Bluetooth)
- [ ] Real NFC vehicle unlock
- [ ] Bluetooth fallback for older vehicles
- [ ] Secure handshake protocol

### Phase 4 (Backend Integration)
- [ ] Real-time vehicle tracking API
- [ ] Payment processing (Stripe/Apple Pay)
- [ ] User authentication (Firebase/Auth0)
- [ ] Ride history database (Room + Firebase)

### Phase 5 (Advanced AI)
- [ ] Voice commands (Google Assistant integration)
- [ ] Contextual notifications ("Time to leave for appointment")
- [ ] Route optimization based on preferences
- [ ] Accessibility features (voice-only mode)

---

## 🎯 Design Principles

1. **Anticipation over Interaction**: Predict what the user needs before they ask
2. **Clarity over Clutter**: Show only essential information
3. **Delight in Details**: Smooth animations and haptic feedback
4. **Consistency**: iOS-inspired design language throughout
5. **Accessibility**: High contrast, large touch targets, voice support

---

## 📦 Dependencies

All dependencies are managed in `libs.versions.toml`:

**Core**:
- `androidx.navigation:navigation-compose:2.7.7`
- `androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0`

**AI**:
- `com.google.ai.client.generativeai:generativeai:0.2.2`

**Location**:
- `com.google.android.gms:play-services-location:21.1.0`

**AR**:
- `com.google.ar:core:1.42.0`

**UI Enhancements**:
- `com.google.accompanist:accompanist-systemuicontroller:0.32.0`
- `com.airbnb.android:lottie-compose:6.3.0`
- `io.coil-kt:coil-compose:2.5.0`

---

## 🐛 Troubleshooting

### Build Errors
- **Missing API key**: Ensure `GEMINI_API_KEY` is in `gradle.properties`
- **Gradle sync failed**: Check internet connection and update Gradle

### Runtime Issues
- **Gemini API errors**: Verify API key is valid and billing is enabled
- **Location not working**: Grant location permissions in settings
- **Animations laggy**: Enable GPU rendering in developer options

---

## 📄 License

This project is a demonstration of UI/UX design principles for autonomous vehicle applications. 

---

## 👨‍💻 Author

Built with ❤️ as a showcase of "Apple Intelligence" aesthetic adapted for Android.

**Key Technologies**: Jetpack Compose • Gemini AI • Material Design 3 • MVVM Architecture

---

## 🌟 Highlights

✨ **Zero-UI Philosophy**: Minimal touches, maximum prediction  
🎨 **Glassmorphism**: Frosted glass effects throughout  
🤖 **AI-Powered**: Google Gemini predicts destinations  
🚗 **In-Ride Controls**: Temperature, lighting, music dashboard  
📱 **iOS-Inspired**: Apple Intelligence design language  
🏎️ **Smooth Animations**: Spring physics and fluid transitions  

**Perfect for**: Portfolio projects, design showcases, AV/mobility startups

