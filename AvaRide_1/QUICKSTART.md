# AvaRide - Quick Start Guide

## 🚀 Getting Started in 5 Minutes

### Step 1: Clone/Open the Project
Open this project in Android Studio Hedgehog (2023.1.1) or later.

### Step 2: Configure API Key

1. **Get a Gemini API Key**:
   - Visit: https://makersuite.google.com/app/apikey
   - Sign in with your Google account
   - Click "Create API Key"
   - Copy the generated key

2. **Add to gradle.properties**:
   - Open `gradle.properties` in the project root
   - Find the line: `GEMINI_API_KEY=your_gemini_api_key_here`
   - Replace `your_gemini_api_key_here` with your actual API key

   Example:
   ```properties
   GEMINI_API_KEY=AIzaSyAbc123def456ghi789jkl012mno345pqr
   ```

### Step 3: Sync Gradle
- Click "Sync Now" when Android Studio prompts
- Or: File → Sync Project with Gradle Files
- Wait for dependencies to download (~2-3 minutes first time)

### Step 4: Run the App
- Select an emulator or physical device (Android 7.0+ / API 24+)
- Click Run ▶️ (or Shift+F10)
- The app will launch showing the onboarding screen

---

## 📱 Testing the App Flow

### 1. Onboarding (First Launch)
- Watch the welcome animation
- Enter your name
- Select "Apple Pay" as payment
- Enable biometric auth
- Tap "Get Started"

### 2. Home Screen (Predictive)
- The app will show a loading orb
- After ~2 seconds, it predicts a destination
- Example: "Heading Home?"
- Tap "Book Ride" to proceed

### 3. Booking Flow
- See "Finding your vehicle..." animation
- Auto-advances to NFC unlock after 3 seconds

### 4. NFC Unlock
- "Tap to Unlock" screen appears
- (Simulated - no actual NFC needed)
- Auto-advances to In-Ride after 2 seconds

### 5. In-Ride Experience
- See the journey progress ring
- Try adjusting temperature (tap +/-)
- Change mood lighting (tap different modes)
- Ride auto-completes after 10 seconds

### 6. Ride Summary
- See total cost and "You've Arrived"
- Payment info shows "Paid with Google Pay"
- Rate with 👍 or 👎
- Auto-dismisses after 5 seconds back to Home

### 7. Settings (Optional)
- Tap the profile avatar (top-right on Home)
- Explore Control Center-style settings
- Toggle "Quiet Mode"
- Tap outside to dismiss

---

## 🎨 Visual Features to Notice

### Glowing Mesh Gradient
- Background slowly animates with moving gradients
- Creates depth and organic feel
- Changes color palette per screen

### Frosted Glass Cards
- Semi-transparent white cards with blur
- Content appears to float above background
- iOS-inspired glassmorphism effect

### Pulsating Orb
- Siri-style loading indicator
- Scale and opacity animations
- Used in loading and setup states

### Progress Ring
- Smooth animated arc drawing
- Gradient stroke (blue to purple)
- Real-time percentage display

### Spring Animations
- Cards scale down when tapped
- Smooth bouncy transitions
- Natural physics-based motion

---

## 🔧 Troubleshooting

### "GEMINI_API_KEY not found" Error
**Problem**: API key not configured  
**Solution**: 
1. Open `gradle.properties`
2. Add: `GEMINI_API_KEY=your_actual_key`
3. Sync Gradle again

### Gradle Sync Failed
**Problem**: Dependencies can't download  
**Solutions**:
- Check internet connection
- File → Invalidate Caches → Restart
- Update Gradle: `./gradlew wrapper --gradle-version 8.4`

### App Crashes on Launch
**Problem**: Missing dependency or API issue  
**Solutions**:
1. Check Logcat for error messages
2. Verify all dependencies synced successfully
3. Clean build: Build → Clean Project → Rebuild

### Gemini API Returns Errors
**Problem**: API key invalid or quota exceeded  
**Solutions**:
- Verify API key is correct in gradle.properties
- Check Google Cloud Console for quota limits
- Ensure billing is enabled (if required)

### Animations Are Laggy
**Problem**: Emulator performance  
**Solutions**:
- Use a physical device for best experience
- In emulator settings: Graphics → Hardware
- Enable "GPU acceleration" in AVD settings

---

## 📝 Customization Guide

### Change Color Scheme
Edit `ui/theme/Theme.kt`:
```kotlin
private val AvaRideDarkScheme = darkColorScheme(
    primary = Color(0xFF0A84FF),    // Change to your brand color
    secondary = Color(0xFF5E5CE6),
    // ...
)
```

