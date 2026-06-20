# World Cup 2026 App

An Android app for following the FIFA World Cup 2026 — live scores, group standings, top scorers, team info, favorites, and community score predictions.

## Setup Instructions

### 1. Get your football-data.org API Key
- Go to https://www.football-data.org/client/register
- Register for free
- You will receive an API key by email

### 2. Add your API Key
Open this file:
```
app/src/main/java/com/worldcup/app/utils/Constants.kt
```
Replace `YOUR_API_KEY_HERE` with your actual API key:
```kotlin
const val API_KEY = "your_actual_key_here"
```

### 3. Set up Firebase (for Predictions feature)
- Create a Firebase project at https://console.firebase.google.com
- Add an Android app with package name `com.worldcup.app`
- Download `google-services.json` and place it in the `app/` folder
- Enable **Firestore Database** in the Firebase console

### 4. Build & Run
- Open in Android Studio
- Sync Gradle
- Run on device or emulator (API 26+)

## Project Structure
```
app/
├── data/
│   ├── local/                  ← Room database, DAOs, Entities (matches, favorites, scorers)
│   └── remote/
│       ├── api/                ← Retrofit API service (football-data.org)
│       ├── models/             ← API response DTOs
│       └── firebase/           ← Firestore Prediction model + repository
├── di/                         ← Hilt dependency injection (Retrofit, Room, Firestore)
├── repository/                 ← Single data access point (Network Bound Resource pattern)
├── ui/
│   ├── adapters/                ← RecyclerView adapters (Matches, Standings, Scorers, Teams, Favorites, Predictions)
│   ├── fragments/                ← Home, Matches, Standings, Scorers, Favorites, Teams, Settings
│   └── viewmodels/               ← Shared ViewModel (WorldCupViewModel)
├── utils/                       ← Constants, Resource, Extensions
└── workers/                      ← WorkManager background sync
```

## Features
- Live match scores & results — live matches surface first on the Home screen
- Group stage standings (by group, and overall) with ★ favorites
- Top scorers leaderboard
- All 48 teams with details & search
- Favorites list (Room) with swipe-to-delete
- **Score predictions (Firebase Firestore)** — predict the outcome of upcoming matches and see real-time community predictions; locked once a match starts
- Dark mode
- Background auto-sync (WorkManager, every 3 hours) with result notifications
- Full Hebrew + English localization (follows device language automatically)
- API request throttling to avoid rate-limit errors when switching tabs quickly

## API Endpoints Used (Retrofit)
1. `GET /competitions/WC/matches` - All matches
2. `GET /competitions/WC/standings` - Group standings
3. `GET /competitions/WC/scorers` - Top scorers
4. `GET /competitions/WC/teams` - All teams

## Cloud Database (Firebase)
User-submitted match predictions are stored in **Firestore**, separate from the Room database used for API data. This keeps a single source of truth per data type: Retrofit + Room for official tournament data, Firestore for user-generated predictions (which Firestore already caches offline on its own).

## Architecture
MVVM with a single shared `WorldCupViewModel` across all fragments, backed by two repositories:
- `WorldCupRepository` — Retrofit + Room (tournament data)
- `PredictionRepository` — Firebase Firestore (user predictions)

Dependency injection via Hilt throughout.
