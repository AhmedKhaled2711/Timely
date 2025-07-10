package com.lee.timely.features.testing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lee.timely.R
//import com.lee.timely.util.TestDataGenerator
import kotlinx.coroutines.launch

// /*
// @OptIn(ExperimentalMaterial3Api::class)
// @Composable
// fun PerformanceTestScreen(
//     navController: NavController,
//     groupId: Int
// ) {
//     val context = LocalContext.current
//     val coroutineScope = rememberCoroutineScope()
//     var isLoading by remember { mutableStateOf(false) }
//     var testResults by remember { mutableStateOf("") }
//     var studentCount by remember { mutableStateOf("500") }
    
//     Scaffold(
//         topBar = {
//             TopAppBar(
//                 title = { Text("Performance Testing") },
//                 navigationIcon = {
//                     IconButton(onClick = { navController.popBackStack() }) {
//                         Icon(
//                             imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                             contentDescription = "Back"
//                         )
//                     }
//                 }
//             )
//         }
//     ) { padding ->
//         Column(
//             modifier = Modifier
//                 .fillMaxSize()
//                 .padding(padding)
//                 .padding(16.dp)
//                 .verticalScroll(rememberScrollState()),
//             verticalArrangement = Arrangement.spacedBy(16.dp)
//         ) {
//             Text(
//                 text = "Performance Testing for Group $groupId",
//                 style = MaterialTheme.typography.headlineSmall
//             )
            
//             // Student count input
//             OutlinedTextField(
//                 value = studentCount,
//                 onValueChange = { studentCount = it },
//                 label = { Text("Number of students to generate") },
//                 modifier = Modifier.fillMaxWidth(),
//                 keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
//                     keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
//                 )
//             )
            
//             // Test buttons
//             Row(
//                 modifier = Modifier.fillMaxWidth(),
//                 horizontalArrangement = Arrangement.spacedBy(8.dp)
//             ) {
//                 Button(
//                     onClick = {
//                         coroutineScope.launch {
//                             isLoading = true
//                             try {
//                                 val count = studentCount.toIntOrNull() ?: 500
//                                 val startTime = System.currentTimeMillis()
//                                 val insertedCount = TestDataGenerator.insertTestStudents(context, groupId, count)
//                                 val endTime = System.currentTimeMillis()
//                                 val duration = endTime - startTime
                                
//                                 testResults = """
//                                     ✅ Successfully inserted $insertedCount students
//                                     ⏱️ Insertion time: ${duration}ms
//                                     📊 Average time per student: ${duration / insertedCount}ms
//                                     💾 Database operation completed successfully
//                                 """.trimIndent()
//                             } catch (e: Exception) {
//                                 testResults = "❌ Error: ${e.message}"
//                             } finally {
//                                 isLoading = false
//                             }
//                         }
//                     },
//                     modifier = Modifier.weight(1f),
//                     enabled = !isLoading
//                 ) {
//                     Icon(Icons.Default.Add, contentDescription = null)
//                     Spacer(modifier = Modifier.width(8.dp))
//                     Text("Generate Students")
//                 }
                
//                 Button(
//                     onClick = {
//                         coroutineScope.launch {
//                             isLoading = true
//                             try {
//                                 val startTime = System.currentTimeMillis()
//                                 TestDataGenerator.clearTestData(context)
//                                 val endTime = System.currentTimeMillis()
//                                 val duration = endTime - startTime
                                
//                                 testResults = """
//                                     🗑️ Cleared all test data
//                                     ⏱️ Clear operation time: ${duration}ms
//                                     ✅ Database cleared successfully
//                                 """.trimIndent()
//                             } catch (e: Exception) {
//                                 testResults = "❌ Error: ${e.message}"
//                             } finally {
//                                 isLoading = false
//                             }
//                         }
//                     },
//                     modifier = Modifier.weight(1f),
//                     enabled = !isLoading,
//                     colors = ButtonDefaults.buttonColors(
//                         containerColor = MaterialTheme.colorScheme.error
//                     )
//                 ) {
//                     Icon(Icons.Default.Delete, contentDescription = null)
//                     Spacer(modifier = Modifier.width(8.dp))
//                     Text("Clear Data")
//                 }
//             }
            
//             // Performance tips
//             Card(
//                 modifier = Modifier.fillMaxWidth(),
//                 colors = CardDefaults.cardColors(
//                     containerColor = MaterialTheme.colorScheme.surfaceVariant
//                 )
//             ) {
//                 Column(
//                     modifier = Modifier.padding(16.dp)
//                 ) {
//                     Text(
//                         text = "Performance Tips:",
//                         style = MaterialTheme.typography.titleMedium,
//                         fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
//                     )
//                     Spacer(modifier = Modifier.height(8.dp))
//                     Text("• Use Paging 3 for lists with 500+ items")
//                     Text("• LazyColumn only renders visible items")
//                     Text("• Database operations run on background threads")
//                     Text("• Search is optimized for Arabic and English")
//                     Text("• Memory usage is minimized with pagination")
//                 }
//             }
            
//             // Test results
//             if (testResults.isNotEmpty()) {
//                 Card(
//                     modifier = Modifier.fillMaxWidth(),
//                     colors = CardDefaults.cardColors(
//                         containerColor = MaterialTheme.colorScheme.primaryContainer
//                     )
//                 ) {
//                     Column(
//                         modifier = Modifier.padding(16.dp)
//                     ) {
//                         Text(
//                             text = "Test Results:",
//                             style = MaterialTheme.typography.titleMedium,
//                             fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
//                         )
//                         Spacer(modifier = Modifier.height(8.dp))
//                         Text(
//                             text = testResults,
//                             fontSize = 14.sp,
//                             lineHeight = 20.sp
//                         )
//                     }
//                 }
//             }
            
//             // Loading indicator
//             if (isLoading) {
//                 Box(
//                     modifier = Modifier.fillMaxWidth(),
//                     contentAlignment = Alignment.Center
//                 ) {
//                     CircularProgressIndicator()
//                 }
//             }
            
//             // Navigation to test the actual list
//             Button(
//                 onClick = { navController.popBackStack() },
//                 modifier = Modifier.fillMaxWidth()
//             ) {
//                 Text("Go Back to Group Details")
//             }
//         }
//     }
// }
// */ 