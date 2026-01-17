# DiaLog - Diabetes Monitoring App

A modern Android app for tracking diabetes with multi-profile support, customizable measurement labels, and smart risk indicators.

## Features

- 👥 **Multi-Profile Support** - Track for family members
- 📊 **Smart Risk Indicators** - Color-coded glucose levels (Normal, Borderline, High, Low)
- 🏷️ **Custom Labels** - Pre-loaded labels (Fasting, After Lunch, etc.) + create your own
- ⚙️ **Customizable Targets** - Set personal glucose target ranges
- 📱 **Beautiful UI** - Material 3 design with dark mode support
- 💾 **Offline First** - Works without internet

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Database**: Room (SQLite)
- **Architecture**: MVVM + Repository Pattern
- **Navigation**: Jetpack Navigation Compose

## Building

1. Open the project in Android Studio
2. Sync Gradle files
3. Run on an emulator or device (API 26+)

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

## Project Structure

```
app/src/main/java/com/dialog/app/
├── data/
│   ├── database/    # Room database, DAOs
│   ├── model/       # Entity classes
│   └── repository/  # Data repository
├── ui/
│   ├── components/  # Reusable UI components
│   ├── navigation/  # Navigation graph
│   ├── screens/     # Screen composables
│   └── theme/       # Colors, typography, theme
├── DiaLogApp.kt     # Application class
└── MainActivity.kt  # Main entry point
```

## License

MIT License
