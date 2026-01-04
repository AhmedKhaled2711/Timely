package com.lee.timely.features.home.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lee.timely.R
import com.lee.timely.domain.User
import com.lee.timely.features.home.ui.state.AddUserUiEvent
import com.lee.timely.features.home.ui.state.AddUserUiState
import com.lee.timely.features.home.viewmodel.viewModel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    viewModel: MainViewModel,
    groupId: Int,
    onUserAdded: () -> Unit,
    onBackPressed: () -> Unit,
    editUser: User? = null
) {
    var allName by remember { mutableStateOf(editUser?.allName ?: "") }
    var guardiansNumber by remember { mutableStateOf(editUser?.guardiansNumber ?: "") }
    var startDate by remember { mutableStateOf(editUser?.startDate ?: "") }
    var studentNumber by remember { mutableStateOf(editUser?.studentNumber ?: "") }
    var allNameError by remember { mutableStateOf(false) }
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
    val context = LocalContext.current

    // Observe add user UI state and events
    val addUserUiState by viewModel.addUserUiState.collectAsState()

    // Get success messages outside of LaunchedEffect
    val successMessage = if (isEditMode) 
        stringResource(R.string.user_updated) else 
        stringResource(R.string.user_added)

    // Reset loading state when screen is first displayed
    LaunchedEffect(Unit) {
        if (addUserUiState is AddUserUiState.Loading) {
            // Reset loading state when entering screen
            viewModel.resetAddUserUiState()
        }
    }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.addUserEvent.collect { event ->
            when (event) {
                is AddUserUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is AddUserUiEvent.NavigateBack -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(successMessage)
                    }
                    onUserAdded()
                }
                is AddUserUiEvent.None -> { /* No action needed */ }
            }
        }
    }
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
                            imageVector = if (isArabic) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .imePadding()
                    .padding(padding)
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = allName,
                    onValueChange = {
                        val filtered = it.filter { c -> c.isLetterOrDigit() || c.isWhitespace() }
                            .replace(Regex("\\s+"), " ")
                            .trimStart()
                            .take(60)
                        allName = filtered
                        allNameError = filtered.length < 3
                    },
                    label = { Text(stringResource(id = R.string.full_name_label), color = MaterialTheme.colorScheme.onSurface) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    isError = allNameError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                if (allNameError) {
                    Text(
                        text = stringResource(id = R.string.error_enter_full_name),
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
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
                Button(
                    onClick = {
                        // Prevent multiple clicks during loading
                        if (addUserUiState is AddUserUiState.Loading) return@Button

                        // Validate form fields
                        allNameError = allName.length < 3
                        studentNumberError = studentNumber.isNotBlank() && studentNumber.length < 11
                        guardiansNumberError = guardiansNumber.isNotBlank() && guardiansNumber.length < 11
                        startDateError = startDate.isBlank()

                        if (allNameError || studentNumberError || guardiansNumberError || startDateError) {
                            // Auto-scroll to first error field
                            coroutineScope.launch {
                                when {
                                    allNameError -> scrollState.animateScrollTo(0)
                                    studentNumberError -> scrollState.animateScrollTo(100)
                                    guardiansNumberError -> scrollState.animateScrollTo(200)
                                    startDateError -> scrollState.animateScrollTo(300)
                                }
                            }
                            return@Button
                        }

                        // Proceed with operation if validation passes
                        if (isEditMode) {
                            viewModel.updateUser(
                                editUser!!.copy(
                                    allName = allName,
                                    guardiansNumber = guardiansNumber,
                                    startDate = startDate,
                                    studentNumber = studentNumber
                                )
                            )
                        } else {
                            viewModel.addUser(
                                User(
                                    allName = allName,
                                    groupId = groupId,
                                    guardiansNumber = guardiansNumber,
                                    startDate = startDate,
                                    studentNumber = studentNumber
                                ),
                                context
                            )
                        }
                    },
                    enabled = addUserUiState !is AddUserUiState.Loading, // Disable button during loading
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
                    if (addUserUiState is AddUserUiState.Loading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                        ) {
//                            Box(
//                                modifier = Modifier
//                                    .size(24.dp)
//                                    .background(Color.White, CircleShape)
//                            ) {
//                                CircularProgressIndicator(
//                                    modifier = Modifier.size(24.dp),
//                                    strokeWidth = 2.dp,
//                                    color = Color.Black
//                                )
//                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEditMode) stringResource(R.string.user_updated) else stringResource(R.string.add_student),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    } else {
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

            // Loading overlay
            if (addUserUiState is AddUserUiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.shapes.medium
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 4.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (isEditMode) stringResource(R.string.updating_student) else stringResource(R.string.adding_student),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}