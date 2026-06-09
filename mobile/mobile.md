# KrishiNetra Mobile — Android App

## Overview

KrishiNetra Mobile is an Android companion app for the KrishiNetra potato leaf disease detection system. Users can upload plant photos, receive AI-powered disease diagnoses, and chat with a plant care assistant.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.3.0 |
| Architecture | MVVM (Model-View-ViewModel) |
| UI | XML layouts + Material 3 Design + custom animations |
| DI | Manual (no DI framework — constructor injection) |
| Navigation | Navigation Component (single-activity, multi-fragment) |
| Networking | Retrofit 2 + OkHttp + Gson |
| Local storage | Room 2.8.4 (chat messages) via KSP |
| Auth | Clerk Android SDK 1.0.27 (optional — fallback to local mock) |
| Image loading | Glide |
| Image capture | CameraX via `ActivityResultContracts.TakePicture` |
| Build | Gradle 9.1 with version catalog (`libs.versions.toml`) |

---

## Project Structure

```
app/src/main/java/com/ace/krishinetra_mobile/
├── KrishiNetraApp.kt              # Application class (Room DB, optional Clerk init)
├── MainActivity.kt                 # Single activity, bottom nav, NavHost
├── SplashActivity.kt               # Animated splash (logo scale+fade → MainActivity)
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
│   │   ├── SignInFragment.kt      # Sign in form (uses Clerk or local auth)
│   │   └── SignUpFragment.kt      # Sign up form (uses Clerk or local auth)
│   ├── chat/
│   │   └── ChatFragment.kt        # Full-screen chat with quick questions
│   ├── home/
│   │   └── HomeFragment.kt        # Dashboard: stats, quick actions, how-it-works
│   └── profile/
│       └── ProfileFragment.kt     # Profile card, auth state toggle, sign out
├── utils/
│   ├── Constants.kt               # API URL, disease info library
│   ├── Extensions.kt              # Bitmap/Uri helpers (rotate, resize, toByteArray)
│   ├── ImageUtils.kt              # File name, size, cache copy helpers
│   ├── ToastType.kt               # Enum: SUCCESS, ERROR, INFO
│   └── Toaster.kt                 # Custom animated toast (Material3-styled)
└── viewmodel/
    ├── AnalyzeViewModel.kt        # Analysis state, progress simulation, API call
    ├── AuthViewModel.kt           # Clerk SDK sign-in/sign-up (falls back to local)
    ├── ChatViewModel.kt           # Messages flow, AI keyword-based response
    ├── HomeViewModel.kt           # (empty — reserved)
    └── ProfileViewModel.kt        # Clerk user session or local mock auth
```

**Resources (under `app/src/main/res/`):**

| Directory | Contents |
|---|---|
| `layout/` | `activity_main.xml`, `activity_splash.xml`, `fragment_*.xml`, `item_*.xml` |
| `navigation/` | `nav_graph.xml` (5 fragment destinations + 2 actions) |
| `menu/` | `bottom_nav_menu.xml` (Home, Analyze, Chat, Profile) |
| `drawable/` | 30+ drawables (backgrounds, gradients, bubble shapes, logo.png, stat cards) |
| `anim/` | `fade_in.xml`, `fade_out.xml`, `slide_up.xml`, `slide_down.xml` |
| `values/` | `colors.xml`, `strings.xml`, `themes.xml` |
| `values-night/` | Dark theme overrides |
| `color/` | `bottom_nav_color.xml` (selector) |
| `xml/` | `file_paths.xml` (camera FileProvider), `backup_rules.xml`, `data_extraction_rules.xml` |

---

## Screens & Navigation

### Flow

```
SplashActivity (animated 2s)
  └── MainActivity (bottom nav visible)
        ├── HomeFragment       — dashboard stats, quick actions
        ├── AnalyzeFragment    — image upload + analysis
        ├── ChatFragment       — AI plant assistant
        └── ProfileFragment    — auth section or user profile
              ├── SignInFragment   (nav hidden)
              └── SignUpFragment   (nav hidden)
```

### Bottom Navigation (4 tabs)

| Tab | Fragment | Route ID | Description |
|---|---|---|---|
| Home | `HomeFragment` | `homeFragment` | Dashboard: stats cards, quick actions, how-it-works |
| Analyze | `AnalyzeFragment` | `analyzeFragment` | Image upload (gallery/camera), progress bar, result card |
| Chat | `ChatFragment` | `chatFragment` | AI plant assistant with typing indicator + quick questions |
| Profile | `ProfileFragment` | `profileFragment` | Cover photo, avatar, auth or profile info |

### Auth Screens (bottom nav hidden)

| Screen | Fragment | Route ID |
|---|---|---|
| Sign In | `SignInFragment` | `signInFragment` |
| Sign Up | `SignUpFragment` | `signUpFragment` |

