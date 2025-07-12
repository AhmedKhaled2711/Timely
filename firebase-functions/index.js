const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

/**
 * Cloud Function to claim a license key
 * Provides atomic "claimKey" logic to prevent race conditions
 */
exports.claimKey = functions.https.onCall(async (data, context) => {
  try {
    const { key, device, appVersion, deviceModel } = data;
    
    // Validate input
    if (!key || !device) {
      throw new functions.https.HttpsError('invalid-argument', 'Missing key or device parameter');
    }
    
    const trimmedKey = key.trim().toUpperCase();
    
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
      
      // Determine action based on key status
      if (!isUsed) {
        // Key is available, claim it
        transaction.update(keyRef, {
          used: true,
          device: device,
          activatedAt: admin.firestore.FieldValue.serverTimestamp(),
          appVersion: appVersion || '1.0',
          deviceModel: deviceModel || 'Unknown',
          lastUpdated: admin.firestore.FieldValue.serverTimestamp()
        });
        return 'success';
      } else if (usedDevice === device) {
        // Key already claimed by this device
        return 'ok';
      } else {
        // Key used by different device
        throw new functions.https.HttpsError('already-exists', 'Key already used on another device');
      }
    });
    
    return result;
    
  } catch (error) {
    console.error('claimKey error:', error);
    
    if (error instanceof functions.https.HttpsError) {
      throw error;
    }
    
    throw new functions.https.HttpsError('internal', 'Internal server error');
  }
});

/**
 * Cloud Function to check key status
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
 * Cloud Function to deactivate a key
 */
exports.deactivateKey = functions.https.onCall(async (data, context) => {
  try {
    const { key, device } = data;
    
    if (!key || !device) {
      throw new functions.https.HttpsError('invalid-argument', 'Missing key or device parameter');
    }
    
    const trimmedKey = key.trim().toUpperCase();
    const keyDoc = await admin.firestore().collection('activationKeys').doc(trimmedKey).get();
    
    if (!keyDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Key not found');
    }
    
    const keyData = keyDoc.data();
    const usedDevice = keyData.device || null;
    
    if (usedDevice !== device) {
      throw new functions.https.HttpsError('permission-denied', 'Cannot deactivate key owned by another device');
    }
    
    // Reset the key to unused state
    await admin.firestore().collection('activationKeys').doc(trimmedKey).update({
      used: false,
      device: null,
      activatedAt: null,
      deactivatedAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    return { success: true };
    
  } catch (error) {
    console.error('deactivateKey error:', error);
    
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
    
    keysSnapshot.forEach(doc => {
      totalKeys++;
      const data = doc.data();
      if (data.used) {
        usedKeys++;
      } else {
        availableKeys++;
      }
    });
    
    return {
      totalKeys,
      usedKeys,
      availableKeys,
      usageRate: totalKeys > 0 ? (usedKeys / totalKeys * 100).toFixed(2) + '%' : '0%'
    };
    
  } catch (error) {
    console.error('getKeyStats error:', error);
    throw new functions.https.HttpsError('internal', 'Internal server error');
  }
}); 