### Adjust Animation Speed
In any screen file, modify duration:
```kotlin
animationSpec = tween(durationMillis = 1000) // Change 1000 to desired ms
```

### Change Gradient Colors
In `GlowingMeshGradient.kt`:
```kotlin
colors = listOf(
    Color(0xFF1A1A2E),  // Replace with your colors
    Color(0xFF16213E),
    // ...
)
```

### Mock Different Predictions
In `HomeViewModel.kt`, modify the mock context:
```kotlin
recentDestinations = listOf(
    Destination(name = "Gym", address = "...", ...)
)
```

---

## 🧪 Testing with Different Scenarios

### Test Prediction Confidence
The UI changes based on prediction confidence:
- **High (>70%)**: Shows green "High confidence" indicator
- **Medium**: Shows prediction with search option
- **Low**: Falls back to search

Currently hardcoded in mock data. To test, modify `GeminiPredictiveService.kt`:
```kotlin
confidence = 0.95f // High confidence
confidence = 0.50f // Medium confidence
confidence = 0.30f // Low confidence
```

### Test Different Times of Day
Gemini prompt includes time context. To simulate:
```kotlin
// In HomeViewModel.getCurrentUserContext()
hourOfDay = 17  // 5 PM (predicts "Home")
hourOfDay = 8   // 8 AM (predicts "Work")
hourOfDay = 12  // Noon (predicts lunch spot)
```

### Test Error States
Force error in `HomeViewModel.loadPrediction()`:
```kotlin
_uiState.value = HomeUiState.Error("No network connection")
```

---

## 🎯 Next Steps

### For Development
1. **Add Real API Integration**:
   - Replace mock vehicle data with backend API
   - Implement real-time GPS tracking
   - Add payment processing (Stripe SDK)

2. **Implement ARCore**:
   - See `ARCHITECTURE.md` for ARCore setup
   - Create AR wayfinding scene
   - Add vehicle identification tags

3. **Add NFC Support**:
   - Request NFC permission
   - Implement NFC reader mode
   - Add secure vehicle unlock protocol

### For Design
1. **Refine Animations**:
   - Add more micro-interactions
   - Implement haptic feedback
   - Add sound effects (optional)

2. **Create Additional Screens**:
   - Manual destination search
   - Ride history list
   - Payment methods management
   - Help & support center

3. **A/B Testing**:
   - Test different prediction UI layouts
   - Compare rating systems (stars vs thumbs)
   - Optimize color schemes for readability

---

## 📚 Additional Resources

### Documentation
- `README.md` - Project overview and features
- `ARCHITECTURE.md` - Detailed technical architecture
- `gradle/libs.versions.toml` - Dependency versions

### Key Files to Explore
- **UI Components**: `presentation/components/`
- **Screen Implementations**: `presentation/screens/`
- **AI Integration**: `data/remote/GeminiPredictiveService.kt`
- **Theme Customization**: `ui/theme/Theme.kt`

### External Links
- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Google Gemini API](https://ai.google.dev/)
- [Material Design 3](https://m3.material.io/)
- [ARCore Developer Guide](https://developers.google.com/ar)

---

## ❓ FAQ

**Q: Can I use this in production?**  
A: This is a reference implementation. You'll need to add:
- Backend API integration
- Real payment processing
- User authentication
- Database for ride history
- Legal/privacy compliance

**Q: Does this work on iOS?**  
A: No, this is Android-only. For iOS, see README for SwiftUI adaptation notes.

**Q: How much does Gemini API cost?**  
A: Free tier includes generous quota. Check current pricing at ai.google.dev/pricing

**Q: Can I customize the UI completely?**  
A: Yes! All Compose code is modular. Replace components, colors, and animations freely.

**Q: Is NFC/ARCore required?**  
A: No, both are optional features. The app works without them (using fallbacks).

**Q: How do I disable the predictive feature?**  
A: In `HomeScreen.kt`, replace `PredictiveDestinationCard` with a search input.

---

## 🤝 Contributing

This is a showcase project, but improvements welcome:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

Focus areas:
- Performance optimizations
- Accessibility improvements
- Additional screen implementations
- Better error handling
- Unit test coverage

---

## 📄 License

This project is provided as-is for educational and portfolio purposes.

---

## 🌟 Show Your Support

If you found this helpful:
- ⭐ Star the repository
- 📱 Share screenshots of your customizations
- 🐛 Report issues you find
- 💡 Suggest new features

**Built with passion for great design and user experience!**

---

**Need Help?** Check `ARCHITECTURE.md` for deep technical details or open an issue on GitHub.

