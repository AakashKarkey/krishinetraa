# KrishiNetra Mobile — Android App

## Overview

KrishiNetra Mobile is an Android companion app for the KrishiNetra potato leaf disease detection system. Users can upload plant photos, receive AI-powered disease diagnoses, and chat with a plant care assistant.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Architecture | MVVM (Model-View-ViewModel) |
| UI | XML layouts + Material 3 Design |
| DI | Manual (no DI framework — constructor injection) |
| Navigation | Navigation Component (single-activity, multi-fragment) |
| Networking | Retrofit 2 + OkHttp + Gson |
| Local storage | Room (chat messages) |
| Image loading | Glide |
| Image capture | CameraX via `ActivityResultContracts.TakePicture` |
| Build | Gradle with version catalog (`libs.versions.toml`) |

---

## Project Structure

```
app/src/main/java/com/ace/krishinetra_mobile/
├── KrishiNetraApp.kt              # Application class (Room DB init)
├── MainActivity.kt                 # Single activity, bottom nav, NavHost
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt         # Room database (singleton)
│   │   └── ChatMessageDao.kt      # DAO: insert, get, delete messages
│   ├── model/
│   │   ├── ChatMessage.kt         # Room entity (id, text, isUser, timestamp)
│   │   └── PredictionResponse.kt  # API response model
│   ├── remote/
│   │   ├── ApiService.kt          # Retrofit interface (POST /predict)
│   │   └── RetrofitClient.kt      # Retrofit singleton
│   └── repository/
│       ├── AnalysisRepository.kt  # Image upload + prediction call
│       └── ChatRepository.kt      # Wraps ChatMessageDao
├── navigation/
│   └── BottomNavItem.kt           # Sealed class for bottom nav items
├── ui/
│   ├── adapters/
│   │   ├── ChatAdapter.kt         # RecyclerView adapter for chat bubbles
│   │   └── ProbabilityAdapter.kt  # Reusable probability bars
│   ├── analyze/
│   │   └── AnalyzeFragment.kt     # Upload, camera, progress, results
│   ├── auth/
│   │   ├── SignInFragment.kt      # Sign in form
│   │   └── SignUpFragment.kt      # Sign up form
│   ├── chat/
│   │   └── ChatFragment.kt        # Full-screen chat with quick questions
│   ├── home/
│   │   └── HomeFragment.kt        # Landing: hero, features, how-it-works, FAQ
│   └── profile/
│       └── ProfileFragment.kt     # Profile card, auth state toggle
├── utils/
│   ├── Constants.kt               # API URL, disease info library
│   ├── Extensions.kt              # Bitmap/Uri helpers (rotate, resize, toByteArray)
│   └── ImageUtils.kt              # File name, size, cache copy helpers
└── viewmodel/
    ├── AnalyzeViewModel.kt        # Analysis state, progress simulation, API call
    ├── AuthViewModel.kt           # Validation (email, password, match)
    ├── ChatViewModel.kt           # Messages flow, AI keyword-based response
    ├── HomeViewModel.kt           # (empty — reserved)
    └── ProfileViewModel.kt        # Login/signout state
```

**Resources (under `app/src/main/res/`):**

| Directory | Contents |
|---|---|
| `layout/` | `activity_main.xml`, `fragment_*.xml`, `item_*.xml` |
| `navigation/` | `nav_graph.xml` (5 fragment destinations + 2 actions) |
| `menu/` | `bottom_nav_menu.xml` (Home, Analyze, Chat, Profile) |
| `drawable/` | 25+ vector drawables (icons, backgrounds, badges) |
| `values/` | `colors.xml`, `strings.xml`, `themes.xml` |
| `values-night/` | Dark theme overrides |
| `color/` | `bottom_nav_color.xml` (selector) |
| `xml/` | `file_paths.xml` (camera FileProvider), `backup_rules.xml`, `data_extraction_rules.xml` |

---

## Screens & Navigation

### Bottom Navigation (4 tabs)

| Tab | Fragment | Route ID | Description |
|---|---|---|---|
| Home | `HomeFragment` | `homeFragment` | Hero section, feature cards, how-it-works steps, FAQ accordion |
| Analyze | `AnalyzeFragment` | `analyzeFragment` | Image upload (gallery/camera), progress bar, analysis result card |
| Chat | `ChatFragment` | `chatFragment` | AI plant assistant with message bubbles and quick questions |
| Profile | `ProfileFragment` | `profileFragment` | Profile card, analysis stats, sign in/up/out |

### Auth Screens (bottom nav hidden)

| Screen | Fragment | Route ID |
|---|---|---|
| Sign In | `SignInFragment` | `signInFragment` |
| Sign Up | `SignUpFragment` | `signUpFragment` |

Navigation actions:
- `ProfileFragment` → `SignInFragment` (via `action_profile_to_signIn`)
- `ProfileFragment` → `SignUpFragment` (via `action_profile_to_signUp`)

---

## API Endpoint

| Method | Path | Purpose |
|---|---|---|
| `POST` | `http://10.0.2.2:8000/predict` | Upload image + get disease prediction |

**Request:** `multipart/form-data` with field name `file`.

**Response shape:**
```json
{
  "class": "Early Blight",
  "confidence": 0.92,
  "probabilities": { "Early Blight": 0.92, "Late Blight": 0.05, "Healthy": 0.03 },
  "model": "ResNet50",
  "processing_time_s": 0.85,
  "description": "...",
  "treatment": "...",
  "prevention_tips": ["...", "..."]
}
```

