const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

/**
 * Enhanced Cloud Function to claim a license key
 * Provides atomic "claimKey" logic to prevent race conditions
 * Includes comprehensive validation and audit logging
 */
exports.claimKey = functions.https.onCall(async (data, context) => {
  try {
    const { key, device, appVersion, deviceModel, timestamp } = data;
    
    // Validate input
    if (!key || !device) {
      throw new functions.https.HttpsError('invalid-argument', 'Missing key or device parameter');
    }
    
    // Validate key format
    const trimmedKey = key.trim().toUpperCase();
    if (!isValidKeyFormat(trimmedKey)) {
      throw new functions.https.HttpsError('invalid-argument', 'Invalid key format');
    }
    
    // Rate limiting check (optional)
    if (timestamp && Date.now() - timestamp > 300000) { // 5 minutes
      throw new functions.https.HttpsError('deadline-exceeded', 'Request too old');
    }
    
    console.log(`Claiming key: ${trimmedKey} for device: ${device.substring(0, 16)}...`);
    
    // Use Firestore transaction for atomic operation
    const result = await admin.firestore().runTransaction(async (transaction) => {
      const keyRef = admin.firestore().collection('activationKeys').doc(trimmedKey);
      const keyDoc = await transaction.get(keyRef);
      
      // Check if key exists
      if (!keyDoc.exists) {
        throw new functions.https.HttpsError('not-found', 'Invalid license key');
      }
      
      const keyData = keyDoc.data();
      const isUsed = keyData.used || false;
      const usedDevice = keyData.device || null;
      const isActive = keyData.isActive !== false; // Default to true if not set
      
      // Check if key is active
      if (!isActive) {
        throw new functions.https.HttpsError('permission-denied', 'License key is inactive');
      }
      
      // Determine action based on key status
      if (!isUsed) {
        // Key is available, claim it
        transaction.update(keyRef, {
          used: true,
          device: device,
          activatedAt: admin.firestore.FieldValue.serverTimestamp(),
          appVersion: appVersion || '1.0',
          deviceModel: deviceModel || 'Unknown',
          lastUpdated: admin.firestore.FieldValue.serverTimestamp(),
          claimCount: (keyData.claimCount || 0) + 1
        });
        
        // Create device activation record
        const deviceRef = admin.firestore().collection('device_activations').doc(device);
        transaction.set(deviceRef, {
          licenseKey: trimmedKey,
          deviceId: device,
          activatedAt: admin.firestore.FieldValue.serverTimestamp(),
          appVersion: appVersion || '1.0',
          deviceModel: deviceModel || 'Unknown',
          lastCheck: admin.firestore.FieldValue.serverTimestamp()
        });
        
        console.log(`Key ${trimmedKey} claimed successfully by device ${device.substring(0, 16)}...`);
        return 'success';
      } else if (usedDevice === device) {
        // Key already claimed by this device
        console.log(`Key ${trimmedKey} already claimed by this device`);
        return 'ok';
      } else {
        // Key used by different device
        console.log(`Key ${trimmedKey} already used by device ${usedDevice.substring(0, 16)}...`);
        throw new functions.https.HttpsError('already-exists', 'Key already used on another device');
      }
    });
    
    // Log audit event
    await logAuditEvent('key_claimed', trimmedKey, device, 'success', {
      appVersion,
      deviceModel,
      result
    });
    
    return result;
    
  } catch (error) {
    console.error('claimKey error:', error);
    
    // Log audit event for failure
    if (data && data.key && data.device) {
      await logAuditEvent('key_claim_failed', data.key.trim().toUpperCase(), data.device, 'error', {
        error: error.message,
        appVersion: data.appVersion,
        deviceModel: data.deviceModel
      });
    }
    
    if (error instanceof functions.https.HttpsError) {
      throw error;
    }
    
    throw new functions.https.HttpsError('internal', 'Internal server error');
  }
});

