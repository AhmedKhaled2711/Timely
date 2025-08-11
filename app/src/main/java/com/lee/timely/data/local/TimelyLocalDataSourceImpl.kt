package com.lee.timely.data.local

import android.content.Context
import android.util.Log
import com.lee.timely.model.GroupName
import com.lee.timely.model.GradeYear
import com.lee.timely.model.User
import kotlinx.coroutines.flow.Flow

private const val TAG = "TimelyLocalDS"

class TimelyLocalDataSourceImpl private constructor(context: Context) : TimelyLocalDataSource {

    private val timelyDao: TimelyDao
    
    init {
        Log.d(TAG, "Initializing TimelyLocalDataSourceImpl")
        try {
            val database = TimelyDatabase.getInstance(context.applicationContext)
            timelyDao = database.getTimelyDao()
            Log.d(TAG, "Database and DAO initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize database or DAO", e)
            throw IllegalStateException("Failed to initialize database", e)
        }
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

    override suspend fun updateFlag1(userId: Int, value: Boolean) {
        return timelyDao.updateFlag1(userId , value)
    }

    override suspend fun updateFlag2(userId: Int, value: Boolean) {
        return timelyDao.updateFlag2(userId , value)
    }

    override suspend fun updateFlag3(userId: Int, value: Boolean) {
        return timelyDao.updateFlag3(userId , value)
    }

    override suspend fun updateFlag4(userId: Int, value: Boolean) {
        return timelyDao.updateFlag4(userId , value)
    }

    override suspend fun updateFlag5(userId: Int, value: Boolean) {
        return timelyDao.updateFlag5(userId , value)
    }

    override suspend fun updateFlag6(userId: Int, value: Boolean) {
        return timelyDao.updateFlag6(userId , value)
    }

    override suspend fun updateFlag7(userId: Int, value: Boolean) {
        return timelyDao.updateFlag7(userId , value)
    }

    override suspend fun updateFlag8(userId: Int, value: Boolean) {
        return timelyDao.updateFlag8(userId , value)
    }

    override suspend fun updateFlag9(userId: Int, value: Boolean) {
        return timelyDao.updateFlag9(userId , value)
    }

    override suspend fun updateFlag10(userId: Int, value: Boolean) {
        return timelyDao.updateFlag10(userId , value)
    }

    override suspend fun updateFlag11(userId: Int, value: Boolean) {
        return timelyDao.updateFlag11(userId , value)
    }

    override suspend fun updateFlag12(userId: Int, value: Boolean) {
        return timelyDao.updateFlag12(userId , value)
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

    override fun getUsersPagingSource(groupId: Int, searchQuery: String): UserPagingSource {
        return UserPagingSource(timelyDao, groupId, searchQuery)
    }

}