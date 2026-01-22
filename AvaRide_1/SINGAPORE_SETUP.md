# 🇸🇬 SINGAPORE LOCATIONS & TRAVEL HISTORY

**Update**: January 22, 2026  
**Purpose**: Enable AI predictions from first launch  
**Location**: Singapore (Punggol area)  
**Status**: ✅ CONFIGURED

---

## 📍 CONFIGURED LOCATIONS

### **🏠 Home Address**
```
Name: Home
Address: Blk 174 Punggol Field, Singapore 531174
Coordinates: 1.4010, 103.9070
Area: Punggol
Confidence: 95%
```

### **💼 Work Address**
```
Name: Work  
Address: SIT Punggol Campus, 10 Dover Drive, Singapore 138683
Coordinates: 1.4086, 103.9046
Area: Punggol
Confidence: 92%
```

### **🛍️ Common Destination**
```
Name: Waterway Point
Address: 83 Punggol Central, Singapore 828761
Coordinates: 1.4062, 103.9022
Area: Punggol (Shopping Mall)
Confidence: 75%
```

---

## 🤖 AI PREDICTION LOGIC

### **Time-Based Predictions**

The AI uses **time of day** and **travel history** to predict where you're going:

#### **Morning (6:00 AM - 9:00 AM)** ☀️
```
User likely at: Home
Prediction: "Heading to Work?"
Destination: SIT Punggol Campus
Reasoning: Commute time
```

#### **Evening (5:00 PM - 8:00 PM)** 🌆
```
User likely at: Work
Prediction: "Heading Home?"
Destination: Singapore 531174
Reasoning: End of work day
```

#### **Lunch Time (12:00 PM - 2:00 PM)** 🍽️
```
User likely at: Work
Prediction: "Going to Waterway Point?"
Destination: Waterway Point Mall
Reasoning: Lunch break, nearby mall
```

#### **Other Times** 🌙
```
Prediction: Based on most frequent destination
Priority: Home > Work > Mall
```

---

## 🎯 HOW IT WORKS

### **1. User Opens App**
App launches → Home screen loads

### **2. Context Gathering**
```kotlin
getCurrentUserContext() {
    currentLocation = "Orchard Road, Singapore"
    currentTime = System.currentTimeMillis()
    hourOfDay = 18 // 6 PM
    recentDestinations = [Work, Home, Mall]
}
```

### **3. AI Prediction**
```
Time: 6:00 PM (evening)
History: Recently at Work
Prediction: "Heading Home?"
```

### **4. Display on Home Screen**
```
┌─────────────────────────────────┐
│  Heading Home?                  │
│  Blk 174 Punggol Field          │
│  Singapore 531174               │
│                                 │
│  🚗 Standard AV                 │
│  ⏱️ 4 mins                       │
│  💳 $12.50                       │
│                                 │
│  [Book Ride]                    │
└─────────────────────────────────┘
```

---

## 🗺️ SINGAPORE MAP REFERENCE

```
                    Singapore

            ┌─────────────────┐
            │   Punggol Area  │
            │                 │
            │  🏠 Home (531174)│
            │  💼 SIT Punggol  │
            │  🛍️ Waterway Pt │
            └─────────────────┘
                    │
                    │ ~15 km
                    │
            ┌───────▼─────────┐
            │   City Center   │
            │                 │
            │  📍 Orchard Rd  │
            │  (Current Loc)  │
            └─────────────────┘
```

---

## 📊 TRAVEL HISTORY SIMULATION

### **Fake History Created:**

The app pre-populates travel history so AI predictions work **immediately** on first launch.

#### **Recent Trips (Simulated):**

| Time | From | To | Status |
|------|------|----|----|
| Morning | Home | Work | ✅ Regular commute |
| Lunch | Work | Waterway Point | ✅ Lunch trip |
| Evening | Work | Home | ✅ Return commute |
| Weekend | Home | Waterway Point | ✅ Shopping |

#### **Pattern Recognition:**

AI learns from this history:
- Weekday mornings → Work
- Weekday evenings → Home  
- Lunch hours → Mall (sometimes)
- Weekends → Mall or leisure

---

## 🎨 USER EXPERIENCE

### **First Time User Journey:**

1. **Sign Up** (30-40 seconds)
   - Enter phone number
   - Verify OTP
   - Enter name
   - Complete

2. **Home Screen** (Immediate!)
   - See AI prediction right away
   - "Heading Home?" or "Heading to Work?"
   - Based on current time
   - One-tap booking ready

3. **Book Ride**
   - Tap "Book Ride"
   - Booking confirmation
   - Vehicle arrives
   - NFC unlock
   - In-ride experience

---

## 🔄 DYNAMIC PREDICTIONS

### **How Predictions Change:**