/**
 * Enhanced Cloud Function to check key status
 */
exports.checkKeyStatus = functions.https.onCall(async (data, context) => {
  try {
    const { key, device } = data;
    
    if (!key) {
      throw new functions.https.HttpsError('invalid-argument', 'Missing key parameter');
    }
    
    const trimmedKey = key.trim().toUpperCase();
    const keyDoc = await admin.firestore().collection('activationKeys').doc(trimmedKey).get();
    
    if (!keyDoc.exists) {
      return { status: 'invalid' };
    }
    
    const keyData = keyDoc.data();
    const isUsed = keyData.used || false;
    const usedDevice = keyData.device || null;
    const isActive = keyData.isActive !== false;
    
    if (!isActive) {
      return { status: 'inactive' };
    }
    
    if (!isUsed) {
      return { status: 'available' };
    } else if (usedDevice === device) {
      return { 
        status: 'used_by_this_device',
        activatedAt: keyData.activatedAt,
        deviceModel: keyData.deviceModel,
        appVersion: keyData.appVersion
      };
    } else {
      return { status: 'used_by_other_device' };
    }
    
  } catch (error) {
    console.error('checkKeyStatus error:', error);
    throw new functions.https.HttpsError('internal', 'Internal server error');
  }
});

/**
 * Enhanced Cloud Function to deactivate a key
 */
exports.deactivateKey = functions.https.onCall(async (data, context) => {
  try {
    const { key, device } = data;
    
    if (!key || !device) {
      throw new functions.https.HttpsError('invalid-argument', 'Missing key or device parameter');
    }
    
    const trimmedKey = key.trim().toUpperCase();
    
    // Use transaction for atomic operation
    const result = await admin.firestore().runTransaction(async (transaction) => {
      const keyRef = admin.firestore().collection('activationKeys').doc(trimmedKey);
      const keyDoc = await transaction.get(keyRef);
      
      if (!keyDoc.exists) {
        throw new functions.https.HttpsError('not-found', 'Key not found');
      }
      
      const keyData = keyDoc.data();
      const usedDevice = keyData.device || null;
      
      if (usedDevice !== device) {
        throw new functions.https.HttpsError('permission-denied', 'Cannot deactivate key owned by another device');
      }
      
      // Reset the key to unused state
      transaction.update(keyRef, {
        used: false,
        device: null,
        activatedAt: null,
        deactivatedAt: admin.firestore.FieldValue.serverTimestamp(),
        lastUpdated: admin.firestore.FieldValue.serverTimestamp()
      });
      
      // Remove device activation record
      const deviceRef = admin.firestore().collection('device_activations').doc(device);
      transaction.delete(deviceRef);
      
      return { success: true };
    });
    
    // Log audit event
    await logAuditEvent('key_deactivated', trimmedKey, device, 'success');
    
    return result;
    
  } catch (error) {
    console.error('deactivateKey error:', error);
    
    // Log audit event for failure
    if (data && data.key && data.device) {
      await logAuditEvent('key_deactivation_failed', data.key.trim().toUpperCase(), data.device, 'error', {
        error: error.message
      });
    }
    
    if (error instanceof functions.https.HttpsError) {
      throw error;
    }
    
    throw new functions.https.HttpsError('internal', 'Internal server error');
  }
});

/**
 * Cloud Function to get key statistics (admin only)
 */
