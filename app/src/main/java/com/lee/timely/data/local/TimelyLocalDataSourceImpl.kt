package com.lee.timely.db

import android.content.Context
import com.lee.timely.data.local.TimelyDao
import com.lee.timely.data.local.UserPagingSource
import com.lee.timely.domain.AcademicYearPayment
import com.lee.timely.domain.GroupName
import com.lee.timely.domain.GradeYear
import com.lee.timely.domain.User
import kotlinx.coroutines.flow.Flow

class TimelyLocalDataSourceImpl (context: Context) : TimelyLocalDataSource {

    private var timelyDao: TimelyDao
    init {
        val dataBase = TimelyDatabase.getInstance(context)
        timelyDao = dataBase.getTimelyDao()
    }

    companion object {
        @Volatile
        private var instance: TimelyLocalDataSourceImpl? = null
        fun getInstance(context: Context): TimelyLocalDataSourceImpl {
            if(instance == null)
                instance = TimelyLocalDataSourceImpl(context)
            return instance as TimelyLocalDataSourceImpl
        }
    }

    override fun getAll(): Flow<List<User>> {
        return timelyDao.getAllUsers()
    }

    override fun getAllUsers(): Flow<List<User>> {
        return timelyDao.getAllUsers()
    }

    override suspend fun insertUser(user: User) {
        return timelyDao.insertUser(user)
    }

    override suspend fun deleteUser(user: User) {
        return timelyDao.deleteUser(user)
    }

    override fun getUsersByGroupId(groupId: Int): Flow<List<User>> {
        return timelyDao.getUsersByGroupId(groupId)
    }

    override fun getAllSchoolYears(): Flow<List<GradeYear>> {
        return timelyDao.getAllSchoolYears()
    }

    override fun getGroupsForSchoolYearId(schoolYearId: Int): Flow<List<GroupName>> {
        return timelyDao.getGroupsForSchoolYearId(schoolYearId)
    }

    override fun getGroupById(groupId: Int): Flow<GroupName?> {
        return timelyDao.getGroupById(groupId)
    }

    override suspend fun insertSchoolYear(schoolYear: GradeYear) {
        return timelyDao.insertSchoolYear(schoolYear)
    }

    override suspend fun updateSchoolYear(schoolYear: GradeYear) {
        return timelyDao.updateSchoolYear(schoolYear)
    }

    override suspend fun deleteSchoolYear(schoolYear: GradeYear) {
        return timelyDao.deleteSchoolYear(schoolYear)
    }

    override suspend fun insertGroup(group: GroupName) {
        return timelyDao.insertGroup(group)
    }

    override suspend fun updateGroup(group: GroupName) {
        return timelyDao.updateGroup(group)
    }

    override suspend fun deleteGroup(group: GroupName) {
        return timelyDao.deleteGroup(group)
    }

    // --- Academic Year Payment Implementations ---
    override suspend fun insertPayment(payment: AcademicYearPayment) {
        return timelyDao.insertPayment(payment)
    }

    override suspend fun insertPayments(payments: List<AcademicYearPayment>) {
        return timelyDao.insertPayments(payments)
    }

    override fun getPaymentsByUserAndAcademicYear(userId: Int, academicYear: String): Flow<List<AcademicYearPayment>> {
        return timelyDao.getPaymentsByUserAndAcademicYear(userId, academicYear)
    }

    override suspend fun getPaymentByUserAndMonth(userId: Int, academicYear: String, month: Int): AcademicYearPayment? {
        return timelyDao.getPaymentByUserAndMonth(userId, academicYear, month)
    }

    override suspend fun updatePaymentStatus(userId: Int, academicYear: String, month: Int, isPaid: Boolean, paymentDate: String?) {
        // Use INSERT OR REPLACE to handle both update and insert cases
        // Extract year from academic year (e.g., "2025/2026" -> 2025 for months 9-12, 2026 for months 1-8)
        val academicYearMonths = com.lee.timely.util.AcademicYearUtils.getAcademicYearMonths(academicYear)
        val monthYearPair = academicYearMonths.find { it.first == month }
            ?: throw IllegalArgumentException("Invalid month: $month for academic year: $academicYear")
        val year = monthYearPair.second
        
        val payment = AcademicYearPayment(
            userId = userId,
            academicYear = academicYear,
            month = month,
            year = year,
            isPaid = isPaid,
            paymentDate = paymentDate
        )
        return timelyDao.insertPayment(payment)
    }

