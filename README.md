# World Cup 2026 App

## Setup Instructions

### 1. Get your API Key
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

### 3. Add App Icon
- Right-click on `res` folder → New → Image Asset
- Create a launcher icon (football/World Cup themed)
- This will replace the default icon

### 4. Build & Run
- Open in Android Studio
- Sync Gradle
- Run on device or emulator (API 26+)

## Project Structure
```
app/
├── data/
│   ├── local/          ← Room database, DAOs, Entities
│   └── remote/         ← Retrofit API, Response models
├── di/                 ← Hilt dependency injection
├── repository/         ← Single data access point
├── ui/
│   ├── adapters/       ← RecyclerView adapters
│   ├── fragments/      ← 6 fragments (Home, Matches, Standings, Scorers, Favorites, Teams + Settings)
│   └── viewmodels/     ← Shared ViewModel
├── utils/              ← Constants, Resource, Extensions
└── workers/            ← WorkManager background sync
```

## Features
- Live match scores & results
- Group stage standings with ★ favorites
- Top scorers leaderboard
- All 32 teams with details & search
- Favorites list (Room) with swipe-to-delete
- Dark mode
- Background auto-sync (WorkManager)
- Full Hebrew + English localization

## API Endpoints Used
1. `GET /competitions/WC/matches` - All matches
2. `GET /competitions/WC/standings` - Group standings
3. `GET /competitions/WC/scorers` - Top scorers
4. `GET /competitions/WC/teams` - All teams
