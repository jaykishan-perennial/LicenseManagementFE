# License Manager (Android)

Android Kotlin application for the License Management System.

## Features

- **Authentication**: Sign in and sign up against `sdk/auth/login` and `sdk/auth/signup`; session is backed by an API key stored on device.
- **Subscription overview**: View current subscription status on the home screen.
- **Subscription packs**: Browse packs and request a subscription.
- **History**: Paginated subscription history with sorting.
- **Deactivate**: Cancel the active subscription from the app.
- **UX**: View binding, edge-to-edge layouts, pull-to-refresh where applicable, and Snackbar feedback for errors.

## Project structure

```
app/
├── build.gradle.kts
└── src/main/
    ├── AndroidManifest.xml
    ├── java/com/licensemanager/app/
    │   ├── LicenseManagerApp.kt
    │   ├── data/
    │   │   ├── remote/
    │   │   │   ├── ApiClient.kt
    │   │   │   ├── ApiService.kt
    │   │   │   └── dto/
    │   │   │       ├── AuthDto.kt
    │   │   │       └── SubscriptionDto.kt
    │   │   └── repository/
    │   │       ├── AuthRepository.kt
    │   │       └── SubscriptionRepository.kt
    │   ├── ui/
    │   │   ├── common/
    │   │   │   └── UiState.kt
    │   │   ├── history/
    │   │   │   ├── SubscriptionHistoryActivity.kt
    │   │   │   ├── SubscriptionHistoryAdapter.kt
    │   │   │   └── SubscriptionHistoryViewModel.kt
    │   │   ├── login/
    │   │   │   ├── LoginActivity.kt
    │   │   │   └── LoginViewModel.kt
    │   │   ├── main/
    │   │   │   ├── MainActivity.kt
    │   │   │   └── MainViewModel.kt
    │   │   ├── register/
    │   │   │   ├── RegisterActivity.kt
    │   │   │   └── RegisterViewModel.kt
    │   │   └── request/
    │   │       ├── PackAdapter.kt
    │   │       ├── RequestSubscriptionActivity.kt
    │   │       └── RequestSubscriptionViewModel.kt
    │   └── util/
    │       ├── Constants.kt
    │       ├── FlowExtensions.kt
    │       └── PreferenceManager.kt
    └── res/
        ├── drawable/
        ├── layout/
        ├── menu/
        ├── mipmap-*/
        ├── values/
        └── xml/
            └── network_security_config.xml
```

## Setup

### Prerequisites

- Android Studio (recommended) or JDK 17 and the Android SDK
- A running backend instance (default port **8080** in this project)

### Update API base URL

Retrofit’s base URL is defined in **`app/src/main/java/com/licensemanager/app/util/Constants.kt`** as `Constants.BASE_URL` (used when building `Retrofit` in `ApiClient.kt`).

| Environment | Base URL |
|-------------|----------|
| **Android Emulator** | `http://10.0.2.2:8080/` — special alias to the host machine’s loopback |
| **Physical device** | `http://YOUR_COMPUTER_LAN_IP:8080/` — use your computer’s IP on the same Wi‑Fi as the phone |

Ensure the device can reach the server (firewall, `usesCleartextTraffic`, and `network_security_config` are already set for HTTP in debug-style use).

### Build the project

From the **`android/`** directory:

```bash
./gradlew build
```

### Run the app

Install a debug build on a connected device or emulator:

```bash
./gradlew installDebug
```

Then open the app from the launcher (**LoginActivity** is the entry activity).

## Dependencies

Versions are taken from `app/build.gradle.kts`:

| Library | Role |
|---------|------|
| **Retrofit** `2.11.0` + **converter-gson** | HTTP API client and Gson integration |
| **OkHttp** `4.12.0` + **logging-interceptor** | HTTP stack and request/response logging |
| **Gson** (via Retrofit converter) | JSON serialization |
| **Material** `1.12.0` | Material Design components |
| **AndroidX AppCompat, ConstraintLayout, CoordinatorLayout, SwipeRefreshLayout** | UI and layout |
| **Lifecycle** `lifecycle-viewmodel-ktx`, `lifecycle-runtime-ktx` | ViewModels and lifecycle-aware coroutine APIs |
| **Kotlin Coroutines** (`kotlinx-coroutines-android`, `core`) | Async work and UI state flows |
| **View binding** | Type-safe view access |

> **Note:** Navigation is done with **Activities** and `Intent`s; there is no Navigation Component dependency in this module.

## API integration

The app uses **Retrofit** for all network calls. Endpoints are declared in **`ApiService.kt`** and align with the backend **OpenAPI** specification (e.g. `openapi.yaml` in the repo root).

Typical paths used include:

- `POST sdk/auth/login`, `POST sdk/auth/signup`
- `GET` / `POST` / `DELETE sdk/v1/subscription`
- `GET sdk/v1/subscription-packs`
- `GET sdk/v1/subscription-history`

## Authentication flow

1. The user signs in from **`LoginActivity`** (or registers via **`RegisterActivity`**).
2. On success, the backend returns an **API key**; it is persisted in **SharedPreferences** through **`PreferenceManager`** (`AuthRepository` saves it after login/signup).
3. **`ApiClient`** attaches the key to outgoing requests with the **`X-API-Key`** header when a key is present.

If you extend the app to use **Bearer** tokens for non-SDK routes, add an OkHttp interceptor similar to the existing API-key interceptor and store the token in `PreferenceManager` (or a dedicated store).

## SDK integration

For SDK-style access, the app already uses **`sdk/auth/login`** (and signup), which returns an API key. Store that key (as implemented) and send it on SDK routes with **`X-API-Key`**, matching the backend’s SDK expectations.

## Configuration

Set the base URL in **`Constants.kt`**:

```kotlin
// For a physical device — replace with your machine's IP
const val BASE_URL = "http://YOUR_SERVER_IP:8080/"
```

For **local development on the Android Emulator**:

```kotlin
const val BASE_URL = "http://10.0.2.2:8080/"
```

The default in source may be `http://localhost:8080/`; that only works on the host, not inside the emulator—switch to **`10.0.2.2`** for emulator testing.