    override suspend fun isPaymentMade(userId: Int, academicYear: String, month: Int): Boolean {
        return timelyDao.isPaymentMade(userId, academicYear, month)
    }

    override suspend fun hasPaymentsForAcademicYear(userId: Int, academicYear: String): Boolean {
        return timelyDao.hasPaymentsForAcademicYear(userId, academicYear)
    }

    override suspend fun deletePaymentsForAcademicYear(userId: Int, academicYear: String) {
        return timelyDao.deletePaymentsForAcademicYear(userId, academicYear)
    }

    override suspend fun hasPaidUsersForMonthInAcademicYear(groupId: Int, academicYear: String, month: Int): Boolean {
        return timelyDao.hasPaidUsersForMonthInAcademicYear(groupId, academicYear, month)
    }

    override suspend fun getUsersByGroupIdAndMonth(
        groupId: Int,
        academicYear: String,
        month: Int,
        query: String?,
        limit: Int,
        offset: Int
    ): List<User> {
        return timelyDao.getUsersByGroupIdAndMonth(groupId, academicYear, month, query, limit, offset)
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
        return timelyDao.searchUsersByUidAndMonthAndPaymentStatus(groupId, uid, academicYear, month, isPaid, limit, offset)
    }

    override suspend fun getUsersByGroupIdAndMonthAndPaymentStatus(
        groupId: Int,
        academicYear: String,
        month: Int,
        isPaid: Boolean,
        limit: Int,
        offset: Int
    ): List<User> {
        return timelyDao.getUsersByGroupIdAndMonthAndPaymentStatus(groupId, academicYear, month, isPaid, limit, offset)
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
        return timelyDao.searchUsersByGroupIdAndMonthAndPaymentStatus(groupId, academicYear, query, month, isPaid, limit, offset)
    }

    override suspend fun getUsersPaginated(limit: Int, offset: Int): List<User> {
        return timelyDao.getUsersPaginated(limit , offset)
    }

    override suspend fun getUsersByGroupIdPaginated(
        groupId: Int,
        limit: Int,
        offset: Int
    ): List<User> {
        return timelyDao.getUsersByGroupIdPaginated(groupId,limit,offset)
    }

    override suspend fun getUsersCountByGroupId(groupId: Int): Int {
        return timelyDao.getUsersCountByGroupId(groupId)
    }

    override fun getAllGroups(): Flow<List<GroupName>> {
        return timelyDao.getAllGroups()
    }

    override suspend fun deleteAllUsers() {
        timelyDao.deleteAllUsers()
    }

    override suspend fun deleteAllGroups() {
        timelyDao.deleteAllGroups()
    }

    override suspend fun deleteAllSchoolYears() {
        timelyDao.deleteAllSchoolYears()
    }

    override suspend fun updateUser(user: User) {
        timelyDao.updateUser(user)
    }

    override suspend fun getUserById(userId: Int): User? {
        return timelyDao.getUserById(userId)
    }

    override fun getUsersPagingSource(
        groupId: Int,
        searchQuery: String,
        month: Int?
    ): UserPagingSource {
        return UserPagingSource(
            dao = timelyDao, 
            groupId = groupId, 
            searchQuery = searchQuery,
            month = month
        )
    }
    
    override fun getUsersByPaymentStatusPagingSource(
        groupId: Int,
        searchQuery: String?,
        month: Int,
        isPaid: Boolean,
        academicYear: String
    ): UserPagingSource {
        return UserPagingSource(
            dao = timelyDao,
            groupId = groupId,
            searchQuery = searchQuery ?: "",
            month = month,
            isPaid = isPaid,
            academicYear = academicYear
        )
    }
    
//    override suspend fun hasPaidUsersForMonth(groupId: Int, month: Int): Boolean {
//        return timelyDao.hasPaidUsersForMonth(groupId, month)
//    }
    
    override suspend fun getUserPayments(userId: Int, academicYear: String): List<AcademicYearPayment> {
        return timelyDao.getUserPayments(userId, academicYear)
    }
    
    // --- Duplicate Validation Implementation ---
    override suspend fun isDuplicateStudentName(groupId: Int, fullName: String): Boolean {
        return timelyDao.isDuplicateStudentName(groupId, fullName)
    }

}