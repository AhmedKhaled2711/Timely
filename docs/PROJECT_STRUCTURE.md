# 📁 Project Structure Documentation

## **Architecture Overview**
Timely follows **MVVM (Model-View-ViewModel)** architecture with **Clean Architecture** principles.

```
app/
├── src/main/java/com/lee/timely/
│   ├── data/                    # Data Layer
│   │   ├── local/              # Local database (Room)
│   │   │   ├── TimelyDatabase.kt
│   │   │   ├── TimelyDao.kt
│   │   │   ├── TimelyLocalDataSource.kt
│   │   │   └── UserPagingSource.kt
│   │   └── remote/             # Remote data (Firebase)
│   │       └── [Future implementation]
│   ├── domain/                 # Domain Layer (Business Logic)
│   │   ├── Repository.kt
│   │   ├── RepositoryImpl.kt
│   │   └── model.kt
│   ├── features/               # Feature-based organization
│   │   ├── group/             # Group management
│   │   │   ├── ui/
│   │   │   │   ├── view/
│   │   │   │   │   ├── GroupDetailsScreen.kt
│   │   │   │   │   ├── StudentProfileScreen.kt
│   │   │   │   │   └── GroupSelectionBottomSheet.kt
│   │   │   │   └── viewmodel/
│   │   │   │       └── GroupDetailsViewModel.kt
│   │   │   └── [domain logic]
│   │   ├── groups/            # Groups list
│   │   │   └── GroupsScreen.kt
│   │   ├── home/              # Home & user management
│   │   │   ├── ui/
│   │   │   │   ├── view/
│   │   │   │   │   ├── AddUserScreen.kt
│   │   │   │   │   └── GradeScreen.kt
│   │   │   │   ├── state/
│   │   │   │   │   ├── AddUserUiState.kt
│   │   │   │   │   └── TransferUserUiState.kt
│   │   │   │   └── viewmodel/
│   │   │   │       └── MainViewModel.kt
│   │   │   └── [domain logic]
│   │   └── settings/          # App settings & auth
│   │       ├── ActivationScreen.kt
│   │       ├── ActivationViewModel.kt
│   │       └── SettingScreen.kt
│   ├── navigation/            # Navigation logic
│   │   └── NavGraph.kt
│   ├── ui/theme/             # UI theming
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   ├── Type.kt
│   │   └── Typography.kt
│   ├── util/                 # Utilities
│   │   ├── AcademicYearUtils.kt
│   │   ├── ActivationResult.kt
│   │   ├── ActivationStatus.kt
│   │   ├── EnhancedLicenseManager.kt
│   │   ├── LocaleHelper.kt
│   │   ├── PaymentUpdateState.kt
│   │   ├── Sealed.kt
│   │   └── TestDataGenerator.kt
│   ├── animation/            # Animations
│   │   └── NoGroupsAnimation.kt
│   ├── App.kt                # Application class
│   └── MainActivity.kt       # Main activity
├── src/main/res/            # Android resources
│   ├── values/              # Strings, colors, themes
│   ├── values-ar/           # Arabic localization
│   └── [other resources]
└── build.gradle.kts         # App-level build configuration
```

## **Layer Responsibilities**

### **🗄️ Data Layer**
- **LocalDataSource**: Room database operations
- **Database**: SQLite database with Room ORM
- **Models**: Entity classes for database tables

### **🧠 Domain Layer**
- **Repository**: Abstract data operations
- **RepositoryImpl**: Concrete implementation
- **Models**: Business logic entities

### **🎨 UI Layer**
- **Screens**: Compose UI screens
- **ViewModels**: State management and business logic
- **States**: UI state management classes

## **Key Technologies**

### **Database**
- **Room**: Local SQLite database
- **Paging 3**: Efficient large list handling
- **Coroutines**: Asynchronous operations

### **UI Framework**
- **Jetpack Compose**: Modern UI toolkit
- **Material 3**: Design system
- **Navigation Compose**: Screen navigation

### **Authentication**
- **Firebase Auth**: Google Sign-In
- **BuildConfig**: Secure configuration

### **Architecture Patterns**
- **MVVM**: Separation of concerns
- **Repository Pattern**: Data abstraction
- **StateFlow**: Reactive state management

## **Data Flow**

```
UI (Screen) ↔ ViewModel ↔ Repository ↔ DataSource (Room/Firebase)
```

1. **User Interaction** → UI Screen
2. **Event Handling** → ViewModel
3. **Business Logic** → Repository
4. **Data Operations** → DataSource
5. **State Updates** → ViewModel → UI

## **State Management**

### **UI States**
- **AddUserUiState**: User creation/update flow
- **TransferUserUiState**: User transfer between groups
- **GoogleSignInState**: Authentication state

### **StateFlow Usage**
- Reactive UI updates
- Configuration change survival
- Lifecycle-aware observation

## **Feature Modules**

### **Group Management**
- Create, view, edit groups
- Student profiles and details
- Payment tracking per month

### **User Management**
- Add, edit, delete students
- Transfer between groups
- Grade management

### **Settings & Auth**
- Google Sign-In integration
- App activation/licensing
- Language preferences

## **Best Practices Applied**

- **Single Responsibility**: Each class has one purpose
- **Dependency Injection**: Manual DI implementation
- **Error Handling**: Comprehensive exception management
- **Localization**: Multi-language support (EN/AR)
- **Security**: Secrets management via BuildConfig
- **Testing**: Unit and integration test structure

---

**📝 Note**: This structure supports scalability and maintainability while following Android development best practices.
