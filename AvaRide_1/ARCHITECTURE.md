# AvaRide Architecture Document

## Executive Summary

AvaRide is an autonomous vehicle (AV) ride-hailing application that reimagines the transportation experience through **"Zero-UI"** and **anticipatory design** principles. Built with Jetpack Compose for Android, the app adapts Apple's Intelligence aesthetic to create a minimalist, AI-driven interface that predicts user needs before they interact.

---

## Design Philosophy

### Zero-UI Principles

1. **Anticipatory Design**
   - AI predicts destinations based on context (location, time, history)
   - Single-tap confirmations instead of multi-step forms
   - Auto-dismissing screens (ride summary fades after 5s)

2. **Visual Minimalism**
   - No cluttered maps or car lists on home screen
   - One dominant UI element per screen
   - Frosted glass effects replace solid backgrounds

3. **Conversational Interaction**
   - Onboarding feels like a conversation, not a form
   - Natural language prompts ("Heading Home?")
   - Binary choices (thumbs up/down vs. 5-star ratings)

4. **Environmental Awareness**
   - App adapts to ride state (calm colors during trip)
   - Predictive destination changes by time of day
   - Emergency controls always accessible but subtle

---

## Technical Architecture

### Clean Architecture Layers

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Compose UI + ViewModels + Navigation) │
└─────────────────────────────────────────┘
                    ↕
┌─────────────────────────────────────────┐
│          Domain Layer                   │
│     (Models + Business Logic)           │
└─────────────────────────────────────────┘
                    ↕
┌─────────────────────────────────────────┐
│           Data Layer                    │
│  (Repositories + API Services + Local)  │
└─────────────────────────────────────────┘
```

### MVVM Pattern with Compose

**ViewModel** manages state and business logic:
```kotlin
class HomeViewModel(private val geminiService: GeminiPredictiveService) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    fun loadPrediction() {
        viewModelScope.launch {
            val prediction = geminiService.predictDestination(context)
            _uiState.value = HomeUiState.Prediction(prediction, ...)
        }
    }
}
```

**Composable** observes state and renders UI:
```kotlin
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (uiState) {
        is HomeUiState.Loading -> LoadingState()
        is HomeUiState.Prediction -> PredictiveDestinationCard(...)
        is HomeUiState.Error -> ErrorState()
    }
}
```

---

## Application Flow Diagram

```
┌───────────────┐
│  Onboarding   │ (First launch only)
│  - Name       │
│  - Payment    │
│  - Biometric  │
└───────┬───────┘
        │
        ↓
┌───────────────────────────────────────┐
│       Predictive Home Screen          │ ← Main Loop Start
│  • Gemini AI predicts destination    │
│  • Single card: "Heading Home?"       │
│  • One-tap booking                    │
└───────┬───────────────────────────────┘
        │
        ↓ [Book Ride]
┌───────────────┐
│   Booking     │
│  • Find AV    │
│  • AR Guide   │
└───────┬───────┘
        │
        ↓ [Vehicle Arrives]
┌───────────────┐
│  NFC Unlock   │
│  • Tap phone  │
│  • Door opens │
└───────┬───────┘
        │
        ↓ [Enter Vehicle]
┌───────────────────────────────────┐
│       In-Ride Experience          │
│  • Progress ring                  │
│  • Temperature control            │
│  • Lighting control               │
│  • Music playback                 │
│  • Emergency stop button          │
└───────┬───────────────────────────┘
        │
        ↓ [Arrive at Destination]
┌───────────────┐
│ Ride Summary  │
│  • Total cost │
│  • Rating 👍👎│
│  • Auto-exit  │
└───────┬───────┘
        │
        ↓ [5 seconds later]
        └──────→ Back to Home Screen (Loop)
```

---

## Gemini AI Integration Strategy

### Prediction Context

The app builds a rich context object for AI prediction:

```kotlin
data class UserContext(
    val currentLocation: Location,        // GPS coordinates + address
    val currentTime: Long,                // Timestamp
    val dayOfWeek: Int,                   // 1-7 (Mon-Sun)
    val hourOfDay: Int,                   // 0-23
    val recentDestinations: List<Destination>,
    val favoriteLocations: Map<DestinationType, Destination>
)
```

### Prompt Engineering

**Structured Prompt** sent to Gemini:
```
You are an intelligent transportation assistant. Predict where the user is likely heading.

Current Context:
- Location: 123 Market St, San Francisco (Work)
- Day: Friday
- Time: 17:00 (Evening)
- Recent trips: Home (3x this week), Gym (2x this week)
- Saved locations: Home: 456 Oak St, Work: 123 Market St

Based on typical patterns, where is the user going?

