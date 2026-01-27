package com.lee.timely.features.settings

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.lee.timely.R
import com.lee.timely.ui.theme.ErrorRed
import com.lee.timely.ui.theme.LighterSecondaryBlue
import com.lee.timely.ui.theme.OnBackground
import com.lee.timely.ui.theme.PaleSecondaryBlue
import com.lee.timely.ui.theme.PrimaryBlue
import com.lee.timely.ui.theme.SecondaryBlue
import com.lee.timely.ui.theme.SuccessGreen
import com.lee.timely.util.ActivationResult
import com.lee.timely.util.ActivationStatus
import com.lee.timely.util.EnhancedLicenseManager
import com.lee.timely.BuildConfig
import kotlinx.coroutines.delay

@Composable
fun ActivationScreen(
    onNavigateToHome: () -> Unit,
    viewModel: ActivationViewModel = viewModel()
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    LaunchedEffect(Unit) { viewModel.context = context }

    var licenseKey by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var messageColor by remember { mutableStateOf(SuccessGreen) }
    var isActivated by remember { mutableStateOf(false) }
    var activationInfo by remember { mutableStateOf("") }
    var deviceInfo by remember { mutableStateOf("") }

    val licenseManager = remember { EnhancedLicenseManager(context) }

    // Helper function to check internet connectivity
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    LaunchedEffect(Unit) {
        try {
            val status = licenseManager.checkActivationStatus()
            isActivated = status is ActivationStatus.Activated
            if (isActivated) activationInfo = licenseManager.getActivationInfo()
            deviceInfo = licenseManager.getDeviceInfo()
        } catch (e: Exception) {
            message = context.getString(R.string.error_checking_activation, e.message ?: "")
            messageColor = ErrorRed
        }
    }

    val googleSignInState by viewModel.googleSignInState.collectAsState()
    
    // Get OAuth Client ID from BuildConfig (loaded from local.properties)
    val oauthClientId = BuildConfig.GOOGLE_SIGN_IN_CLIENT_ID
    
    val googleSignInClient = remember {
        if (oauthClientId.isBlank()) {
            // If OAuth Client ID is not configured, show error
            null
        } else {
            GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(oauthClientId)
                    .requestEmail()
                    .build()
            )
        }
    }
    val googleSignInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            viewModel.onGoogleSignInResult(account.idToken)
        } catch (e: Exception) {
            viewModel.onGoogleSignInResult(null)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.license_activation),
            style = typography.titleLarge,
            color = PrimaryBlue,
            modifier = Modifier.padding(bottom = 28.dp)
        )

        if (isActivated) {
            Card(
                modifier = Modifier.fillMaxWidth(0.95f),
                colors = CardDefaults.cardColors(containerColor = PaleSecondaryBlue)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.app_is_activated),
                        style = typography.titleMedium,
                        color = SuccessGreen
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = activationInfo,
                        style = typography.bodyLarge,
                        color = OnBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    // Removed refresh and deactivate buttons - users can only activate
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(0.95f),
                colors = CardDefaults.cardColors(containerColor = PaleSecondaryBlue)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.enter_license_key),
                        style = typography.titleMedium,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    if (!isActivated) {
                        // Google Sign-In Button
                        if (googleSignInClient == null) {
                            // Show configuration error if OAuth Client ID is missing
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Error: Google Sign-In is not configured. Please set GOOGLE_SIGN_IN_CLIENT_ID in local.properties",
                                color = ErrorRed,
                                style = typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        } else {
                            FilledTonalButton(
                                onClick = { 
                                    try {
                                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                    } catch (e: Exception) {
                                        message = "Error launching Google Sign-In: ${e.localizedMessage}"
                                        messageColor = ErrorRed
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_google_c),
                                    contentDescription = stringResource(R.string.google_sign_in),
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.sign_in_with_google),
                                    color = Color.Black
                                )
                            }
                        }
                        if (googleSignInState.error != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = googleSignInState.error ?: "",
                                color = ErrorRed,
                                style = typography.bodyMedium
                            )
                        }
                        if (googleSignInState.success) {
                            // Optionally show a success message or proceed
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.google_sign_in_success),
                                color = SuccessGreen,
                                style = typography.bodyMedium
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    val keyDescription = stringResource(R.string.license_key)
                    OutlinedTextField(
                        value = licenseKey,
                        onValueChange = { licenseKey = it },
                        label = { Text(stringResource(R.string.license_key), color = SecondaryBlue) },
                        placeholder = { Text(stringResource(R.string.license_key_placeholder), color = LighterSecondaryBlue) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { 
                                contentDescription = keyDescription
                            },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = SecondaryBlue,
                            cursorColor = PrimaryBlue
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                    FilledTonalButton(
                        onClick = {
                            if (!isInternetAvailable(context)) {
                                message = context.getString(R.string.internet_required)
                                messageColor = ErrorRed
                                return@FilledTonalButton
                            }
                            if (licenseKey.isNotBlank()) {
                                viewModel.launch {
                                    try {
                                        isLoading = true
                                        message = ""
                                        when (val result = licenseManager.activateKey(licenseKey)) {
                                            is ActivationResult.Success -> {
                                                message = result.message
                                                messageColor = SuccessGreen
                                                isActivated = true
                                                activationInfo = licenseManager.getActivationInfo()
                                                viewModel.launch {
                                                    delay(1500)
                                                    onNavigateToHome()
                                                }
                                            }
                                            is ActivationResult.Error -> {
                                                message = result.message
                                                messageColor = ErrorRed
                                            }
                                        }
                                    } catch (e: Exception) {
                                        message = context.getString(R.string.error_with_message, e.message ?: "")
                                        messageColor = ErrorRed
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            } else {
                                message = context.getString(R.string.please_enter_license_key)
                                messageColor = ErrorRed
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && licenseKey.isNotBlank() && googleSignInState.success,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.activate), color = Color.White)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

        }
        if (message.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (messageColor == SuccessGreen) LighterSecondaryBlue else ErrorRed.copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                Text(
                    text = message,
                    color = messageColor,
                    textAlign = TextAlign.Center,
                    style = typography.bodyLarge,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        Spacer(Modifier.height(32.dp))
    }
} 