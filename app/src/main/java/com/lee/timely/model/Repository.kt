package com.lee.timely.model

import kotlinx.coroutines.flow.Flow

interface Repository {

    // --- User Queries ---
    fun getAll(): Flow<List<User>>
    suspend fun insertUser(user: User)
    suspend fun deleteUser(user: User)
    fun getUsersByGroupId(groupId: Int): Flow<List<User>>

    // --- SchoolYear Queries ---
    suspend fun insertSchoolYear(schoolYear: GradeYear)
    suspend fun deleteSchoolYear(schoolYear: GradeYear)
    fun getAllSchoolYears(): Flow<List<GradeYear>>

    // --- Group Queries ---
    fun getGroupsForSchoolYearId(schoolYearId: Int): Flow<List<GroupName>>
    fun getGroupById(groupId: Int): Flow<GroupName?>
    suspend fun insertGroup(group: GroupName)
    suspend fun deleteGroup(group: GroupName)

    // --- User Flag Updates ---
    suspend fun updateFlag1(userId: Int, value: Boolean)
    suspend fun updateFlag2(userId: Int, value: Boolean)
    suspend fun updateFlag3(userId: Int, value: Boolean)
    suspend fun updateFlag4(userId: Int, value: Boolean)
    suspend fun updateFlag5(userId: Int, value: Boolean)
    suspend fun updateFlag6(userId: Int, value: Boolean)


}