package com.lee.timely


import android.content.res.Configuration
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
import com.lee.timely.home.viewModel.MainViewModel
import com.lee.timely.home.viewModel.MainViewModelFactory
import com.lee.timely.model.RepositoryImpl
import com.lee.timely.navigation.AppNavGraph
import com.lee.timely.ui.theme.TimelyTheme
import java.util.Locale
import android.util.LayoutDirection as AndroidLayoutDirection


class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(RepositoryImpl.getInstance(TimelyLocalDataSourceImpl.getInstance(this)))
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
                    AppNavGraph(viewModel = viewModel)
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