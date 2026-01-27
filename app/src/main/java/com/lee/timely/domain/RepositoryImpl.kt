package com.lee.timely.domain

import androidx.paging.PagingSource
import com.lee.timely.data.local.UserPagingSource
import com.lee.timely.db.TimelyLocalDataSource
import com.lee.timely.domain.AcademicYearPayment
import com.lee.timely.domain.GradeYear
import com.lee.timely.domain.GroupName
import com.lee.timely.domain.User
import android.util.Log
import kotlinx.coroutines.flow.Flow

class RepositoryImpl(private val localDataSource: TimelyLocalDataSource) : Repository {

    companion object {
        private var instance: RepositoryImpl? = null
        fun getInstance(
            localDataSource: TimelyLocalDataSource
        ): RepositoryImpl {
            return instance ?: synchronized(this) {
                val temp = RepositoryImpl(
                    localDataSource
                )
                instance = temp
                temp
            }

        }
    }

    override fun getAll(): Flow<List<User>> {
        return localDataSource.getAll()
    }


    override suspend fun insertUser(user: User) {
        return localDataSource.insertUser(user)
    }

    override suspend fun deleteUser(user: User) {
        return localDataSource.deleteUser(user)
    }

    override fun getUsersByGroupId(groupId: Int): Flow<List<User>> {
        return localDataSource.getUsersByGroupId(groupId)
    }

    override fun getAllSchoolYears(): Flow<List<GradeYear>> {
        return localDataSource.getAllSchoolYears()
    }

    override fun getGroupsForSchoolYearId(schoolYearId: Int): Flow<List<GroupName>> {
        return localDataSource.getGroupsForSchoolYearId(schoolYearId)
    }

    override fun getGroupById(groupId: Int): Flow<GroupName?> {
        return localDataSource.getGroupById(groupId)
    }

    override suspend fun insertSchoolYear(schoolYear: GradeYear) {
        return localDataSource.insertSchoolYear(schoolYear)
    }

    override suspend fun updateSchoolYear(schoolYear: GradeYear) {
        return localDataSource.updateSchoolYear(schoolYear)
    }

    override suspend fun deleteSchoolYear(schoolYear: GradeYear) {
        return localDataSource.deleteSchoolYear(schoolYear)
    }


    override suspend fun insertGroup(group: GroupName) {
        localDataSource.insertGroup(group)
    }

    override suspend fun updateGroup(group: GroupName) {
        localDataSource.updateGroup(group)
    }

    override suspend fun deleteGroup(group: GroupName) {
        localDataSource.deleteGroup(group)
    }

    // --- Academic Year Payment Implementations ---
    override suspend fun insertPayment(payment: AcademicYearPayment) {
        localDataSource.insertPayment(payment)
    }

    override suspend fun insertPayments(payments: List<AcademicYearPayment>) {
        localDataSource.insertPayments(payments)
    }

    override fun getPaymentsByUserAndAcademicYear(userId: Int, academicYear: String): Flow<List<AcademicYearPayment>> {
        return localDataSource.getPaymentsByUserAndAcademicYear(userId, academicYear)
    }

    override suspend fun getPaymentByUserAndMonth(userId: Int, academicYear: String, month: Int): AcademicYearPayment? {
        return localDataSource.getPaymentByUserAndMonth(userId, academicYear, month)
    }

    override suspend fun updatePaymentStatus(userId: Int, academicYear: String, month: Int, isPaid: Boolean, paymentDate: String?) {
        localDataSource.updatePaymentStatus(userId, academicYear, month, isPaid, paymentDate)
    }

    override suspend fun isPaymentMade(userId: Int, academicYear: String, month: Int): Boolean {
        return localDataSource.isPaymentMade(userId, academicYear, month)
    }

    override suspend fun hasPaymentsForAcademicYear(userId: Int, academicYear: String): Boolean {
        return localDataSource.hasPaymentsForAcademicYear(userId, academicYear)
    }

    override suspend fun deletePaymentsForAcademicYear(userId: Int, academicYear: String) {
        localDataSource.deletePaymentsForAcademicYear(userId, academicYear)
    }

    override suspend fun hasPaidUsersForMonthInAcademicYear(groupId: Int, academicYear: String, month: Int): Boolean {
        return localDataSource.hasPaidUsersForMonthInAcademicYear(groupId, academicYear, month)
    }

    override suspend fun getUsersByGroupIdAndMonth(
        groupId: Int,
        academicYear: String,
        month: Int,
        query: String?,
        limit: Int,
        offset: Int
    ): List<User> {
        return localDataSource.getUsersByGroupIdAndMonth(groupId, academicYear, month, query, limit, offset)
    }

    override suspend fun searchUsersByUidAndMonthAndPaymentStatus(
        groupId: Int,
        uid: Int,
        academicYear: String,
        month: Int,
        isPaid: Boolean,
        limit: Int,
        offset: Int
    ): List<User> {
        return localDataSource.searchUsersByUidAndMonthAndPaymentStatus(groupId, uid, academicYear, month, isPaid, limit, offset)
    }

