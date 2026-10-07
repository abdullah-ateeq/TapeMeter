# TapeMeter 📏

**TapeMeter** is an intuitive Android utility application designed to estimate packing tape requirements for batches of rectangular cartons. Built entirely with **Kotlin** and **Jetpack Compose**, TapeMeter simplifies logistics, warehousing, and shipping workflows by providing accurate tape length estimates, roll requirements, and visual diagrams.

---

## 🌟 Key Features

- **Carton & Batch Calculations**: Enter carton width, height, and total box count to instantly compute required tape lengths.
- **Sealing Patterns**:
  - **Top Only**: Single strip across the top seam.
  - **Top & Bottom**: Standard H-tape or top/bottom centerline sealing.
  - **Top, Bottom & Side**: Full edge sealing including side wraps.
- **Custom Overlap & Waste Percentage**: Add customizable seam overlap (e.g., 50mm) and waste margins (e.g., 5–10%) to account for practical packaging variations.
- **Roll Calculator**: Input tape roll lengths to determine the exact number of rolls needed and estimate leftover tape.
- **Interactive Visualizations**:
  - **Carton Diagram**: Visual 2D diagram displaying carton dimensions and tape placement paths.
  - **Tape Breakdown & Formulas**: Detailed breakdown of top, bottom, and side tape segments alongside transparent mathematical formulas.
- **Multi-Unit Support**: Seamlessly switch between Millimeters (`mm`), Centimeters (`cm`), and Inches (`in`).
- **Calculation History & Presets**:
  - Save frequently used box sizes as presets.
  - Store and view calculation history offline with options to favorite, search, or export/copy results.
- **100% Offline & Private**: All data and preferences are stored locally on device using AndroidX DataStore. No internet connection or tracking required.

---

## 🛠 Tech Stack & Architecture

- **Language**: Kotlin 2.x
- **UI Framework**: Jetpack Compose with Material 3 Design
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Domain Architecture
- **State Management**: Kotlin `StateFlow` & `LiveData`
- **Navigation**: Jetpack Navigation Compose
- **Local Persistence**: AndroidX DataStore Preferences
- **Build System**: Gradle with Kotlin DSL & Version Catalogs (`libs.versions.toml`)

---

## 📁 Project Structure

```text
com.example.tapemeter
├── data/
│   ├── datastore/      # PreferencesManager for DataStore persistence
│   ├── model/          # Data classes (TapeMeterInput, TapeMeterResult, SavedCalculation, etc.)
│   └── repository/     # Data repositories
├── domain/
│   ├── calculator/     # Core calculation logic (TapeMeterCalculator)
│   └── util/           # Unit conversion and formatting utilities
├── ui/
│   ├── components/     # Reusable Compose components (CartonDiagram, ResultCard, FormulaCard, etc.)
│   ├── navigation/     # Jetpack Navigation graphs and screen definitions
│   ├── screens/        # Calculator, History, Settings, About, and Splash screens
│   ├── theme/          # Material 3 Color palettes, Typography, and Theme setup
│   └── viewmodel/      # TapeMeterViewModel managing UI state
└── util/               # Haptic feedback helpers
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug / Meerkat or newer recommended
- **JDK**: Version 17 or higher
- **Android SDK**: Minimum SDK 24 (Android 7.0), Target SDK 35 (Android 15)

### Building & Running

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/TapeMeter.git
   cd TapeMeter
   ```

2. **Open in Android Studio**:
   - Open Android Studio and select **Open**.
   - Navigate to the cloned `TapeMeter` directory.

3. **Build and Run**:
   - Allow Gradle to sync dependencies.
   - Select an emulator or connected physical Android device.
   - Click **Run** (`Shift + F10` / `Cmd + R`).

4. **Running Unit Tests**:
   ```bash
   ./gradlew test
   ```

---

## 📋 Planning Notice & Disclaimer

> **Note**: TapeMeter provides estimates for planning purposes. Actual tape consumption may vary depending on packaging method, tape width, application technique, carton condition, and environmental factors.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
