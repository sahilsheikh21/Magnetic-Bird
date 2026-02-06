# Magnetic Flappy Bird 🐦🧲

An Android Flappy Bird game controlled by your phone's magnetic sensor! Bring a magnet near your phone to make the bird jump.

## Features

- **Magnetic Sensor Control**: Use a magnet (like a fridge magnet or phone case magnet) to control the bird
- **Classic Flappy Bird Gameplay**: Navigate through pipes and score points
- **Smooth Physics**: Realistic gravity and jump mechanics
- **Simple Graphics**: Clean, colorful design

## How to Play

1. Launch the app on your Android device
2. Bring a magnet near your phone to make the bird jump
3. Navigate through the pipes without hitting them
4. Try to get the highest score!
5. Tap the screen to restart after game over

## Requirements

- Android device with API level 21 (Android 5.0 Lollipop) or higher
- Magnetic sensor (magnetometer) - most modern Android phones have this
- A magnet to play with (fridge magnet, phone case magnet, etc.)

## Building the App

### Prerequisites

- Android Studio Arctic Fox or newer
- JDK 8 or higher
- Android SDK with API level 34

### Build Instructions

1. Open the project in Android Studio
2. Let Gradle sync the project
3. Connect your Android device or start an emulator
4. Click "Run" or use the command:
   ```bash
   ./gradlew installDebug
   ```

### Build APK

To build a debug APK:
```bash
./gradlew assembleDebug
```

The APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`

## Technical Details

- **Language**: Kotlin
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)
- **Sensor Used**: TYPE_MAGNETIC_FIELD
- **Magnetic Threshold**: 60 microTesla (adjustable in GameView.kt)

## Adjusting Sensitivity

If the game is too sensitive or not sensitive enough to your magnet, you can adjust the threshold in `GameView.kt`:

```kotlin
val threshold = 60f // microTesla - increase for less sensitivity, decrease for more
```

## Project Structure

```
app/src/main/
├── java/com/magneticflappy/
│   ├── MainActivity.kt      # Sensor management and activity lifecycle
│   ├── GameView.kt          # Main game loop and rendering
│   ├── Bird.kt              # Bird entity with physics
│   └── Pipe.kt              # Obstacle generation and scrolling
├── res/
│   ├── layout/
│   │   └── activity_main.xml
│   └── values/
│       ├── colors.xml
│       └── strings.xml
└── AndroidManifest.xml
```

## License

This is a demo project created for educational purposes.

## Credits

Inspired by the classic Flappy Bird game by Dong Nguyen.
