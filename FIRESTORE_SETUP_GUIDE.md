# Firestore License System Setup Guide (No Cloud Functions)

## Overview

This guide will help you set up the Firebase Firestore-based license system for your Timely app without using Cloud Functions. The system provides "one-key, one-device" security using direct Firestore operations.

## ⚠️ Security Note

**This approach is less secure than using Cloud Functions** because:
- License validation logic runs on the client side
- Users could potentially bypass license checks
- No server-side validation of license logic

**For production use, consider using Cloud Functions for better security.**

---

## 🏗️ Firestore Database Structure

### 1. Collection: `activationKeys`

**Document ID:** The license key (e.g., `TEST-KEY-123`)

**Document Fields:**
```json
{
  "used": false,
  "device": null,
  "isActive": true,
  "activatedAt": null,
  "appVersion": null,
  "deviceModel": null,
  "lastUpdated": null
}
```

### 2. Collection: `device_activations`

**Document ID:** Device ID (auto-generated hash)

**Document Fields:**
```json
{
  "licenseKey": "TEST-KEY-123",
  "activatedAt": "2024-01-01T00:00:00Z",
  "appVersion": "1.0.0",
  "deviceModel": "Samsung Galaxy S21",
  "lastUpdated": "2024-01-01T00:00:00Z"
}
```

### 3. Collection: `activation_audit` (Optional)

**Auto-generated document ID**

**Document Fields:**
```json
{
  "event": "key_activated",
  "licenseKey": "TEST-KEY...",
  "deviceId": "abc123...",
  "result": "success",
  "timestamp": "2024-01-01T00:00:00Z",
  "appVersion": "1.0.0",
  "deviceModel": "Samsung Galaxy S21"
}
```

---

## 🔧 Setup Steps

### Step 1: Create Firestore Database

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Go to **Firestore Database**
4. Click **Create Database**
5. Choose **Start in test mode** (for development)
6. Select a location close to your users

### Step 2: Add Test License Keys

1. In Firestore, create a collection called `activationKeys`
2. Add a document with ID `TEST-KEY-123`
3. Add the following fields:

```json
{
  "used": false,
  "device": null,
  "isActive": true
}
```

### Step 3: Update Security Rules

Replace the default rules with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow read/write access to all users under any document
    // WARNING: This is for development only!
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

**⚠️ Important:** These rules allow anyone to read/write. For production, use more restrictive rules.

### Step 4: Enable Authentication

1. Go to **Authentication** in Firebase Console
2. Click **Get Started**
3. Go to **Sign-in method**
4. Enable **Anonymous** authentication
5. Click **Save**

---

## 🧪 Testing

### Test License Activation

1. Build and run your app
2. Go to the activation screen
3. Enter `TEST-KEY-123`
4. Click "Activate License"
5. Should show success message

### Test Duplicate Prevention

1. Activate `TEST-KEY-123` on Device A
2. Try to activate same key on Device B
3. Should show "Key already used on another device"

### Test Deactivation

1. Activate a key
2. Go to Settings → Deactivate License
3. Key should be freed for use on another device

---

## 📱 App Integration

The app now uses direct Firestore operations:

- **Activation:** Uses Firestore transactions to atomically claim keys
- **Validation:** Checks local storage first, then Firestore
- **Deactivation:** Removes device records and frees keys

### Key Features

- ✅ **One-key, one-device** enforcement
- ✅ **Offline support** (uses local storage)
- ✅ **Device fingerprinting**
- ✅ **Audit logging**
- ✅ **No Cloud Functions required**

---

## 🚨 Production Considerations

### Security Rules (Production)

For production, use more restrictive rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Only authenticated users can access license data
    match /activationKeys/{keyId} {
      allow read, write: if request.auth != null;
    }
    
    match /device_activations/{deviceId} {
      allow read, write: if request.auth != null;
    }
    
    match /activation_audit/{auditId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### Additional Security Measures

1. **Key Encryption:** Store keys in encrypted format
2. **Rate Limiting:** Implement client-side rate limiting
3. **Device Validation:** Add additional device integrity checks
4. **Key Expiration:** Implement key expiration dates
5. **Admin Panel:** Create web interface for key management

---

## 🔍 Troubleshooting

### Common Issues

1. **"Invalid license key"**
   - Check if the key document exists in Firestore
   - Ensure document ID matches the key exactly (uppercase)

2. **"Key already used on another device"**
   - Key is already activated on a different device
   - Use a different key or deactivate the existing device

3. **"Authentication failed"**
   - Check if Anonymous authentication is enabled
   - Verify Firebase configuration

4. **"Network error"**
   - Check internet connection
   - Verify Firestore rules allow read/write
   - Check Firebase project configuration

### Debug Information

The app provides detailed debug information:

```kotlin
// Get device info
val deviceInfo = licenseManager.getDeviceInfo()

// Get activation info
val activationInfo = licenseManager.getActivationInfo()
```

### Log Tags

Monitor these log tags in Logcat:
- `EnhancedFirebaseLicense`
- `EnhancedLicenseManager`

---

## 📋 Checklist

- [ ] Firebase project created
- [ ] Firestore database created
- [ ] Authentication enabled (Anonymous)
- [ ] Security rules updated
- [ ] Test keys added to Firestore
- [ ] App builds successfully
- [ ] License activation works
- [ ] Duplicate key prevention works
- [ ] Deactivation works
- [ ] Offline operation works

---

## 🎯 Next Steps

1. **Add Real License Keys:** Replace test keys with actual keys
2. **Implement Key Management:** Create admin interface
3. **Add Analytics:** Track activation success/failure rates
4. **Implement Expiration:** Add key expiration dates
5. **Add Backup:** Implement key backup and recovery
6. **Multi-language:** Add localization support

The Firestore-based license system is now ready to use without Cloud Functions! 