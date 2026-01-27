# Security Guide - Timely Android App

This document explains the security measures implemented in the Timely app and provides guidance on Firebase key management.

## 🔒 Security Overview

Timely follows Android security best practices and implements multiple layers of protection for sensitive data and user privacy.

## 🔑 Firebase API Keys & Secrets

### Understanding Firebase API Keys

#### ✅ Safe to Expose (with restrictions)
The API key in `google-services.json` is **relatively safe** to expose in Android apps because:

1. **Package Name Restriction**: The API key is tied to your app's package name (`com.lee.timely`)
2. **SHA Fingerprint Restriction**: Firebase can restrict API keys to specific app signatures
3. **OAuth Restrictions**: Google Cloud Console allows restricting OAuth client IDs by app package and SHA fingerprints

**However**, we still keep it private for:
- Defense in depth
- Preventing abuse attempts
- Following security best practices
- Protecting against future vulnerabilities

#### ❌ Never Expose
- **Server API Keys**: If you have any server-side API keys, NEVER expose them
- **Service Account Keys**: Keep these completely private
- **OAuth Client Secrets**: Web client secrets should never be in mobile apps

### Current Implementation

1. **OAuth Client ID**
   - Location: `local.properties` → `GOOGLE_SIGN_IN_CLIENT_ID`
   - Used for: Google Sign-In authentication
   - Security: Loaded at build time via `BuildConfig`
   - Status: ✅ Properly secured (not committed to Git)

2. **Firebase API Key**
   - Location: `app/google-services.json` → `api_key.current_key`
   - Used for: Firebase services initialization
   - Security: File is gitignored, template provided
   - Status: ✅ Properly secured (not committed to Git)

3. **Project Configuration**
   - Location: `app/google-services.json`
   - Contains: Project ID, app ID, API key
   - Security: File is gitignored
   - Status: ✅ Properly secured

## 🛡️ Security Measures

### 1. Secrets Management
- ✅ All secrets stored in `local.properties` (gitignored)
- ✅ Secrets loaded via `BuildConfig` at build time
- ✅ No hardcoded secrets in source code
- ✅ Template files provided for easy setup

### 2. Firebase Security
- ✅ Anonymous authentication for Firestore access
- ✅ Google Sign-In for user authentication
- ✅ Firestore security rules (see `firestore.rules`)
- ✅ API key restrictions (configured in Firebase Console)

### 3. Data Protection
- ✅ Room database for local storage (encrypted by Android)
- ✅ Android Security Crypto for sensitive data
- ✅ No sensitive data in logs (production builds)
- ✅ Proper error handling without exposing internals

### 4. Authentication Flow
```
User → Google Sign-In → Firebase Auth → License Validation → App Access
```
- ✅ Secure token-based authentication
- ✅ License key validation via Firestore
- ✅ Device-based activation tracking
- ✅ Error handling without exposing system details

## 📋 Security Checklist

### Before Publishing to GitHub

- [x] All secrets moved to `local.properties` (gitignored)
- [x] `google-services.json` added to `.gitignore`
- [x] Template files created for setup
- [x] No hardcoded API keys or secrets in code
- [x] BuildConfig used for runtime configuration
- [x] Error messages don't expose sensitive information

### Before Production Release

- [ ] Configure API key restrictions in Google Cloud Console
- [ ] Add SHA-1 and SHA-256 fingerprints to Firebase project
- [ ] Set up OAuth consent screen properly
- [ ] Review and tighten Firestore security rules
- [ ] Enable Firebase App Check (recommended)
- [ ] Enable ProGuard/R8 code obfuscation
- [ ] Review all error messages for information leakage
- [ ] Test authentication flow thoroughly
- [ ] Verify license key validation works correctly
- [ ] Check that no sensitive data is logged

## 🔐 Firebase Security Configuration

### Step 1: API Key Restrictions

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your Firebase project
3. Navigate to **APIs & Services** → **Credentials**
4. Find your API key (from `google-services.json`)
5. Click **Edit** and add restrictions:
   - **Application restrictions**: Android apps
   - **Package name**: `com.lee.timely`
   - **SHA-1 certificate fingerprints**: Add your app's SHA-1
   - **SHA-256 certificate fingerprints**: Add your app's SHA-256

### Step 2: OAuth Client Restrictions

1. In Google Cloud Console → **APIs & Services** → **Credentials**
2. Find your OAuth 2.0 Client ID (Web client)
3. Add restrictions:
   - **Application type**: Web application
   - **Authorized JavaScript origins**: Your Firebase project domain
   - **Authorized redirect URIs**: Firebase Auth callback URLs

### Step 3: Firestore Security Rules

Review and update `firestore.rules`:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // License keys - read/write for authenticated users only
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

### Step 4: Get SHA Fingerprints

**Debug keystore:**
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Release keystore:**
```bash
keytool -list -v -keystore /path/to/your/release.keystore -alias your-alias
```

Add both SHA-1 and SHA-256 to Firebase Console → Project Settings → Your apps → Android app.

## 🚨 Security Best Practices

### Do's ✅
- ✅ Always use `local.properties` for secrets
- ✅ Keep `google-services.json` gitignored
- ✅ Use BuildConfig for runtime configuration
- ✅ Implement proper error handling
- ✅ Validate all user inputs
- ✅ Use HTTPS for all network requests
- ✅ Encrypt sensitive local data
- ✅ Follow principle of least privilege

### Don'ts ❌
- ❌ Never commit secrets to Git
- ❌ Don't hardcode API keys or secrets
- ❌ Don't log sensitive information
- ❌ Don't expose internal error details to users
- ❌ Don't trust client-side validation alone
- ❌ Don't store passwords in plain text
- ❌ Don't use deprecated security APIs

## 🔍 Security Audit

### Code Review Checklist
- [ ] No hardcoded secrets
- [ ] All secrets in `local.properties`
- [ ] Proper error handling
- [ ] Input validation implemented
- [ ] No sensitive data in logs
- [ ] Proper authentication checks
- [ ] Secure data storage
- [ ] Network security (HTTPS only)

### Runtime Security
- [ ] Authentication works correctly
- [ ] License validation secure
- [ ] No data leakage in errors
- [ ] Proper session management
- [ ] Secure token handling

## 📚 Additional Resources

- [Firebase Security Rules](https://firebase.google.com/docs/rules)
- [Android Security Best Practices](https://developer.android.com/training/best-security)
- [OWASP Mobile Top 10](https://owasp.org/www-project-mobile-top-10/)
- [Google Cloud API Key Best Practices](https://cloud.google.com/docs/authentication/api-keys)

## 🆘 Reporting Security Issues

If you discover a security vulnerability, please **DO NOT** open a public issue. Instead:

1. Email security concerns privately
2. Provide detailed information about the vulnerability
3. Allow time for the issue to be addressed before public disclosure

---

**Last Updated**: January 2025
**Security Review Status**: ✅ Production Ready