#### **Scenario 1: Morning Commute**
```
Time: 8:00 AM
Day: Monday
Current Location: Near Home
Prediction: "Heading to Work?"
Confidence: 95%
```

#### **Scenario 2: Evening Return**
```
Time: 6:30 PM
Day: Monday
Current Location: Near Work
Prediction: "Heading Home?"
Confidence: 92%
```

#### **Scenario 3: Weekend**
```
Time: 2:00 PM
Day: Saturday
Current Location: Home
Prediction: "Going to Waterway Point?"
Confidence: 75%
```

---

## 🚀 BENEFITS

### **For Demo/Testing:**
- ✅ AI predictions work immediately
- ✅ No need to manually enter destinations
- ✅ Realistic Singapore locations
- ✅ Time-aware predictions
- ✅ Shows off AI capabilities

### **For User Experience:**
- ✅ Feels intelligent from day 1
- ✅ Learns user patterns
- ✅ Reduces input friction
- ✅ One-tap ride booking
- ✅ "Zero-UI" philosophy realized

---

## 🛠️ TECHNICAL IMPLEMENTATION

### **Code Location:**
```
File: HomeViewModel.kt
Function: getCurrentUserContext()
```

### **Key Changes:**
```kotlin
// Before: Generic SF location
currentLocation = Location(
    latitude = 37.7749,
    longitude = -122.4194,
    address = "San Francisco, CA"
)

// After: Real Singapore locations
currentLocation = Location(
    latitude = 1.3521,
    longitude = 103.8198,
    address = "Orchard Road, Singapore"
)

// Before: Single destination
recentDestinations = listOf(homeDestination)

// After: Time-based realistic history
recentDestinations = when (currentHour) {
    in 6..9 -> listOf(homeDestination, workDestination, mallDestination)
    in 17..20 -> listOf(workDestination, homeDestination, mallDestination)
    else -> listOf(homeDestination, workDestination, mallDestination)
}
```

---

## 🧪 TESTING

### **Test Morning Prediction:**
1. Set device time to 8:00 AM
2. Launch app
3. Expected: "Heading to Work?"
4. Destination: SIT Punggol Campus

### **Test Evening Prediction:**
1. Set device time to 6:00 PM
2. Launch app
3. Expected: "Heading Home?"
4. Destination: Singapore 531174

### **Test Lunch Prediction:**
1. Set device time to 1:00 PM
2. Launch app
3. Expected: "Going to Waterway Point?"
4. Destination: Waterway Point Mall

---

## 📈 PREDICTION ACCURACY

### **Confidence Scores:**

| Destination | Confidence | Reason |
|-------------|-----------|--------|
| Home (Evening) | 95% | Strong pattern |
| Work (Morning) | 92% | Daily commute |
| Mall (Lunch) | 75% | Occasional |
| Other | 60% | Exploratory |

---

## 🎯 GEMINI API CONTEXT

### **What Gets Sent to AI:**
```json
{
  "currentLocation": {
    "address": "Orchard Road, Singapore",
    "latitude": 1.3521,
    "longitude": 103.8198
  },
  "currentTime": 1737523200000,
  "hourOfDay": 18,
  "dayOfWeek": 2,
  "recentDestinations": [
    {
      "name": "Work",
      "address": "SIT Punggol Campus",
      "confidence": 0.92
    },
    {
      "name": "Home", 
      "address": "Singapore 531174",
      "confidence": 0.95
    },
    {
      "name": "Waterway Point",
      "address": "Punggol Central",
      "confidence": 0.75
    }
  ]
}
```

### **AI Response:**
```json
{
  "predictedDestination": {
    "name": "Home",
    "address": "Blk 174 Punggol Field, Singapore 531174",
    "confidence": 0.95,
    "reasoning": "User typically travels home from work around this time"
  }
}
```

---

## ✅ VERIFICATION CHECKLIST

After installation, verify:

- [ ] Home screen shows AI prediction immediately
- [ ] Prediction text is "Heading Home?" or "Heading to Work?"
- [ ] Singapore addresses are displayed
- [ ] Postal code 531174 appears for Home
- [ ] SIT Punggol appears for Work
- [ ] Confidence indicator shows (if implemented)
- [ ] "Book Ride" button works
- [ ] Prediction changes based on time of day

---

## 🎉 SUMMARY

**Singapore travel history configured:**
- ✅ Home: Singapore 531174 (Punggol Field)
- ✅ Work: SIT Punggol Campus
- ✅ Mall: Waterway Point
- ✅ Time-based predictions
- ✅ Realistic patterns
- ✅ AI works from day 1

**User Experience:**
- Opens app → Sees prediction immediately
- No manual destination entry needed
- Smart, contextual suggestions
- One-tap ride booking
- Feels like magic! ✨

---

*Updated: January 22, 2026*  
*Location: Singapore (Punggol)*  
*Status: Ready for Testing*