Respond ONLY in JSON:
{
  "destination_name": "Home",
  "address": "456 Oak Street, San Francisco",
  "confidence": 0.92,
  "reasoning": "Leaving work on Friday evening"
}
```

### Response Parsing

```kotlin
private fun parseDestinationFromResponse(responseText: String): Destination? {
    val json = JSONObject(extractJsonFromMarkdown(responseText))
    return Destination(
        name = json.getString("destination_name"),
        address = json.getString("address"),
        confidence = json.getDouble("confidence").toFloat()
    )
}
```

### Fallback Strategy

1. **High Confidence (>70%)**: Show prediction card
2. **Medium Confidence (40-70%)**: Show prediction + "Or search" option
3. **Low Confidence (<40%)**: Show search bar with recent destinations
4. **API Failure**: Show search bar with cached favorites

---

## UI Component Library

### 1. GlowingMeshGradient

**Purpose**: Animated background that creates depth and movement

**Implementation**:
- Uses `Canvas` with multiple radial gradients
- Infinite animation with 20s duration
- Gradient circles move in circular paths using sin/cos

**Usage**:
```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    GlowingMeshGradient(
        colors = listOf(deepNavy, darkBlue, royalBlue, deepPurple)
    )
    // Content here
}
```

### 2. FrostedGlassCard

**Purpose**: Glassmorphism container for content cards

**Implementation**:
- Two layers: blur background + semi-transparent foreground
- `Color.White.copy(alpha = 0.08f)` for subtle transparency
- 24dp corner radius for modern feel

**Usage**:
```kotlin
FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
    Text("Heading Home?", fontSize = 32.sp)
    // More content
}
```

### 3. PulsatingOrb

**Purpose**: Siri-style loading/assistant indicator

**Implementation**:
- Three concentric circles (glow, ring, core)
- Scale animation: 0.8f ↔ 1.2f
- Alpha animation: 0.4f ↔ 0.9f
- 1500ms duration with FastOutSlowInEasing

**Usage**:
```kotlin
PulsatingOrb(color = Color(0xFF0A84FF))
```

### 4. Journey Progress Ring

**Purpose**: Visual feedback for ride progress

**Implementation**:
- Custom Canvas drawing
- Animated arc sweep from 0° to 360° * progress
- Gradient stroke (blue to purple)
- Center text shows percentage

**Code**:
```kotlin
Canvas(modifier) {
    // Background ring
    drawCircle(color = White.copy(0.1f), style = Stroke(...))
    
    // Progress arc
    drawArc(
        brush = Brush.sweepGradient(colors),
        startAngle = -90f,
        sweepAngle = 360f * progress,
        style = Stroke(width = 12.dp)
    )
}
```

---

## State Management

### Sealed Classes for UI States

```kotlin
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Prediction(
        val destination: Destination,
        val etaMinutes: Int,
        val estimatedPrice: Double
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
```

### StateFlow for Reactive Updates

```kotlin
class InRideViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(InRideUiState())
    val uiState: StateFlow<InRideUiState> = _uiState.asStateFlow()
    
    fun updateTemperature(temp: Int) {
        _uiState.update { it.copy(temperature = temp) }
        // Send to vehicle API
    }
}
```

### Benefits

1. **Type Safety**: Sealed classes prevent invalid states
2. **Reactivity**: UI auto-updates when StateFlow emits
3. **Testability**: Easy to mock ViewModel states
4. **Single Source of Truth**: ViewModel owns state

---

## Navigation Architecture

### Screen Graph

```kotlin
NavHost(navController, startDestination = Screen.Onboarding.route) {
    composable(Screen.Onboarding.route) { OnboardingScreen(...) }
    composable(Screen.Home.route) { HomeScreen(...) }
    composable(Screen.Booking.route) { BookingScreen(...) }
    composable(Screen.NFCUnlock.route) { NFCUnlockScreen(...) }
    composable(Screen.InRide.route) { InRideScreen(...) }
    composable(Screen.RideSummary.route) { RideSummaryScreen(...) }
}
```

### Overlay Pattern (Settings)

Instead of navigation, Settings uses a modal overlay:

```kotlin
var showSettings by remember { mutableStateOf(false) }

if (showSettings) {
    SettingsSheet(onDismiss = { showSettings = false })
}
```

**Why?** Maintains context and allows blur background.

---

## Performance Optimizations

### 1. Animation Performance

- Use `remember` for animation states
- `LaunchedEffect` for side effects (auto-dismiss)
- `derivedStateOf` for computed values

### 2. Lazy Loading

```kotlin
LazyVerticalGrid(columns = GridCells.Fixed(2)) {
    items(settingsCards) { card ->
        ControlCenterCard(card)
    }
}
```

### 3. Gemini API Caching

```kotlin
// Cache predictions for 5 minutes
private val predictionCache = mutableMapOf<String, CachedPrediction>()

suspend fun predictDestination(context: UserContext): Destination? {
    val cacheKey = "${context.currentLocation}_${context.hourOfDay}"
    val cached = predictionCache[cacheKey]
    
    if (cached != null && !cached.isExpired()) {
        return cached.destination
    }
    
    val prediction = geminiService.predict(context)
    predictionCache[cacheKey] = CachedPrediction(prediction, System.currentTimeMillis())
    return prediction
}
```

---

## Hardware Integration Points

### 1. NFC (Vehicle Unlock)

**Flow**:
1. User reaches vehicle (GPS match)
2. App shows "Tap to Unlock" screen
3. NFC reader activates
4. User taps phone to vehicle sensor
5. Backend validates + sends unlock command
6. Haptic feedback + visual confirmation

**Implementation (Production)**:
```kotlin
val nfcAdapter = NfcAdapter.getDefaultAdapter(context)

