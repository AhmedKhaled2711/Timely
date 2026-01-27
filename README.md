# Timely - Student Management System

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-039BE5?style=for-the-badge&logo=Firebase&logoColor=white)
![Room](https://img.shields.io/badge/Room-3DDC84?style=for-the-badge&logo=android&logoColor=white)

A modern, production-ready Android application built with Jetpack Compose that helps educational institutions efficiently manage student information, group organization, and payment tracking.

[🎯 Overview](#-overview) • [✨ Features](#-key-features) • [🏗️ Architecture](#️-architecture) • [🚀 Setup](#-getting-started) • [🔐 Security](#-security--secrets-management) • [🎥 Demo Video](#-demo-video) • [🔧 Tech Stack](#-tech-stack) • [🤝 Contributing](#-contributing)

</div>

---

## 🎯 Overview

Timely is a comprehensive student management solution designed for teachers and administrators. The application provides an intuitive interface for managing student records, organizing them into groups, and tracking monthly payment statuses across academic years.

**🌟 Production Highlights:**
- ✅ **Portfolio-Ready**: Clean, secure, and professionally structured
- ✅ **Modern Architecture**: MVVM with Clean Architecture principles
- ✅ **Offline-First**: Local Room database with Firebase sync capabilities
- ✅ **Secure Authentication**: Google Sign-In with proper secrets management
- ✅ **Privacy-Focused**: Zero hardcoded secrets, production-grade security

## ✨ Key Features

### 📚 Student Management
- **Complete CRUD Operations**: Add, edit, delete, and view student profiles
- **Rich Student Profiles**: Store comprehensive information including name, student number, guardian contacts, and enrollment dates
- **Smart Search**: Quick search by student name, UID, or student number
- **Duplicate Prevention**: Built-in validation to prevent duplicate student names within groups

### 🏫 Group Organization
- **Hierarchical Structure**: Organize students by academic years and groups
- **Flexible Grouping**: Create and manage multiple groups within each school year
- **Group Management**: Full CRUD operations for groups and academic years

### 💳 Payment Tracking
- **Monthly Payment System**: Track payment status for each month across academic years
- **Academic Year Support**: Handle multiple academic years with proper month/year calculations
- **Visual Indicators**: Clear payment status indicators with color coding
- **Bulk Operations**: Efficient payment status updates with optimistic UI updates

### 🔐 Authentication & Security
- **Google Sign-In**: Secure authentication using Firebase Authentication
- **License Key System**: Device-based activation system for access control
- **Secure Storage**: Sensitive data encrypted using Android Security Crypto
- **Privacy Protection**: No sensitive data exposed in codebase

### 🎨 Modern UI/UX
- **Material 3 Design**: Latest Material Design principles with beautiful theming
- **Jetpack Compose**: Modern declarative UI framework for smooth, responsive interfaces
- **Dark/Light Themes**: Automatic theme switching based on system preferences
- **RTL Support**: Full right-to-left language support for Arabic and other RTL languages
- **Responsive Design**: Optimized for various screen sizes and orientations

### 🔧 Technical Excellence
- **MVVM Architecture**: Clean separation of concerns with Model-View-ViewModel pattern
- **Local-First Approach**: All data stored locally using Room database for privacy and offline access
- **Modern Android**: Built with latest Android development practices and Kotlin
- **Performance Optimized**: Efficient data loading with Paging 3 for large datasets
- **Type Safety**: Full Kotlin type safety with null-safety throughout
- **Error Handling**: Comprehensive error handling and user feedback

## 🛠️ Tech Stack

### Core Technologies
- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Database**: Room with SQLite
- **Asynchronous**: Kotlin Coroutines & Flow
- **Navigation**: Compose Navigation
- **Dependency Injection**: Manual DI (clean, no heavy frameworks)

### Key Libraries
- **Room Database**: Local data persistence (v2.5.2)
- **Paging 3**: Efficient data loading for large lists (v3.1.1)
- **Coroutines**: Asynchronous programming (v1.7.3)
- **Material 3**: Modern UI components
- **Compose Navigation**: Screen navigation (v2.7.5)
- **Lottie**: Beautiful animations (v6.0.0)
- **Firebase**: Authentication, Firestore, Functions (BOM v32.1.0)
- **Google Sign-In**: OAuth authentication (v20.7.0)
- **Security Crypto**: Encrypted storage (v1.1.0-alpha06)

## 🏗️ Architecture

### Clean Architecture Implementation
```
┌─────────────────────────────────────────┐
│              UI Layer                   │
│  ┌─────────────┐  ┌─────────────────────┐│
│  │   Screens   │  │    ViewModels       ││
│  │ (Compose)   │  │   (Business Logic)  ││
│  └─────────────┘  └─────────────────────┘│
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│            Domain Layer                  │
│  ┌─────────────┐  ┌─────────────────────┐│
│  │   Models    │  │   Repository       ││
│  │   (Entities)│  │   (Interfaces)     ││
│  └─────────────┘  └─────────────────────┘│
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│            Data Layer                    │
│  ┌─────────────┐  ┌─────────────────────┐│
│  │ Room Database│  │  Data Sources      ││
│  │   (DAOs)    │  │  (Implementation)  ││
│  └─────────────┘  └─────────────────────┘│
└─────────────────────────────────────────┘
```

### Database Design
The application uses a well-structured Room database with the following entities:

- **User**: Student information with personal details
- **GroupName**: Group/class organization
- **GradeYear**: Academic year management
- **AcademicYearPayment**: Monthly payment tracking with proper relationships

### Authentication Flow
```
User → Google Sign-In → Firebase Auth → License Validation → App Access
```

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Hedgehog (2023.1.1) or later
- **Android SDK**: API 34 (Android 14)
- **JDK**: Version 11 or later
- **Kotlin**: 1.8.10 or later
- **Firebase Account**: For authentication and license management

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/timely.git
   cd timely
   ```

2. **Set up local.properties**
   ```bash
   # Copy the template file
   cp local.properties.template local.properties
   
   # Edit local.properties and add:
   # - Your Android SDK path
   # - Your Google Sign-In OAuth Client ID (see Firebase Setup below)
   ```

3. **Set up Firebase**
   
   **Step 1: Create Firebase Project**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project or select existing one
   - Add an Android app with package name: `com.lee.timely`

   **Step 2: Download google-services.json**
   - In Firebase Console → Project Settings → General
   - Download `google-services.json`
   - Place it in: `app/google-services.json`
   - ⚠️ **Important**: This file is gitignored and contains your API keys

   **Step 3: Enable Firebase Services**
   - **Authentication**: Enable "Anonymous" and "Google" sign-in methods
   - **Firestore Database**: Create database in test mode (for development)
   - **Cloud Functions**: Optional, for enhanced license management

   **Step 4: Get OAuth Client ID**
   - In Firebase Console → Authentication → Sign-in method → Google
   - Enable Google sign-in
   - Go to Project Settings → General → Your apps → Android app
   - Find "OAuth 2.0 Client IDs" section
   - Copy the Web client ID (format: `PROJECT_NUMBER-XXXXX.apps.googleusercontent.com`)
   - Add it to `local.properties` as `GOOGLE_SIGN_IN_CLIENT_ID`

   **Step 5: Configure Firestore**
   - Create collection: `license_keys` (or `activationKeys`)
   - Add test documents with fields:
     ```json
     {
       "isActive": true,
       "maxDevices": 1,
       "devices": [],
       "deviceCount": 0,
       "created": [timestamp]
     }
     ```

4. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

5. **Build and Run**
   - Connect an Android device or start an emulator (API 24+)
   - Click "Run" button (or press `Shift+F10`)
   - The app will launch and show the activation screen

### Build Configuration
- **Compile SDK**: 34
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34
- **Java Version**: 11
- **Kotlin**: 1.8.10

## 🔒 Security & Secrets Management

### ✅ Security Best Practices Implemented

1. **Secrets Management**
   - All sensitive data (OAuth Client ID) stored in `local.properties` (gitignored)
   - Secrets exposed via `BuildConfig` (not hardcoded)
   - `google-services.json` is gitignored (contains API keys)

2. **Firebase API Key Safety**
   - ⚠️ **Important**: The API key in `google-services.json` is **SAFE to expose** in Android apps
   - Firebase API keys are restricted by:
     - Package name (`com.lee.timely`)
     - SHA-1/SHA-256 fingerprints (configured in Firebase Console)
     - OAuth restrictions (configured in Google Cloud Console)
   - However, we still keep it private for best practices

3. **OAuth Client ID**
   - Stored in `local.properties` (not committed)
   - Loaded at build time via `BuildConfig`
   - Required for Google Sign-In functionality

4. **What's Safe to Commit**
   - ✅ `google-services.json.template` (template file)
   - ✅ `local.properties.template` (template file)
   - ✅ Source code (no hardcoded secrets)
   - ✅ Build configuration files

5. **What's NOT Committed**
   - ❌ `local.properties` (contains secrets)
   - ❌ `app/google-services.json` (contains API keys)
   - ❌ Build artifacts

### 🔐 Firebase Security Checklist

Before publishing:
- [ ] Configure API key restrictions in Google Cloud Console
- [ ] Add SHA-1/SHA-256 fingerprints to Firebase project
- [ ] Set up OAuth consent screen
- [ ] Configure Firestore security rules (see `firestore.rules`)
- [ ] Enable App Check (recommended for production)
- [ ] Review Firebase security best practices

## 📱 Screens & Features

### 1. Groups Screen
- View all academic years and groups
- Navigate to specific group details
- Add new academic years and groups

### 2. Group Details Screen
- Comprehensive student list with payment status
- Monthly payment tracking for all 12 months
- Search and filter capabilities
- Student profile management

### 3. Student Profile Screen
- Complete student information display
- Monthly payment calendar view
- Edit student details
- Transfer students between groups

### 4. Activation Screen
- Google Sign-In integration
- License key activation
- Device-based activation tracking
- Error handling and user feedback

## 🧪 Testing

### Unit Tests
Unit tests are located in the `app/src/test` directory. To run:
```bash
./gradlew test
```

### Instrumentation Tests
UI and integration tests are in `app/src/androidTest`. To run:
```bash
./gradlew connectedAndroidTest
```

## 📊 Project Structure

```
app/
├── src/main/java/com/lee/timely/
│   ├── data/
│   │   └── local/                 # Room database and DAOs
│   ├── domain/                    # Business logic and models
│   ├── features/                  # Feature-based organization
│   │   ├── group/                 # Group management
│   │   ├── groups/                # Groups listing
│   │   ├── home/                  # Main screens
│   │   └── settings/              # App settings & activation
│   ├── navigation/                # Navigation setup
│   ├── ui/theme/                  # Material 3 theming
│   └── util/                      # Utility classes
├── src/main/res/                  # Android resources
└── src/test/                      # Test files
```

## 🎥 Demo Video

Experience the Timely app in action through our comprehensive demo video:

**[📺 Watch the Full Demo](https://www.youtube.com/watch?v=PXjFRkzjtHU)**

### What the Demo Covers:
- **🔐 Authentication Flow**: Secure Google Sign-In integration with Firebase
- **📱 Core Features**: Student management, group organization, and payment tracking
- **💾 Database Operations**: Room database usage with real-time updates
- **🎨 User Interface**: Material 3 design with smooth animations and transitions
- **🌍 Localization**: RTL support for Arabic language
- **⚡ Performance**: Efficient data handling with Paging 3 and StateFlow

### Technical Highlights Demonstrated:
- MVVM architecture with clean separation of concerns
- Reactive state management using StateFlow and Compose
- Offline-first approach with local Room database
- Modern Android development best practices

> **Perfect for recruiters and technical reviewers** - See how production-ready Android development is implemented with modern technologies and architectural patterns.

## 🔧 Tech Stack

### **Core Technologies**
- **Language**: Kotlin 1.9+
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture
- **Database**: Room with SQLite
- **Async**: Coroutines & Flow

### **Android Libraries**
- **Navigation**: Navigation Compose 2.7.5
- **ViewModel**: Lifecycle ViewModel KTX 2.7.0
- **Paging**: Paging 3 & Paging Compose
- **Security**: AndroidX Security Crypto
- **SplashScreen**: Core SplashScreen API

### **Firebase Services**
- **Authentication**: Firebase Auth with Google Sign-In
- **Firestore**: Cloud database (optional sync)
- **Functions**: Cloud Functions for backend logic

### **UI & Animation**
- **Material 3**: Modern design system
- **Lottie**: Smooth animations
- **Accompanist**: Swipe refresh utilities

### **Testing**
- **Unit Tests**: JUnit 4
- **Instrumentation**: AndroidX Test & Espresso
- **UI Testing**: Compose Testing

## 🏗️ Architecture

### **MVVM Architecture Pattern**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   UI (Compose)  │◄──►│   ViewModel     │◄──►│   Repository    │
│   - Screens     │    │   - State       │    │   - Data Logic  │
│   - Components  │    │   - Events      │    │   - Sources     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │  Data Sources   │
                       │  - Room DB      │
                       │  - Firebase     │
                       └─────────────────┘
```

### **Project Structure**
```
app/
├── src/main/java/com/lee/timely/
│   ├── data/
│   │   └── local/                 # Room database and DAOs
│   ├── domain/                    # Business logic and models
│   ├── features/                  # Feature-based organization
│   │   ├── group/                 # Group management
│   │   ├── groups/                # Groups listing
│   │   ├── home/                  # Main screens
│   │   └── settings/              # App settings & activation
│   ├── navigation/                # Navigation setup
│   ├── ui/theme/                  # Material 3 theming
│   └── util/                      # Utility classes
├── src/main/res/                  # Android resources
└── src/test/                      # Test files
```

### **Key Architecture Decisions**
- **Single Activity Architecture**: One MainActivity with Compose navigation
- **Feature-Based Structure**: Organized by app features, not layers
- **Repository Pattern**: Abstract data sources from business logic
- **StateFlow**: Reactive state management with lifecycle awareness
- **Offline-First**: Local database as primary source, Firebase as optional sync

## 🔮 Future Enhancements

### Planned Features
- [ ] **Data Export/Import**: CSV export for backup and reporting
- [ ] **Payment Reports**: Generate monthly and annual payment reports
- [ ] **Attendance Tracking**: Add attendance management features
- [ ] **Multi-language Support**: Expand language options beyond English/Arabic
- [ ] **Biometric Authentication**: Secure app access with fingerprint/face recognition
- [ ] **Cloud Sync**: Optional cloud synchronization (user-controlled)
- [ ] **Notifications**: Payment due date reminders
- [ ] **Analytics Dashboard**: Visual insights and statistics

### Technical Improvements
- [ ] **Kotlin Multiplatform**: Extend to iOS and desktop
- [ ] **Modularization**: Split into feature modules
- [ ] **CI/CD Pipeline**: Automated testing and deployment
- [ ] **Dependency Injection**: Migrate to Hilt for better DI management
- [ ] **Database Migrations**: Proper incremental database migrations
- [ ] **Comprehensive Testing**: Increase test coverage

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

### Development Guidelines
1. **Code Style**: Follow Kotlin coding conventions
2. **Architecture**: Maintain MVVM pattern and clean architecture
3. **Testing**: Add unit tests for new features
4. **Documentation**: Update README and code comments
5. **Commits**: Use clear, descriptive commit messages
6. **Security**: Never commit secrets or API keys

### Pull Request Process
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Android Team**: For the excellent Jetpack Compose framework
- **Material Design Team**: For the beautiful Material 3 design system
- **Kotlin Team**: For the modern and powerful programming language
- **Firebase Team**: For robust backend services
- **Open Source Community**: For the amazing libraries and tools used

## 📞 Support

If you have any questions, suggestions, or encounter any issues, you can reach me at:

📧 **eng.ahmedkhaled.work@gmail.com**
---

<div align="center">
  <strong>Built with ❤️ using modern Android development practices</strong>
  <br>
  <sub>Production-ready • Secure • Modern</sub>
</div>
