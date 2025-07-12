package com.lee.timely.util

import com.lee.timely.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import com.lee.timely.db.TimelyLocalDataSourceImpl

/*
object TestDataGenerator {
    
    private val firstNames = listOf(
        "Ahmed", "Mohammed", "Ali", "Omar", "Hassan", "Hussein", "Youssef", "Ibrahim",
        "Khalil", "Rashid", "Tariq", "Zaid", "Malik", "Nasser", "Samir", "Karim",
        "Fadi", "Waleed", "Bassam", "Adel", "Hisham", "Mazen", "Rami", "Samer",
        "Ahmad", "Mahmoud", "Mustafa", "Saleh", "Abdullah", "Khalid", "Saeed", "Yahya"
    )
    
    private val lastNames = listOf(
        "Al-Rashid", "Al-Zahra", "Al-Mahmoud", "Al-Sayed", "Al-Hassan", "Al-Hussein",
        "Al-Ibrahim", "Al-Khalil", "Al-Rashid", "Al-Tariq", "Al-Zaid", "Al-Malik",
        "Al-Nasser", "Al-Samir", "Al-Karim", "Al-Fadi", "Al-Waleed", "Al-Bassam",
        "Al-Adel", "Al-Hisham", "Al-Mazen", "Al-Rami", "Al-Samer", "Al-Ahmad",
        "Al-Mahmoud", "Al-Mustafa", "Al-Saleh", "Al-Abdullah", "Al-Khalid", "Al-Saeed"
    )
    
    suspend fun generateTestStudents(
        context: Context, 
        groupId: Int, 
        count: Int = 500
    ): List<User> = withContext(Dispatchers.IO) {
        val students = mutableListOf<User>()
        
        for (i in 1..count) {
            val firstName = firstNames.random()
            val lastName = lastNames.random()
            val studentNumber = "STU${String.format("%04d", i)}"
            val guardianNumber = "+9665${(10000000..99999999).random()}"
            val startDate = "2024-01-${String.format("%02d", (1..28).random())}"
            
            val student = User(
                firstName = firstName,
                lastName = lastName,
                groupId = groupId,
                studentNumber = studentNumber,
                guardiansNumber = guardianNumber,
                startDate = startDate,
                // Randomly set some months as paid
                flag1 = (1..12).random() <= 6,
                flag2 = (1..12).random() <= 6,
                flag3 = (1..12).random() <= 6,
                flag4 = (1..12).random() <= 6,
                flag5 = (1..12).random() <= 6,
                flag6 = (1..12).random() <= 6,
                flag7 = (1..12).random() <= 6,
                flag8 = (1..12).random() <= 6,
                flag9 = (1..12).random() <= 6,
                flag10 = (1..12).random() <= 6,
                flag11 = (1..12).random() <= 6,
                flag12 = (1..12).random() <= 6
            )
            
            students.add(student)
        }
        
        students
    }
    
    suspend fun insertTestStudents(
        context: Context,
        groupId: Int,
        count: Int = 500
    ) = withContext(Dispatchers.IO) {
        val dataSource = TimelyLocalDataSourceImpl.getInstance(context)
        val students = generateTestStudents(context, groupId, count)
        
        students.forEach { student ->
            dataSource.insertUser(student)
        }
        
        students.size
    }
    
    suspend fun clearTestData(context: Context) = withContext(Dispatchers.IO) {
        val dataSource = TimelyLocalDataSourceImpl.getInstance(context)
        dataSource.deleteAllUsers()
    }
}
*/