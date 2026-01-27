# 🔐 Firebase Security Setup Guide

## **Overview**
This guide explains how to securely configure Firebase for the Timely Android app while keeping sensitive data out of version control.

## **🔑 Firebase Keys Security**

### **✅ Safe to Expose (Public)**
- **Project ID**: `your-project-id` - Public identifier
- **Storage Bucket**: `your-project-id.firebasestorage.app` - Public endpoint
- **App ID**: Public app identifier
- **Package Name**: `com.lee.timely` - Public app package

### **❌ Must Keep Private (Secret)**
- **API Keys**: `AIzaSy...` - Server authentication keys
- **OAuth Client IDs**: `XXXXX.apps.googleusercontent.com` - Authentication tokens
- **Service Account Keys**: `.json` files - Server credentials
- **Web App Config**: Firebase config objects with API keys

## **🛡️ Security Best Practices**

### **1. OAuth Client ID Protection**
```kotlin
// ✅ GOOD: Loaded from BuildConfig (secure)
val oauthClientId = BuildConfig.GOOGLE_SIGN_IN_CLIENT_ID

// ❌ BAD: Hardcoded in code
val oauthClientId = "1234567890-abc123.apps.googleusercontent.com"
```

### **2. Firebase Console Security**
1. **Enable App Check**: Protect backend resources
2. **Restrict API Keys**: Add package name and SHA certificate restrictions
3. **Use Security Rules**: Implement Firestore security rules
4. **Monitor Usage**: Set up alerts for unusual activity

### **3. SHA Certificate Restrictions**
Add your app's SHA-1 and SHA-256 certificates to Firebase Console:
```bash
# Debug SHA-1
keytool -list -v -keystore ~/.android/debug.keystore

# Release SHA-1 (from your release keystore)
keytool -list -v -keystore your-release-key.keystore
```

## **🔧 Setup Instructions**

### **Step 1: Firebase Project Setup**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create new project or select existing
3. Add Android app with package name `com.lee.timely`
4. Download `google-services.json`

### **Step 2: Get OAuth Client ID**
1. In Firebase Console → Authentication → Sign-in method
2. Enable Google Sign-In
3. Go to Project Settings → General → Your apps
4. Find OAuth 2.0 Client IDs section
5. Copy the **Web client** ID (not Android client)

### **Step 3: Configure Local Development**
```bash
# Copy template and fill in your values
cp local.properties.template local.properties
cp app/google-services.json.template app/google-services.json
```

Edit `local.properties`:
```properties
GOOGLE_SIGN_IN_CLIENT_ID=your-actual-oauth-client-id.apps.googleusercontent.com
```

### **Step 4: Place Firebase Config**
Place your downloaded `google-services.json` in:
```
app/google-services.json
```

## **🚨 Production Security Checklist**

- [ ] OAuth Client ID stored in `local.properties` only
- [ ] `google-services.json` in `.gitignore`
- [ ] SHA certificates added to Firebase Console
- [ ] API keys restricted to package name and certificates
- [ ] Firestore security rules implemented
- [ ] App Check enabled for production
- [ ] No hardcoded secrets in source code
- [ ] Debug builds separate from release builds

## **🔍 Security Validation**

### **Verify Secrets Are Protected**
```bash
# Check no secrets are committed
git grep "AIzaSy" || echo "✅ No API keys found"
git grep "apps.googleusercontent.com" || echo "✅ No OAuth IDs found"
```

### **Verify Build Configuration**
```bash
# Test build with missing secrets (should show error)
./gradlew assembleDebug
```

## **📱 Firebase Security Rules Example**

### **Firestore Rules**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can only access their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Groups data access based on user ownership
    match /groups/{groupId} {
      allow read, write: if request.auth != null && 
        resource.data.ownerId == request.auth.uid;
    }
  }
}
```

## **🔄 Regular Security Maintenance**

1. **Monthly**: Review Firebase console for unusual activity
2. **Quarterly**: Rotate API keys if needed
3. **Annually**: Update dependencies for security patches
4. **Before Release**: Verify all security measures are in place

---

**⚠️ Important**: Never commit real API keys, OAuth client IDs, or `google-services.json` to version control. Always use the template files and local configuration for development.
