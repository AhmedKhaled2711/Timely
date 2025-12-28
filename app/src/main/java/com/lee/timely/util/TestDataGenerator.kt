package com.lee.timely.util

/*
object TestDataGenerator {
    
    private val fullNames = listOf(
        "Ahmed Al-Rashid", "Mohammed Al-Zahra", "Ali Al-Mahmoud", "Omar Al-Sayed", "Hassan Al-Hassan", "Hussein Al-Hussein",
        "Youssef Al-Ibrahim", "Ibrahim Al-Khalil", "Khalil Al-Rashid", "Rashid Al-Tariq", "Tariq Al-Zaid", "Zaid Al-Malik",
        "Malik Al-Nasser", "Nasser Al-Samir", "Samir Al-Karim", "Karim Al-Fadi", "Fadi Al-Waleed", "Waleed Al-Bassam",
        "Bassam Al-Adel", "Adel Al-Hisham", "Hisham Al-Mazen", "Mazen Al-Rami", "Rami Al-Samer", "Samer Al-Ahmad",
        "Ahmad Al-Mahmoud", "Mahmoud Al-Mustafa", "Mustafa Al-Saleh", "Saleh Al-Abdullah", "Abdullah Al-Khalid", "Khalid Al-Saeed", "Saeed Al-Yahya"
    )
    
    suspend fun generateTestStudents(
        context: Context, 
        groupId: Int, 
        count: Int = 500
    ): List<User> = withContext(Dispatchers.IO) {
        val students = mutableListOf<User>()
        
        for (i in 1..count) {
            val allName = fullNames.random()
            val studentNumber = "STU${String.format("%04d", i)}"
            val guardianNumber = "+9665${(10000000..99999999).random()}"
            val startDate = "2024-01-${String.format("%02d", (1..28).random())}"
            
            val student = User(
                allName = allName,
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