    override suspend fun getUsersByGroupIdAndMonthAndPaymentStatus(
        groupId: Int,
        academicYear: String,
        month: Int,
        isPaid: Boolean,
        limit: Int,
        offset: Int
    ): List<User> {
        return localDataSource.getUsersByGroupIdAndMonthAndPaymentStatus(groupId, academicYear, month, isPaid, limit, offset)
    }

    override suspend fun searchUsersByGroupIdAndMonthAndPaymentStatus(
        groupId: Int,
        academicYear: String,
        query: String,
        month: Int,
        isPaid: Boolean,
        limit: Int,
        offset: Int
    ): List<User> {
        return localDataSource.searchUsersByGroupIdAndMonthAndPaymentStatus(groupId, academicYear, query, month, isPaid, limit, offset)
    }

    override suspend fun getUsersPaginated(limit: Int, offset: Int): List<User> {
        return localDataSource.getUsersPaginated(limit,offset)
    }

    override suspend fun getUsersByGroupIdPaginated(
        groupId: Int,
        page: Int,
        pageSize: Int
    ): List<User> {
        val offset = page * pageSize
        return localDataSource.getUsersByGroupIdPaginated(groupId,pageSize,offset)
    }

    override suspend fun getUsersCountByGroupId(groupId: Int): Int {
        return localDataSource.getUsersCountByGroupId(groupId)
    }

    override suspend fun updateUser(user: User) {
        return localDataSource.updateUser(user)
    }

    override suspend fun getUserById(userId: Int): User? {
        return localDataSource.getUserById(userId)
    }
    
    override fun getUsersPagingSource(
        groupId: Int, 
        searchQuery: String,
        month: Int?
    ): UserPagingSource {
        return localDataSource.getUsersPagingSource(
            groupId = groupId,
            searchQuery = searchQuery,
            month = month
        )
    }
    
    override fun getUsersPagingSource(groupId: Int, searchQuery: String): UserPagingSource {
        return localDataSource.getUsersPagingSource(groupId, searchQuery, null)
    }
    
    override fun getUsersByPaymentStatusPagingSource(
        groupId: Int,
        searchQuery: String?,
        month: Int,
        isPaid: Boolean,
        academicYear: String
    ): UserPagingSource {
        return localDataSource.getUsersByPaymentStatusPagingSource(
            groupId = groupId,
            searchQuery = searchQuery,
            month = month,
            isPaid = isPaid,
            academicYear = academicYear
        )
    }
    
    override suspend fun hasPaidUsersForMonth(groupId: Int, month: Int, academicYear: String): Boolean {
        // Get the first page of paid users for this month and academic year
        val pagingSource = localDataSource.getUsersByPaymentStatusPagingSource(
            groupId = groupId,
            searchQuery = null,
            month = month,
            isPaid = true,
            academicYear = academicYear
        )
        
        // Load the first page with a small page size (1) just to check if any paid users exist
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = 1,
                placeholdersEnabled = false
            )
        )
        
        return when (result) {
            is PagingSource.LoadResult.Page -> result.data.isNotEmpty()
            else -> false
        }
    }
    
    override suspend fun updateUserPaymentStatus(userId: Int, month: Int, isPaid: Boolean) {
        Log.d("RepositoryImpl", "updateUserPaymentStatus called: userId=$userId, month=$month, isPaid=$isPaid")
        // Use the current academic year for payment status updates
        val currentAcademicYear = com.lee.timely.util.AcademicYearUtils.getCurrentAcademicYear()
        Log.d("RepositoryImpl", "Current academic year: $currentAcademicYear")
        val academicYearMonths = com.lee.timely.util.AcademicYearUtils.getAcademicYearMonths(currentAcademicYear)
        
        // Find the year for this month in the current academic year
        val monthYearPair = academicYearMonths.find { it.first == month }
            ?: throw IllegalArgumentException("Invalid month: $month. Month must be between 1 and 12.")
        
        Log.d("RepositoryImpl", "Month year pair: $monthYearPair")
        
        // Update the payment status using the academic year payment system
        Log.d("RepositoryImpl", "About to call localDataSource.updatePaymentStatus")
        localDataSource.updatePaymentStatus(
            userId = userId,
            academicYear = currentAcademicYear,
            month = month,
            isPaid = isPaid,
            paymentDate = if (isPaid) java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) else null
        )
        Log.d("RepositoryImpl", "updatePaymentStatus completed successfully")
    }
    
    override suspend fun getUserPayments(userId: Int, academicYear: String): List<AcademicYearPayment> {
        return localDataSource.getUserPayments(userId, academicYear)
    }
    
    // --- Duplicate Validation Implementation ---
    override suspend fun isDuplicateStudentName(groupId: Int, fullName: String): Boolean {
        return localDataSource.isDuplicateStudentName(groupId, fullName)
    }
}