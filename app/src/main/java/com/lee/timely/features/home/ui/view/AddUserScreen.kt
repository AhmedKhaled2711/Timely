package com.lee.timely.features.home.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lee.timely.R
import com.lee.timely.features.home.viewmodel.viewModel.MainViewModel
import com.lee.timely.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    viewModel: MainViewModel,
    groupId: Int,
    onUserAdded: () -> Unit,
    onBackPressed: () -> Unit,
    editUser: User? = null
) {
    var firstName by remember { mutableStateOf(editUser?.firstName ?: "") }
    var lastName by remember { mutableStateOf(editUser?.lastName ?: "") }
    var guardiansNumber by remember { mutableStateOf(editUser?.guardiansNumber ?: "") }
    var startDate by remember { mutableStateOf(editUser?.startDate ?: "") }
    var studentNumber by remember { mutableStateOf(editUser?.studentNumber ?: "") }
    var firstNameError by remember { mutableStateOf(false) }
    var lastNameError by remember { mutableStateOf(false) }
    var startDateError by remember { mutableStateOf(false) }
    var studentNumberError by remember { mutableStateOf(false) }
    var guardiansNumberError by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    val isArabic = Locale.getDefault().language == "ar"
    val isEditMode = editUser != null
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = if (isEditMode) R.string.edit_student else R.string.add_student),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackPressed() }) {
                        Icon(
                            imageVector = if (isArabic) Icons.Filled.ArrowForward else Icons.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .padding(padding)
                .padding(8.dp)
        ) {
            OutlinedTextField(
                value = firstName,
                onValueChange = {
                    val filtered = it.filter { c -> c.isLetter() || c.isWhitespace() }
                        .replace(Regex("\\s+"), " ")
                        .trimStart()
                        .take(30)
                    firstName = filtered
                    firstNameError = filtered.length < 3
                },
                label = { Text(stringResource(id = R.string.first_name_label), color = MaterialTheme.colorScheme.onSurface) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                isError = firstNameError,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            if (firstNameError) {
                Text(
                    text = stringResource(id = R.string.error_enter_first_name),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 4.dp),
                    textAlign = TextAlign.Start
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = lastName,
                onValueChange = {
                    val filtered = it.filter { c -> c.isLetter() || c.isWhitespace() }
                        .replace(Regex("\\s+"), " ")
                        .trimStart()
                        .take(30)
                    lastName = filtered
                    lastNameError = filtered.length < 3
                },
                label = { Text(stringResource(id = R.string.last_name_label), color = MaterialTheme.colorScheme.onSurface) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                isError = lastNameError,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            if (lastNameError) {
                Text(
                    text = stringResource(id = R.string.error_enter_last_name),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 4.dp),
                    textAlign = TextAlign.Start
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = studentNumber,
                onValueChange = {
                    val filtered = it.filter { c -> c.isDigit() }.take(11)
                    studentNumber = filtered
                    studentNumberError = filtered.isNotBlank() && filtered.length < 11
                },
                label = { Text(stringResource(id = R.string.student_number_label), color = MaterialTheme.colorScheme.onSurface) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = MaterialTheme.shapes.medium,
                isError = studentNumberError,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next, keyboardType = KeyboardType.Number),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            if (studentNumberError) {
                Text(
                    text = stringResource(id = R.string.error_enter_student_number),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 4.dp),
                    textAlign = TextAlign.Start
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = guardiansNumber,
                onValueChange = {
                    val filtered = it.filter { c -> c.isDigit() }.take(11)
                    guardiansNumber = filtered
                    guardiansNumberError = filtered.isNotBlank() && filtered.length < 11
                },
                label = { Text(stringResource(id = R.string.guardians_number_label), color = MaterialTheme.colorScheme.onSurface) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = MaterialTheme.shapes.medium,
                isError = guardiansNumberError,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next, keyboardType = KeyboardType.Number),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            if (guardiansNumberError) {
                Text(
                    text = stringResource(id = R.string.error_enter_guardians_number),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 4.dp),
                    textAlign = TextAlign.Start
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = startDate,
                onValueChange = {
                    startDate = it
                    if (it.isNotBlank()) startDateError = false
                },
                label = { Text(stringResource(id = R.string.start_date_label), color = MaterialTheme.colorScheme.onSurface) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                isError = startDateError,
                trailingIcon = {
                    IconButton(onClick = {
                        val sdf = SimpleDateFormat("yyyy-MM-dd")
                        startDate = sdf.format(Date())
                        startDateError = false
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = stringResource(id = R.string.use_today))
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                })
            )
            if (startDate.isBlank()) {
                val sdf = SimpleDateFormat("yyyy-MM-dd")
                val today = sdf.format(Date())
                TextButton(onClick = {
                    startDate = today
                    startDateError = false
                }) {
                    Text(text = stringResource(id = R.string.suggest_today, today))
                }
            }
            if (startDateError) {
                Text(
                    text = stringResource(id = R.string.error_enter_start_date),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 4.dp),
                    textAlign = TextAlign.Start
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            val useAddedDescription = stringResource(R.string.user_added)
            Button(
                onClick = {
                    firstNameError = firstName.length < 3
                    lastNameError = lastName.length < 3
                    studentNumberError = studentNumber.isNotBlank() && studentNumber.length < 11
                    guardiansNumberError = guardiansNumber.isNotBlank() && guardiansNumber.length < 11
                    startDateError = startDate.isBlank()
                    if (firstNameError || lastNameError || studentNumberError || guardiansNumberError || startDateError) {
                        // Auto-scroll to first error field
                        coroutineScope.launch {
                            when {
                                firstNameError -> scrollState.animateScrollTo(0)
                                lastNameError -> scrollState.animateScrollTo(100)
                                studentNumberError -> scrollState.animateScrollTo(200)
                                guardiansNumberError -> scrollState.animateScrollTo(300)
                                startDateError -> scrollState.animateScrollTo(400)
                            }
                        }
                        return@Button
                    }
                    if (isEditMode) {
                        viewModel.updateUser(
                            editUser!!.copy(
                                firstName = firstName,
                                lastName = lastName,
                                guardiansNumber = guardiansNumber,
                                startDate = startDate,
                                studentNumber = studentNumber
                            )
                        )
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("User updated")
                        }
                    } else {
                        viewModel.addUser(
                            User(
                                firstName = firstName,
                                lastName = lastName,
                                groupId = groupId,
                                guardiansNumber = guardiansNumber,
                                startDate = startDate,
                                studentNumber = studentNumber
                            )
                        )
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(useAddedDescription)
                        }
                    }
                    onUserAdded()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(id = if (isEditMode) R.string.edit_student else R.string.add_student),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}