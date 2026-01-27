package com.lee.timely

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import com.lee.timely.db.TimelyLocalDataSourceImpl
import com.lee.timely.features.home.viewmodel.viewModel.MainViewModel
import com.lee.timely.features.home.viewmodel.viewModel.MainViewModelFactory
import com.lee.timely.domain.RepositoryImpl
import com.lee.timely.navigation.NavGraph
import com.lee.timely.ui.theme.TimelyTheme
import com.lee.timely.ui.theme.PrimaryBlue
import com.lee.timely.ui.theme.SurfaceWhite
import com.lee.timely.util.EnhancedLicenseManager
import com.lee.timely.util.LicenseValidationResult
import com.lee.timely.util.setAppLocale
import com.lee.timely.R
import java.util.Locale
import android.util.LayoutDirection as AndroidLayoutDirection
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var licenseManager: EnhancedLicenseManager
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(RepositoryImpl.getInstance(TimelyLocalDataSourceImpl.getInstance(this)), application)
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("lang", Locale.getDefault().language) ?: "en"
        val context = setAppLocale(newBase, lang)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize licenseManager
        licenseManager = EnhancedLicenseManager(this)

        installSplashScreen()
        enableEdgeToEdge()

        // Set status bar color to primary
        window.statusBarColor = SurfaceWhite.toArgb()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            true

        setContent {
            TimelyTheme {
                val layoutDirection =
                    if (LocalConfiguration.current.layoutDirection == AndroidLayoutDirection.RTL) {
                        LayoutDirection.Rtl
                    } else {
                        LayoutDirection.Ltr
                    }

                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainApp(licenseManager = licenseManager, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MainApp(licenseManager: EnhancedLicenseManager, viewModel: MainViewModel) {
    var isActivated by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var showActivationScreen by remember { mutableStateOf(false) }

    // License check on app start
    LaunchedEffect(Unit) {
        try {
            showActivationScreen = licenseManager.shouldShowActivationScreen()
            if (!showActivationScreen) {
                val validationResult = licenseManager.performLicenseValidation()
                isActivated = validationResult is LicenseValidationResult.Valid
                if (!isActivated) {
                    showActivationScreen = true
                }
            }
        } catch (e: Exception) {
            showActivationScreen = true
            isActivated = false
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        // Show loading screen
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.checking_license))
            }
        }
    } else {
        // Use NavGraph for all navigation and activation
        NavGraph(
            isActivated = isActivated,
            showActivationScreen = showActivationScreen,
            onActivationComplete = {
                isActivated = true
                showActivationScreen = false
            },
            viewModel = viewModel
        )
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.hello_name, name),
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TimelyTheme {
        Greeting("Android")
    }
}