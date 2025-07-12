# Timely App License Security System

## Overview

The Timely app now includes a comprehensive license security system that prevents key sharing across multiple devices. This system ensures that each license key can only be used on a single device, blocking attempts to use the same key on emulators, multiple real devices, or shared between users.

## Security Features

### ✅ Device Fingerprinting
- **Multi-factor device identification** using hardware characteristics, build information, and system properties
- **Comprehensive device signature** that's extremely difficult to spoof
- **Hardware-based validation** including CPU architecture, screen characteristics, and build fingerprint

### ✅ Anti-Tampering Protection
- **Emulator detection** - Blocks activation on Android emulators and virtual machines
- **Root detection** - Prevents activation on rooted devices
- **App integrity checks** - Validates app signature and installation source
- **Package integrity verification** - Ensures app hasn't been modified

### ✅ Key Usage Tracking
- **One key per device** - Each license key can only be activated on one device
- **Persistent tracking** - Key usage is stored locally and persists across app reinstalls
- **Device binding** - Keys are cryptographically bound to specific device signatures
- **Usage monitoring** - Tracks when and where keys are used

### ✅ Secure Storage
- **Encrypted SharedPreferences** - All license data is encrypted using AES-256
- **Secure key storage** - License keys are never stored in plain text
- **Tamper-resistant storage** - Uses Android's security crypto library

## How It Works

### 1. Device Fingerprinting
```kotlin
// Creates a unique device signature using multiple factors
val deviceSignature = DeviceFingerprint.generateComprehensiveSignature(context)
```

The device fingerprint includes:
- Android ID
- Manufacturer and model information
- Build fingerprint
- Hardware characteristics
- Screen properties
- CPU architecture
- Timestamp-based salt

### 2. Key Validation Process
```kotlin
val validationResult = KeyValidationService.validateKey(context, key)
```

The validation process:
1. **Security Check** - Verifies device is not emulator/rooted
2. **Key Validation** - Checks if key exists in valid keys list
3. **Device Check** - Verifies if key is already used on this device
4. **Usage Check** - Ensures key is not used on another device

### 3. Key Activation
```kotlin
if (KeyValidationService.activateKey(context, key)) {
    // Key successfully activated
}
```

During activation:
- Device signature is generated
- Key usage is recorded
- Secure storage is updated
- Device binding is established

## Security Measures

### Emulator Detection
The system detects emulators by checking:
- Build fingerprint patterns
- Model names containing "google_sdk", "Emulator", etc.
- Manufacturer names like "Genymotion"
- Product names indicating virtual devices

### Root Detection
Root detection includes:
- Build tags containing "test-keys"
- Common root binary paths (/system/bin/su, etc.)
- SU command availability check

### Device Signature Validation
Device signatures are validated using:
- Hardware characteristics
- Build information
- Screen properties
- Time-based salting
- SHA-256 hashing

## Usage Examples

### Activating a License
```kotlin
// User enters license key
val key = "P8Z5-1K7J-3S9D"

// Validate and activate
val validation = KeyValidationService.validateKey(context, key)
when (validation.errorCode) {
    KeyValidationError.INVALID_KEY -> // Show error
    KeyValidationError.KEY_ALREADY_USED -> // Key used on another device
    KeyValidationError.SECURITY_VIOLATION -> // Emulator/root detected
    null -> {
        // Key is valid, activate it
        if (KeyValidationService.activateKey(context, key)) {
            // Success
        }
    }
}
```

### Checking License Status
```kotlin
// Check if current device has valid license
if (KeyValidationService.isKeyValidForCurrentDevice(context)) {
    // App is activated
} else {
    // Show activation screen
}
```

### Deactivating License
```kotlin
// Remove license from current device
if (KeyValidationService.deactivateKey(context)) {
    // License deactivated, key can be used on another device
}
```

## Security Benefits

### ✅ Prevents Key Sharing
- Same key on emulator + real device → **BLOCKED**
- Same key on multiple devices → **BLOCKED**
- Key sharing between users → **BLOCKED**

### ✅ Anti-Tampering
- Emulator usage → **BLOCKED**
- Rooted device usage → **BLOCKED**
- App modification → **BLOCKED**

### ✅ Persistent Protection
- App reinstall → **Still protected**
- Device reset → **Still protected**
- Key extraction attempts → **Blocked**

## Implementation Details

### Files Added/Modified
- `DeviceFingerprint.kt` - Device identification and fingerprinting
- `KeyValidationService.kt` - Key validation and usage tracking
- `AntiTampering.kt` - Security checks and tampering detection
- `ActivationScreen.kt` - Updated with new validation
- `MainActivity.kt` - Updated with new activation logic
- `SettingScreen.kt` - Added license management features

### Data Storage
- **Encrypted SharedPreferences** - Stores license key and device signature
- **Local JSON file** - Tracks key usage across devices
- **Secure key hashing** - Keys are hashed with salt before storage

### Security Considerations
- All sensitive data is encrypted
- Device signatures are cryptographically secure
- Anti-tampering measures are comprehensive
- Key usage tracking is persistent

## Testing the System

### Test Scenarios
1. **Valid activation** - Use valid key on real device
2. **Emulator test** - Try to activate on emulator (should fail)
3. **Multiple devices** - Try same key on different devices (should fail)
4. **Key sharing** - Try to use key from another device (should fail)
5. **Deactivation** - Deactivate key and try on another device (should work)

### Error Messages
- "App cannot be activated on emulator"
- "This key is already in use on another device"
- "App has been modified. Activation blocked."
- "Invalid app signature. Activation blocked."

## Conclusion

This security system provides comprehensive protection against license key sharing while maintaining a good user experience for legitimate users. The multi-layered approach ensures that keys cannot be easily bypassed or shared across devices. 