Navigation actions:
- `ProfileFragment` → `SignInFragment` (via `action_profile_to_signIn`)
- `ProfileFragment` → `SignUpFragment` (via `action_profile_to_signUp`)

### Navigation Animations
- `fade_in.xml` / `fade_out.xml` — for auth screen transitions
- `slide_up.xml` / `slide_down.xml` — for bottom-to-top transitions
- Applied in `nav_graph.xml` via `<action>` `enterAnim`/`exitAnim`/`popEnterAnim`/`popExitAnim`

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
- **With Clerk enabled:** calls `SignIn.create(...)` / `SignUp.create(...)` via Clerk Android SDK
- **Clerk disabled (fallback):** validates fields locally, succeeds immediately
- State: `AuthUiState(isLoading, error, isSuccess)`

### ProfileViewModel
- **With Clerk enabled:** observes `Clerk.userFlow` for real-time user data; `signOut()` calls `Clerk.auth.signOut()`
- **Clerk disabled (fallback):** uses local mock `ProfileUiState` with email-derived username
- State: `ProfileUiState(isLoggedIn, userName, userEmail, analysisCount)`

---

## Clerk Integration

The app supports Clerk as an optional auth provider, mirroring the frontend's `@clerk/nextjs` integration.

### Setup

1. Get your Clerk Publishable Key from the [Clerk Dashboard](https://dashboard.clerk.com) (API Keys page).
2. Set it in `app/build.gradle.kts`:
   ```kotlin
   defaultConfig {
       buildConfigField("String", "CLERK_PUBLISHABLE_KEY", "\"pk_test_...\"")
   }
   ```
3. Rebuild. If the key is non-empty, Clerk initializes automatically in `KrishiNetraApp.onCreate()`.

### Fallback Behavior

When `CLERK_PUBLISHABLE_KEY` is empty (default), the app:
- Skips Clerk initialization
- Uses local mock auth (validates input locally, succeeds immediately)
- Shows a non-persistent profile with email-derived username
- All screens work identically

### Auth Flows

| Action | Clerk Enabled | Clerk Disabled |
|---|---|---|
| Sign In | `SignIn.create(Password(identifier, password))` | Local validation, immediate success |
| Sign Up | `SignUp.create(Standard(email, password))` | Local validation, immediate success |
| Profile | Observes `Clerk.userFlow` | Mock `ProfileUiState` |
| Sign Out | `Clerk.auth.signOut()` | Clears local state |

---

## Disease Info Library

When the API doesn't return `description`/`treatment`/`prevention_tips`, the app falls back to a built-in library in `Constants.kt`:

- **Early Blight** — fungal (Alternaria solani), concentric ring spots, chlorothalonil/mancozeb treatment
- **Late Blight** — oomycete (Phytophthora infestans), water-soaked lesions, metalaxyl treatment
- **Healthy** — no disease detected, general care recommendations

---

## Custom Toaster

`Toaster.kt` provides Material3-styled animated toasts with three types:

| Type | Icon | Background |
|---|---|---|
| `SUCCESS` | ✅ | Green (`#4CAF50`) |
| `ERROR` | ❌ | Red (`#F44336`) |
| `INFO` | ℹ️ | Blue (`#2196F3`) |

Usage: `Toaster.show(view, "message", ToastType.SUCCESS)`

Toasts slide in from the top with a shadow, auto-dismiss after 2.5s.

---

## New / Updated Drawables

| Drawable | Purpose |
|---|---|
| `logo.png` | Project logo (replaces `ic_leaf`/`ic_leaf_outline` throughout) |
| `bg_splash_gradient.xml` | Splash screen gradient background |
| `bg_splash_logo.xml` | Splash logo circular container |
| `bg_stat_card.xml` | Stats card / auth logo circular background |
| `bg_profile_header.xml` | Profile fragment cover photo gradient |
| `ic_leaf_outline.xml` | Kept for reference (no longer used in layouts) |
| `bg_upload_zone.xml`, `bg_chat_input.xml`, `bg_gradient_green_dark.xml` | Updated styling |
| `bg_bubble_user.xml`, `bg_bubble_ai.xml` | Chat bubble shapes |

---

## Key Design Decisions

1. **Single Activity** — `MainActivity` hosts a `NavHostFragment` + `BottomNavigationView`. All screens are fragments.

2. **Splash Screen** — `SplashActivity` is the launcher. Runs a 2s scale+fade animation on the logo, then navigates to `MainActivity` and calls `finish()`.

3. **ViewBinding** — enabled in `build.gradle.kts`. All fragments use `fragment_*_binding` inflate pattern.

4. **No Hilt/Dagger** — ViewModels use `by viewModels()` with `AndroidViewModel(application)` for simple constructor injection.

5. **Clerk auth (optional)** — Configure via `CLERK_PUBLISHABLE_KEY` build config field. Falls back to local mock auth when unset.

6. **Chat is client-side** — AI responses use keyword matching, not a real API. Extend `ChatViewModel.generateResponse()` to call an actual LLM endpoint.

7. **Camera** — uses `TakePicture` contract with a `FileProvider` to save to cache.

8. **KSP instead of KAPT** — Room uses Kotlin Symbol Processing (KSP) for faster compilation and better Kotlin version compatibility.

9. **Navigation animations** — fade/slide transitions applied to auth screen navigations in `nav_graph.xml`.

---

## Building & Running

```bash
cd mobile
./gradlew assembleDebug
# APK at: app/build/outputs/apk/debug/app-debug.apk
```

Open the `mobile/` folder in Android Studio — it will sync Gradle automatically and you can run on an emulator or physical device.

### Prerequisites
- Android Studio Otter+ (2025.2+) with AGP 9.0.1
- JDK 17
- Android SDK 36
- Gradle 9.1 (bundled wrapper)

### Emulator networking
The backend URL `http://10.0.2.2:8000` points to host machine's `localhost`. For physical devices, update `Constants.kt` with your machine's LAN IP.

### Clerk configuration
1. Get your Publishable Key from [clerk.com](https://dashboard.clerk.com)
2. Set it in `app/build.gradle.kts` → `defaultConfig` → `buildConfigField("String", "CLERK_PUBLISHABLE_KEY", "\"pk_test_...\"")`
3. Rebuild. The app automatically switches to Clerk auth.

---

## Dependencies (from `gradle/libs.versions.toml`)

| Group | Artifact | Version |
|---|---|---|
| AndroidX Core | core-ktx | 1.18.0 |
| AndroidX Activity | activity | 1.13.0 |
| AndroidX Fragment | fragment-ktx | 1.8.6 |
| AndroidX Navigation | navigation-fragment-ktx | 2.8.9 |
| AndroidX Lifecycle | viewmodel-ktx, livedata-ktx, runtime-ktx | 2.9.0 |
| AndroidX Room | room-runtime, room-ktx, room-compiler (KSP) | 2.8.4 |
| AndroidX ViewPager2 | viewpager2 | 1.1.0 |
| AndroidX ExifInterface | exifinterface | 1.4.0 |
| Material | material | 1.14.0 |
| Retrofit | retrofit, converter-gson | 2.11.0 |
| OkHttp | logging-interceptor | 4.12.0 |
| Glide | glide | 4.16.0 |
| Kotlinx Coroutines | core, android | 1.9.0 |
| Clerk | clerk-android-api | 1.0.27 |

---

## Recent Changes

### Logo Replacement
All `@drawable/ic_leaf` and `@drawable/ic_leaf_outline` references replaced with `@drawable/logo` in:
`activity_splash.xml`, `fragment_home.xml`, `fragment_chat.xml`, `fragment_sign_in.xml`, `fragment_sign_up.xml`, `fragment_profile.xml`

### Bottom Nav Fix
- `MainActivity.kt`: Returns `WindowInsetsCompat.CONSUMED` in `OnApplyWindowInsetsListener` to prevent Material from re-applying insets to the BottomNavigationView
- `activity_main.xml`: Added `app:layout_constraintTop_toBottomOf="@id/nav_host_fragment"` to `bottomNavShadow`

### Clerk Framework
- Added `com.clerk:clerk-android-api:1.0.27` dependency
- `KrishiNetraApp.kt`: Conditional Clerk initialization based on `BuildConfig.CLERK_PUBLISHABLE_KEY`
- `AuthViewModel.kt`: Rewritten with Clerk `SignIn.create` / `SignUp.create` calls; falls back to local validation
- `ProfileViewModel.kt`: Rewritten to observe `Clerk.userFlow`; falls back to mock data

### Kotlin & Toolchain Upgrade
- Kotlin: 2.1.0 → 2.3.0
- Room: 2.7.1 → 2.8.4
- KAPT → KSP for Room annotation processing
- `kotlinOptions` → `compilerOptions` in `build.gradle.kts`

---

## Troubleshooting

### `NavController not set on FragmentContainerView`
Make sure `MainActivity` retrieves the controller from `NavHostFragment`:
```kotlin
val navHostFragment = supportFragmentManager
    .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
val navController = navHostFragment.navController
```

### `Provided Metadata instance has version X while maximum supported version is Y`
Room's KAPT processor has a `kotlin-metadata-jvm` version cap. Fix:
1. Migrate Room from KAPT to KSP
2. Upgrade Room to latest (2.8.4+)
3. Upgrade Kotlin to match

### `android:cx not found` in vector drawables
Use `<path>` with `m`/`a` commands instead of `<circle>`/`<rect>` for minSdk compatibility, or remove the `android:` prefix from geometry attributes.

### Clerk not working
- Ensure `CLERK_PUBLISHABLE_KEY` is non-empty in `app/build.gradle.kts`
- Rebuild after setting the key
- Check Clerk Dashboard → Native Applications is enabled
