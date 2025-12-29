package com.lee.timely.db

import com.lee.timely.domain.AcademicYearPayment
import com.lee.timely.domain.GroupName
import com.lee.timely.domain.GradeYear
import com.lee.timely.domain.User
import com.lee.timely.data.local.UserPagingSource
import kotlinx.coroutines.flow.Flow

interface TimelyLocalDataSource {

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

    // --- Academic Year Payment Queries ---
    suspend fun insertPayment(payment: AcademicYearPayment)
    suspend fun insertPayments(payments: List<AcademicYearPayment>)
    fun getPaymentsByUserAndAcademicYear(userId: Int, academicYear: String): Flow<List<AcademicYearPayment>>
    suspend fun getPaymentByUserAndMonth(userId: Int, academicYear: String, month: Int): AcademicYearPayment?
    suspend fun updatePaymentStatus(userId: Int, academicYear: String, month: Int, isPaid: Boolean, paymentDate: String? = null)
    suspend fun isPaymentMade(userId: Int, academicYear: String, month: Int): Boolean
    suspend fun hasPaymentsForAcademicYear(userId: Int, academicYear: String): Boolean
    suspend fun deletePaymentsForAcademicYear(userId: Int, academicYear: String)
    suspend fun hasPaidUsersForMonthInAcademicYear(groupId: Int, academicYear: String, month: Int): Boolean
    
    // Get users by group and payment status for specific academic year month
    suspend fun getUsersByGroupIdAndMonth(
        groupId: Int,
        academicYear: String,
        month: Int,
        query: String?,
        limit: Int,
        offset: Int
    ): List<User>
    
    // Search users by UID and month payment status for specific academic year
    suspend fun searchUsersByUidAndMonthAndPaymentStatus(
        groupId: Int,
        uid: Int,
        academicYear: String,
        month: Int,
        isPaid: Boolean,
        limit: Int,
        offset: Int
    ): List<User>
    
    // Get users by group, month, and payment status for specific academic year
    suspend fun getUsersByGroupIdAndMonthAndPaymentStatus(
        groupId: Int,
        academicYear: String,
        month: Int,
        isPaid: Boolean,
        limit: Int,
        offset: Int
    ): List<User>
    
    // Search users by group, query, month, and payment status for specific academic year
    suspend fun searchUsersByGroupIdAndMonthAndPaymentStatus(
        groupId: Int,
        academicYear: String,
        query: String,
        month: Int,
        isPaid: Boolean,
        limit: Int,
        offset: Int
    ): List<User>

    //Paginated Users
    suspend fun getUsersPaginated(limit: Int, offset: Int): List<User>
    suspend fun getUsersByGroupIdPaginated(groupId: Int, limit: Int, offset: Int): List<User>
    suspend fun getUsersCountByGroupId(groupId: Int): Int

    fun getAllUsers(): Flow<List<User>>
    fun getAllGroups(): Flow<List<GroupName>>
    suspend fun deleteAllUsers()
    suspend fun deleteAllGroups()
    suspend fun deleteAllSchoolYears()

    suspend fun updateUser(user: User)
    
    // Get user by ID
    suspend fun getUserById(userId: Int): User?
    
    // Paging 3 support
    fun getUsersPagingSource(
        groupId: Int, 
        searchQuery: String = "",
        month: Int? = null
    ): UserPagingSource
    
    // Check if any users have paid for a specific month
    //suspend fun hasPaidUsersForMonth(groupId: Int, month: Int): Boolean
    
    // Get users by payment status with paging support
    fun getUsersByPaymentStatusPagingSource(
        groupId: Int,
        searchQuery: String?,
        month: Int,
        isPaid: Boolean,
        academicYear: String = com.lee.timely.util.AcademicYearUtils.getCurrentAcademicYear()
    ): UserPagingSource
    
    // Get user payments for a specific academic year
    suspend fun getUserPayments(userId: Int, academicYear: String): List<AcademicYearPayment>

}