**Note:** `10.0.2.2` is the Android emulator alias for the host machine's `localhost`. Change `BASE_URL` in `Constants.kt` for physical devices.

---

## ViewModels

### AnalyzeViewModel
- **State:** `AnalyzeUiState(selectedUri, isUploading, uploadProgress, result, error)`
- **Actions:** `setImage(uri)`, `analyze()`, `clearResult()`
- Simulates upload progress (0→95%) while the API call runs.
- Calls `AnalysisRepository.analyzeImage()` via coroutine.

### ChatViewModel
- Exposes `messages: LiveData<List<ChatMessage>>` from Room.
- Sends welcome message on init if chat is empty.
- `sendMessage(text)` — inserts user message, simulates 1s delay, then inserts AI response.
- AI uses keyword-based response generation (water, yellow, disease, fertilizer, humidity, light, pest, repot).
- `clearChat()` — deletes all messages and re-sends welcome.

### AuthViewModel
- `signIn(email, password)` — validates format, min length.
- `signUp(name, email, password, confirmPassword)` — validates all fields, password match.
- State: `AuthUiState(isLoading, error, isSuccess)`.

### ProfileViewModel
- Simple state: `ProfileUiState(isLoggedIn, userName, userEmail, analysisCount)`.
- `login()`, `signUp()`, `signOut()` — toggle the logged-in state.

---

## Disease Info Library

When the API doesn't return `description`/`treatment`/`prevention_tips`, the app falls back to a built-in library in `Constants.kt`:

- **Early Blight** — fungal (Alternaria solani), concentric ring spots, chlorothalonil/mancozeb treatment
- **Late Blight** — oomycete (Phytophthora infestans), water-soaked lesions, metalaxyl treatment
- **Healthy** — no disease detected, general care recommendations

---

## Key Design Decisions

1. **Single Activity** — `MainActivity` hosts a `NavHostFragment` + `BottomNavigationView`. All screens are fragments.

2. **ViewBinding** — enabled in `build.gradle.kts`. All fragments use `fragment_*_binding` inflate pattern.

3. **No Hilt/Dagger** — ViewModels use `by viewModels()` with `AndroidViewModel(application)` for simple constructor injection.

4. **Auth is local-only** — no backend auth. Profile state is held in `ProfileViewModel` (shared across fragments via shared ViewModel or passed state). `SignInFragment` and `SignUpFragment` validate input locally and set the profile state.

5. **Chat is client-side** — AI responses use keyword matching, not a real API. Extend `ChatViewModel.generateResponse()` to call an actual LLM endpoint.

6. **Camera** — uses `TakePicture` contract with a `FileProvider` to save to cache. `ic_launcher_foreground.xml` adapted from the original project.

---

## Building & Running

```bash
cd mobile
./gradlew assembleDebug
# APK at: app/build/outputs/apk/debug/app-debug.apk
```

Open the `mobile/` folder in Android Studio — it will sync Gradle automatically and you can run on an emulator or physical device.

### Prerequisites
- Android Studio Ladybug+ (2024.3+) with AGP 9.0.1
- JDK 17
- Android SDK 36
- Gradle 9.1 (bundled wrapper)

### Emulator networking
The backend URL `http://10.0.2.2:8000` points to host machine's `localhost`. For physical devices, update `Constants.kt` with your machine's LAN IP.

---

## Dependencies (from `gradle/libs.versions.toml`)

| Group | Artifact | Version |
|---|---|---|
| AndroidX Core | core-ktx | 1.18.0 |
| AndroidX Activity | activity | 1.13.0 |
| AndroidX Fragment | fragment-ktx | 1.8.6 |
| AndroidX Navigation | navigation-fragment-ktx | 2.8.9 |
| AndroidX Lifecycle | viewmodel-ktx, livedata-ktx, runtime-ktx | 2.9.0 |
| AndroidX Room | room-runtime, room-ktx, room-compiler | 2.7.1 |
| AndroidX ViewPager2 | viewpager2 | 1.1.0 |
| AndroidX ExifInterface | exifinterface | 1.4.0 |
| Material | material | 1.14.0 |
| Retrofit | retrofit, converter-gson | 2.11.0 |
| OkHttp | logging-interceptor | 4.12.0 |
| Glide | glide | 4.16.0 |
| Kotlinx Coroutines | core, android | 1.9.0 |

---

## Troubleshooting

### `NavController not set on FragmentContainerView`
Make sure `MainActivity` retrieves the controller from `NavHostFragment`:
```kotlin
val navHostFragment = supportFragmentManager
    .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
val navController = navHostFragment.navController
```

### `android:cx not found` in vector drawables
Use `<path>` with `m`/`a` commands instead of `<circle>`/`<rect>` for minSdk compatibility, or remove the `android:` prefix from geometry attributes.

### `android.builtInKotlin=false` deprecation warning
AGP 9+ has built-in Kotlin support. To migrate:
1. Remove `kotlin("android")` and `kotlin("kapt")` plugins
2. Replace kapt with KSP (`com.google.devtools.ksp`)
3. Use Room KSP artifact (`room-compiler` → KSP)
4. Set `compilerOptions { jvmTarget = "17" }` instead of `kotlinOptions`
5. Remove `android.builtInKotlin=false` and `android.newDsl=false` from `gradle.properties`
