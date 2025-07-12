# Firebase Setup Guide - No Billing Required

This guide will help you set up Firebase for your Timely app without requiring billing or Cloud Functions.

## ✅ Step 1: Firebase Project Setup (Already Done)

Your Firebase project `timely-ce528` is already created and the `google-services.json` file is configured.

## ✅ Step 2: Enable Required Services

Go to your Firebase Console: https://console.firebase.google.com/project/timely-ce528

### 2.1 Authentication
1. Go to **Authentication** → **Sign-in method**
2. Enable **Anonymous** authentication
3. Click **Save**

### 2.2 Firestore Database
1. Go to **Firestore Database**
2. Click **Create database**
3. Choose **Start in test mode** (for development)
4. Select a location close to your users (e.g., `us-central1`)
5. Click **Done**

## ✅ Step 3: Add Sample License Keys

You need to add some license keys to Firestore for testing. Here's how:

### 3.1 Using Firebase Console
1. Go to **Firestore Database**
2. Click **Start collection**
3. Collection ID: `license_keys`
4. Add a document with ID: `TEST-KEY-123`
5. Add these fields:
   ```
   isActive: true (boolean)
   maxDevices: 1 (number)
   devices: [] (array)
   deviceCount: 0 (number)
   created: [current timestamp]
   ```

### 3.2 Add More Test Keys
Add these additional test keys:
- `DEMO-KEY-456`
- `SAMPLE-KEY-789`
- `VALID-KEY-ABC`

## ✅ Step 4: Test the App

1. Build and run your app
2. The app will show the activation screen
3. Enter one of the test keys (e.g., `TEST-KEY-123`)
4. The key should activate successfully
5. Try using the same key on another device/emulator - it should be rejected

## 🔧 How It Works

### Firestore Collections
- `license_keys`: Stores license key information
  - `isActive`: Whether the key is valid
  - `maxDevices`: Maximum devices allowed (default: 1)
  - `devices`: Array of device IDs using this key
  - `deviceCount`: Number of devices using this key
  - `lastUsed`: Timestamp of last usage

- `device_activations`: Stores device activation records
  - `licenseKey`: The license key used
  - `deviceId`: Unique device identifier
  - `activatedAt`: When the device was activated
  - `deviceInfo`: Device manufacturer, model, etc.

### Security Features
1. **One Key, One Device**: Each key can only be used on one device
2. **Device Fingerprinting**: Uses Android ID + device serial for unique identification
3. **Atomic Transactions**: Firestore transactions prevent race conditions
4. **Offline Support**: Local storage for offline activation checks
5. **Anonymous Auth**: No user accounts required

## 🚀 Production Deployment

### 1. Update Firestore Rules
Replace the test mode rules with secure rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // License keys - read/write for authenticated users
    match /license_keys/{keyId} {
      allow read, write: if request.auth != null;
    }
    
    // Device activations - users can only access their own device
    match /device_activations/{deviceId} {
      allow read, write: if request.auth != null && request.auth.uid == deviceId;
    }
  }
}
```

### 2. Add Real License Keys
Replace test keys with your actual license keys in the `license_keys` collection.

### 3. Monitor Usage
Use Firebase Analytics to monitor key usage and device activations.

## 🔍 Troubleshooting

### Common Issues

1. **"Invalid license key"**
   - Check if the key exists in Firestore
   - Verify the key is exactly as entered (case-sensitive)

2. **"License key has reached maximum device limit"**
   - The key is already used on another device
   - Use a different key or deactivate the existing device

3. **"Network error"**
   - Check internet connection
   - Verify Firebase project configuration
   - Check if Authentication and Firestore are enabled

4. **"Authentication failed"**
   - Anonymous authentication might be disabled
   - Check Firebase Console → Authentication → Sign-in methods

### Debug Information
The app logs detailed information. Check Logcat with tag:
- `FirebaseLicenseService`
- `LicenseManager`

## 📱 Testing

### Test Scenarios
1. **Valid Key Activation**: Should work on first device
2. **Duplicate Key**: Should fail on second device
3. **Invalid Key**: Should show error message
4. **Offline Mode**: Should work with cached activation
5. **Key Deactivation**: Should remove device from key

### Device Testing
- Test on real device vs emulator
- Test on different Android versions
- Test with different device manufacturers

## 🎯 Next Steps

1. **Customize UI**: Update the activation screen design
2. **Add Analytics**: Track activation success/failure rates
3. **Implement Key Management**: Create admin interface for managing keys
4. **Add Expiration**: Implement key expiration dates
5. **Backup Strategy**: Implement key backup and recovery

## 📞 Support

If you encounter issues:
1. Check Firebase Console for errors
2. Review app logs in Logcat
3. Verify all services are enabled
4. Test with different keys and devices

The system is now ready to use without any billing requirements! 