nfcAdapter.enableReaderMode(
    activity,
    { tag ->
        val vehicleId = readVehicleId(tag)
        unlockVehicle(vehicleId)
    },
    NfcAdapter.FLAG_READER_NFC_A,
    null
)
```

### 2. ARCore (Wayfinding)

**Flow**:
1. User taps "Guide me to Pickup"
2. Camera opens with AR overlay
3. 3D path renders on ground
4. Path updates as user moves
5. When vehicle in view, AR tag appears above it

**Implementation (Production)**:
```kotlin
val arSession = Session(context)
val arSceneView = ArSceneView(context)

// Render path waypoints
waypoints.forEach { point ->
    val anchorNode = AnchorNode(arSession.createAnchor(point))
    anchorNode.renderable = pathSegmentRenderable
    arSceneView.scene.addChild(anchorNode)
}
```

### 3. Location Services

**Continuous Tracking**:
```kotlin
val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

val locationRequest = LocationRequest.create().apply {
    interval = 5000 // 5 seconds
    fastestInterval = 2000
    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
}

fusedLocationClient.requestLocationUpdates(
    locationRequest,
    locationCallback,
    Looper.getMainLooper()
)
```

---

## Security Considerations

### 1. API Key Protection

- Store in `gradle.properties` (not version controlled)
- Use BuildConfig for access
- Production: Use Google Cloud Secret Manager

### 2. Payment Security

- Never store card numbers locally
- Use tokenization (Apple Pay/Google Pay)
- PCI DSS compliance for backend

### 3. Location Privacy

- Request permission only when needed
- Clear explanation of why location is needed
- Option to delete location history

### 4. NFC Security

- Encrypted vehicle ID exchange
- Time-limited unlock tokens
- Backend validation before door unlock

---

## Testing Strategy

### 1. Unit Tests (ViewModels)

```kotlin
@Test
fun `predict destination returns home at evening`() = runTest {
    val mockService = MockGeminiService()
    val viewModel = HomeViewModel(mockService)
    
    advanceUntilIdle()
    
    val state = viewModel.uiState.value
    assertTrue(state is HomeUiState.Prediction)
    assertEquals("Home", (state as HomeUiState.Prediction).destination.name)
}
```

### 2. UI Tests (Compose)

```kotlin
@Test
fun testOnboardingFlow() {
    composeTestRule.setContent {
        OnboardingScreen(onComplete = { })
    }
    
    composeTestRule.onNodeWithText("Welcome to AvaRide").assertIsDisplayed()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("What's your name?").assertIsDisplayed()
}
```

### 3. Integration Tests

- Mock Gemini API responses
- Test navigation flows
- Verify state transitions

---

## Accessibility Features

### 1. Current Implementation

- High contrast colors (WCAG AA compliant)
- Large touch targets (48dp minimum)
- Semantic labels for icons
- Screen reader support (TalkBack)

### 2. Future Enhancements

- Voice-only mode (no screen needed)
- Large text option
- Haptic feedback for all interactions
- Color blind mode (adjust gradients)

---

## Deployment Strategy

### Development
- Debug builds with mock data
- Local Gemini API testing
- Emulator testing

### Staging
- TestFlight/Internal testing
- Real Gemini API with test data
- Beta user feedback

### Production
- Play Store release
- Phased rollout (5% → 25% → 100%)
- A/B testing for UI variations
- Analytics integration (Firebase)

---

## Performance Benchmarks

### Target Metrics

- **Cold start**: < 2 seconds
- **Home screen prediction**: < 1.5 seconds
- **Animation FPS**: 60fps (no jank)
- **Memory usage**: < 150MB
- **Battery drain**: < 5% per 30min ride

### Optimization Tools

- Android Profiler (CPU, Memory, Network)
- Compose Layout Inspector
- Systrace for frame drops
- LeakCanary for memory leaks

---

## Future Roadmap

### Q2 2026: Enhanced AI
- Voice commands ("Take me home")
- Multi-language support
- Context-aware notifications

### Q3 2026: Social Features
- Shared rides with friends
- Trip splitting
- Ride history sharing

### Q4 2026: Enterprise
- Corporate accounts
- Expense reporting
- Admin dashboard

---

## Conclusion

AvaRide demonstrates how AI and thoughtful design can transform complex workflows into simple, delightful experiences. By adapting Apple's Intelligence aesthetic to Android, we've created a reference implementation for the future of autonomous transportation apps.

**Key Achievements**:
✅ Zero-UI philosophy implemented  
✅ Gemini AI integration for predictions  
✅ Glassmorphism and fluid animations  
✅ Clean architecture with testability  
✅ Scalable for production deployment  

**Perfect for**: Design portfolios, AV/mobility startups, Android UI/UX showcases

