package com.lee.timely

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.lee.timely.db.TimelyLocalDataSourceImpl
import com.lee.timely.home.view.RegisterScreen
import com.lee.timely.home.viewModel.MainViewModel
import com.lee.timely.home.viewModel.MainViewModelFactory
import com.lee.timely.model.RepositoryImpl
import com.lee.timely.navigation.AppNavGraph
import com.lee.timely.ui.theme.TimelyTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(RepositoryImpl.getInstance(TimelyLocalDataSourceImpl.getInstance(this)))
    }





//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        installSplashScreen()
//        enableEdgeToEdge()
//        setContent {
//            TimelyTheme {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ){
//                    Greeting(
//                        name = "Android",
//                    )
//                }
//            }
//        }
//
//        // Load existing users
//        viewModel.loadUsers()
//
//        // Observe users
//        lifecycleScope.launch {
//            viewModel.users.collect { users ->
//                println("USERS: $users")
//            }
//        }
//
//        // Add dummy user
////        val dummyUser = User(uid = 1, firstName = "Ahmed", lastName = "Khaled")
////        viewModel.addUser(dummyUser)
//        val dummyUser2 = User(uid = 2, firstName = "Ahmed", lastName = "Mohamed")
//        //viewModel.addUser(dummyUser2)
//
//        // To delete:
//         viewModel.deleteUser(dummyUser2)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                //UserScreen(viewModel)
//                RegisterScreen(
//                    onRegisterClick = { fullName, email, password ->
//                        // Handle registration
//                    },
//                    onLoginClick = {
//                        // Navigate to login screen
//                    },
//                    onGoogleRegisterClick = {
//                        // Handle Google registration
//                    }
//                )
                AppNavGraph(viewModel = viewModel)
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