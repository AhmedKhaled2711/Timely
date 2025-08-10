package com.lee.timely.model

import kotlinx.coroutines.flow.Flow
import com.lee.timely.data.local.UserPagingSource

interface Repository {

    // --- User Queries ---
    fun getAll(): Flow<List<User>>
    suspend fun insertUser(user: User)
    suspend fun deleteUser(user: User)
    fun getUsersByGroupId(groupId: Int): Flow<List<User>>

    // --- SchoolYear Queries ---
    suspend fun insertSchoolYear(schoolYear: GradeYear)
    suspend fun updateSchoolYear(schoolYear: GradeYear)
    suspend fun deleteSchoolYear(schoolYear: GradeYear)
    fun getAllSchoolYears(): Flow<List<GradeYear>>

    // --- Group Queries ---
    fun getGroupsForSchoolYearId(schoolYearId: Int): Flow<List<GroupName>>
    fun getGroupById(groupId: Int): Flow<GroupName?>
    suspend fun insertGroup(group: GroupName)
    suspend fun updateGroup(group: GroupName)
    suspend fun deleteGroup(group: GroupName)

    // --- User Flag Updates ---
    suspend fun updateFlag1(userId: Int, value: Boolean)
    suspend fun updateFlag2(userId: Int, value: Boolean)
    suspend fun updateFlag3(userId: Int, value: Boolean)
    suspend fun updateFlag4(userId: Int, value: Boolean)
    suspend fun updateFlag5(userId: Int, value: Boolean)
    suspend fun updateFlag6(userId: Int, value: Boolean)
    suspend fun updateFlag7(userId: Int, value: Boolean)
    suspend fun updateFlag8(userId: Int, value: Boolean)
    suspend fun updateFlag9(userId: Int, value: Boolean)
    suspend fun updateFlag10(userId: Int, value: Boolean)
    suspend fun updateFlag11(userId: Int, value: Boolean)
    suspend fun updateFlag12(userId: Int, value: Boolean)

    //Paginated Users
    suspend fun getUsersPaginated(limit: Int, offset: Int): List<User>
    suspend fun getUsersByGroupIdPaginated(groupId: Int, page: Int, pageSize: Int): List<User>
    suspend fun getUsersCountByGroupId(groupId: Int): Int

    suspend fun updateUser(user: User)
    
    // Get user by ID
    suspend fun getUserById(userId: Int): User?
    
    // Paging 3 support
    fun getUsersPagingSource(groupId: Int, searchQuery: String = ""): UserPagingSource

}