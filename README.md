# Timely - Student Payment Tracker

Timely is an Android application built with Jetpack Compose that helps educational institutions track student payments. The app allows administrators to manage students, groups, and payment statuses efficiently.

## 📱 Features

- **Student Management**: Add, edit, and remove student records
- **Group Organization**: Organize students into groups and school years
- **Payment Tracking**: Track monthly payment status for each student
- **Search & Filter**: Quickly find students and filter by payment status
- **Dark/Light Theme**: Supports both light and dark themes
- **Responsive UI**: Adapts to different screen sizes and orientations
- **Local Database**: Uses Room for offline data persistence
- **Modern Architecture**: Built with MVVM and Clean Architecture principles

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose, Material 3
- **Architecture**: MVVM, Repository Pattern
- **Local Database**: Room
- **Asynchronous**: Kotlin Coroutines, Flow
- **Dependency Injection**: Hilt
- **Navigation**: Compose Navigation
- **Paging**: For efficient data loading
- **Build System**: Gradle (Kotlin DSL)

## 📦 Dependencies

- AndroidX Core KTX
- Jetpack Compose
- Material 3
- Room Database
- Kotlin Coroutines
- ViewModel & LiveData
- Navigation Component
- Paging 3
- Splash Screen API
- Coil (Image Loading)

## 🚀 Getting Started

### Prerequisites

- Android Studio Flamingo (2022.2.1) or later
- Android SDK 34
- JDK 11

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/AhmedKhaled2711/Timely.git
   ```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Build and run the app on an emulator or physical device

## 🏗️ Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/lee/timely/
│   │   │   ├── data/               # Data layer (repositories, data sources)
│   │   │   │   └── local/          # Room database and DAOs
│   │   │   ├── di/                 # Dependency injection modules
│   │   │   ├── domain/             # Business logic and use cases
│   │   │   ├── features/           # Feature modules
│   │   │   │   ├── group/          # Group management feature
│   │   │   │   ├── groups/         # Groups listing feature
│   │   │   │   ├── home/           # Main screen and navigation
│   │   │   │   └── settings/       # App settings
│   │   │   ├── navigation/         # Navigation components
│   │   │   ├── ui/                 # Common UI components
│   │   │   └── util/               # Utility classes
│   │   └── res/                    # Resources (drawables, strings, etc.)
│   └── test/                       # Unit tests
└── build.gradle.kts                # Project-level build configuration
```

## 🎨 UI Components

- **Composable Functions**: Reusable UI components built with Jetpack Compose
- **Material 3**: Modern Material Design implementation
- **Theming**: Custom theming support
- **Animations**: Smooth transitions and loading states

## 🧪 Testing

The app includes unit tests for critical components. To run the tests:

1. Open the Project view in Android Studio
2. Right-click on the `test` or `androidTest` directory
3. Select "Run 'Tests in 'timely'''"

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📬 Contact

For any questions or feedback, please contact the project maintainer at [eng.ahmedkhaled.work@gmail.com
](eng.ahmedkhaled.work@gmail.com
)

---

<div align="center">
  Made with ❤️ by Ahmed Khaled
</div>