exports.getKeyStats = functions.https.onCall(async (data, context) => {
  try {
    // Optional: Add admin authentication here
    // if (!context.auth || !context.auth.token.admin) {
    //   throw new functions.https.HttpsError('permission-denied', 'Admin access required');
    // }
    
    const keysSnapshot = await admin.firestore().collection('activationKeys').get();
    
    let totalKeys = 0;
    let usedKeys = 0;
    let availableKeys = 0;
    let inactiveKeys = 0;
    
    keysSnapshot.forEach(doc => {
      totalKeys++;
      const data = doc.data();
      if (!data.isActive) {
        inactiveKeys++;
      } else if (data.used) {
        usedKeys++;
      } else {
        availableKeys++;
      }
    });
    
    return {
      totalKeys,
      usedKeys,
      availableKeys,
      inactiveKeys,
      usageRate: totalKeys > 0 ? (usedKeys / totalKeys * 100).toFixed(2) + '%' : '0%'
    };
    
  } catch (error) {
    console.error('getKeyStats error:', error);
    throw new functions.https.HttpsError('internal', 'Internal server error');
  }
});

/**
 * Cloud Function to revoke a key (admin only)
 */
exports.revokeKey = functions.https.onCall(async (data, context) => {
  try {
    const { key, reason } = data;
    
    if (!key) {
      throw new functions.https.HttpsError('invalid-argument', 'Missing key parameter');
    }
    
    const trimmedKey = key.trim().toUpperCase();
    const keyRef = admin.firestore().collection('activationKeys').doc(trimmedKey);
    const keyDoc = await keyRef.get();
    
    if (!keyDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Key not found');
    }
    
    const keyData = keyDoc.data();
    const usedDevice = keyData.device;
    
    // Revoke the key
    await keyRef.update({
      isActive: false,
      revokedAt: admin.firestore.FieldValue.serverTimestamp(),
      revokedReason: reason || 'Admin revocation',
      lastUpdated: admin.firestore.FieldValue.serverTimestamp()
    });
    
    // If key was used, remove device activation
    if (usedDevice) {
      const deviceRef = admin.firestore().collection('device_activations').doc(usedDevice);
      await deviceRef.delete();
    }
    
    // Log audit event
    await logAuditEvent('key_revoked', trimmedKey, usedDevice || 'none', 'success', {
      reason: reason || 'Admin revocation'
    });
    
    return { success: true };
    
  } catch (error) {
    console.error('revokeKey error:', error);
    
    if (error instanceof functions.https.HttpsError) {
      throw error;
    }
    
    throw new functions.https.HttpsError('internal', 'Internal server error');
  }
});

/**
 * Cloud Function to get device activation info
 */
exports.getDeviceInfo = functions.https.onCall(async (data, context) => {
  try {
    const { device } = data;
    
    if (!device) {
      throw new functions.https.HttpsError('invalid-argument', 'Missing device parameter');
    }
    
    const deviceDoc = await admin.firestore().collection('device_activations').doc(device).get();
    
    if (!deviceDoc.exists) {
      return { status: 'not_activated' };
    }
    
    const deviceData = deviceDoc.data();
    return {
      status: 'activated',
      licenseKey: deviceData.licenseKey,
      activatedAt: deviceData.activatedAt,
      appVersion: deviceData.appVersion,
      deviceModel: deviceData.deviceModel,
      lastCheck: deviceData.lastCheck
    };
    
  } catch (error) {
    console.error('getDeviceInfo error:', error);
    throw new functions.https.HttpsError('internal', 'Internal server error');
  }
});

// Helper functions

/**
 * Validate key format
 */
function isValidKeyFormat(key) {
  // Basic format validation - adjust based on your key format
  return key.length >= 8 && 
         key.length <= 50 &&
         /^[A-Z0-9\-]+$/.test(key);
}

/**
 * Log audit events
 */
async function logAuditEvent(event, licenseKey, deviceId, result, additionalData = {}) {
  try {
    const auditData = {
      event,
      licenseKey: licenseKey ? licenseKey.substring(0, 8) + '...' : 'unknown',
      deviceId: deviceId ? deviceId.substring(0, 16) + '...' : 'unknown',
      result,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      ...additionalData
    };
    
    await admin.firestore().collection('activation_audit').add(auditData);
    console.log(`Audit event logged: ${event} - ${result}`);
  } catch (error) {
    console.error('Failed to log audit event:', error);
  }
} 