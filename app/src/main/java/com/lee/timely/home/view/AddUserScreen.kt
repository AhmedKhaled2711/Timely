package com.lee.timely.home.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.lee.timely.R
import com.lee.timely.home.viewModel.MainViewModel
import com.lee.timely.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    viewModel: MainViewModel,
    groupId: Int,
    onUserAdded: () -> Unit,
    onBackPressed: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    // Check if the current language is Arabic
    val isArabic = Locale.getDefault().language == "ar"

    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()) {

        // Top App Bar with Back Button and Centered Title
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.add_user),
                    style = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                )
            },
            navigationIcon = {
                IconButton(onClick = { onBackPressed() }) {
                    Icon(
                        imageVector = if (isArabic) Icons.Filled.ArrowForward else Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // First Name Input with styling and padding
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text(stringResource(id = R.string.first_name_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), // Adding inner padding for comfort
            shape = MaterialTheme.shapes.medium, // Rounded corners
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Last Name Input with styling and padding
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text(stringResource(id = R.string.last_name_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), // Adding inner padding for comfort
            shape = MaterialTheme.shapes.medium, // Rounded corners
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Add User Button with rounded corners and shadow effect
        Button(
            onClick = {
                viewModel.addUser(User(firstName = firstName, lastName = lastName, groupId = groupId))
                firstName = ""
                lastName = ""
                onUserAdded()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium, // Rounded corners
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = stringResource(id = R.string.add_user),
                style = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}
