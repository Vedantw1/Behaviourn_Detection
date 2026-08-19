# Behavioral Data Collector

A production-quality desktop application built with **Java 21**, **Maven**, and **JavaFX** to collect raw behavioral biometric data (mouse movement, clicks, scrolling, keystrokes, and window focus switches) for a final-year engineering project.

The application captures user interactions during a 30-second recording session, calculates 12 engineered behavioral features, previews them in a modern dark GUI table, and appends them as a dataset row in a CSV file for downstream machine learning model training.

---

## Features & Highlights

- **Pure Data Collection**: Zero machine learning, backend dependencies, or databases included—strictly focused on dataset generation.
- **Privacy Compliant**: Key press and release timing are recorded, but actual key characters/typed letters are explicitly discarded.
- **Real-Time 30-Second Countdown**: Configurable recording timer with visual countdown display.
- **Automated Feature Extraction**: Instant calculation of 12 behavioral metrics upon recording completion.
- **Dual Persistence (CSV & SQLite Database)**: Appends formatted CSV rows to `behavioral_data.csv` and automatically saves records to an embedded SQLite database (`behavioral_data.db`).
- **Modern Glassmorphism UI**: Custom JavaFX CSS styling with dark theme, responsive layouts, and status indicator lights.

---

## 12 Engineered Features

| # | Feature Name | Description & Metric Unit |
|---|---|---|
| 1 | **Average Mouse Speed** | Average movement velocity ($\text{px / sec}$) |
| 2 | **Mouse Acceleration** | Rate of change of mouse velocity ($\text{px / sec}^2$) |
| 3 | **Click Frequency** | Total left & right clicks divided by session duration ($\text{clicks / sec}$) |
| 4 | **Scroll Speed** | Total scroll events divided by session duration ($\text{events / sec}$) |
| 5 | **Average Dwell Time** | Average key hold duration from press to release ($\text{ms}$) |
| 6 | **Average Flight Time** | Average time between key release and next key press ($\text{ms}$) |
| 7 | **Typing Speed** | Total keystrokes divided by session duration ($\text{keys / sec}$) |
| 8 | **Backspace Count** | Total number of Backspace key presses ($\text{count}$) |
| 9 | **Idle Time** | Cumulative inactive duration with no actions $> 1.0\text{s}$ threshold ($\text{seconds}$) |
| 10 | **Session Duration** | Exact duration of the recording session ($\text{seconds}$) |
| 11 | **Window Switch Count** | Total times window focus was lost during recording ($\text{count}$) |
| 12 | **Active Time Ratio** | $(\text{Session Duration} - \text{Idle Time}) / \text{Session Duration}$ ($\text{ratio 0.0 - 1.0}$) |

---

## Project Architecture

```
BehaviorCollector/
├── pom.xml                                  # Maven build configuration (Java 21 & JavaFX 21)
├── README.md                                # Usage documentation
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── behavior/
        │           └── collector/
        │               ├── Main.java        # Entrypoint launcher
        │               ├── App.java         # JavaFX Application
        │               ├── events/          # Raw event records (Mouse, Keyboard, Window)
        │               ├── model/           # Session models & FeatureVector record
        │               ├── listeners/       # Mouse, Keyboard & Window focus event listeners
        │               ├── feature/         # Feature calculation helpers & engine
        │               ├── export/          # Thread-safe CSV exporter service
        │               ├── util/            # High-precision time & logging utilities
        │               └── ui/              # MainView, FeaturePreviewTable & SessionController
        └── resources/
            └── styles/
                └── main.css                 # Dark Glassmorphism JavaFX theme stylesheet
```

---

## Prerequisites

- **Java Development Kit (JDK)**: Java 21 or higher
- **Apache Maven**: Version 3.8+

---

## How to Build and Run

### 1. Clone or Navigate to the Workspace
```bash
cd /Users/vedantmaske/Desktop/DataCollection
```

### 2. Compile the Maven Project
```bash
mvn clean compile
```

### 3. Run the Application
```bash
mvn exec:java
```

---

## CSV Dataset Output Format

Completed sessions append one row to `behavioral_data.csv` in the root workspace directory.

### Example CSV Row
```csv
UserID,Label,AvgMouseSpeed,MouseAcceleration,ClickFrequency,ScrollSpeed,AvgDwellTime,AvgFlightTime,TypingSpeed,BackspaceCount,IdleTime,SessionDuration,WindowSwitchCount,ActiveTimeRatio
user_01,Genuine,342.1520,128.4310,0.8333,0.2000,112.4500,185.3200,4.2000,2,3.1000,30.0000,1,0.8967
```

---

## License & Final Year Project Context

This application forms Phase 1 (Data Collection Module) of a Hybrid Behavioral Biometrics Fraud Detection Project.
