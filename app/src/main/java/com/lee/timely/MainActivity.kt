package com.lee.timely


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.lee.timely.db.TimelyLocalDataSourceImpl
import com.lee.timely.features.home.viewmodel.viewModel.MainViewModel
import com.lee.timely.features.home.viewmodel.viewModel.MainViewModelFactory
import com.lee.timely.model.RepositoryImpl
import com.lee.timely.navigation.AppNavGraph
import com.lee.timely.ui.theme.TimelyTheme
import android.util.LayoutDirection as AndroidLayoutDirection
import android.content.Context
import java.util.*
import com.lee.timely.util.setAppLocale
import com.lee.timely.features.settings.ActivationScreen
import android.provider.Settings
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.CircularProgressIndicator
import com.lee.timely.util.ActivationState


class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(RepositoryImpl.getInstance(TimelyLocalDataSourceImpl.getInstance(this)))
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("lang", Locale.getDefault().language) ?: "en"
        val context = setAppLocale(newBase, lang)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
        enableEdgeToEdge()

        setContent {
            TimelyTheme {
                val layoutDirection = if (LocalConfiguration.current.layoutDirection == AndroidLayoutDirection.RTL) {
                    LayoutDirection.Rtl
                } else {
                    LayoutDirection.Ltr
                }

                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    ActivationGate(viewModel)
                }
            }
        }

    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
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

@Composable
fun ActivationGate(viewModel: MainViewModel) {
    val context = LocalContext.current
    var activationState by remember { mutableStateOf(ActivationState.Loading) }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isActivated = prefs.getBoolean("is_activated", false)
        if (isActivated) {
            activationState = ActivationState.Activated
        } else {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val securePrefs = EncryptedSharedPreferences.create(
                "secure_prefs",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            val storedKey = securePrefs.getString("license_key", null)
            val storedId = securePrefs.getString("device_id", null)
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            if (storedKey != null && storedId != null && storedId == androidId) {
                prefs.edit().putBoolean("is_activated", true).apply()
                activationState = ActivationState.Activated
            } else {
                activationState = ActivationState.NotActivated
            }
        }
    }

    when (activationState) {
        ActivationState.Loading -> CircularProgressIndicator()
        ActivationState.NotActivated -> ActivationScreen(onActivated = {
            context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit().putBoolean("is_activated", true).apply()
            activationState = ActivationState.Activated
        })
        ActivationState.Activated -> AppNavGraph(viewModel = viewModel)
    }
}