package com.lee.timely.model

import com.lee.timely.data.local.UserPagingSource
import com.lee.timely.db.TimelyLocalDataSource
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

    override suspend fun updateFlag1(userId: Int, value: Boolean) {
        return localDataSource.updateFlag1(userId,value)
    }

    override suspend fun updateFlag2(userId: Int, value: Boolean) {
        return localDataSource.updateFlag2(userId,value)
    }

    override suspend fun updateFlag3(userId: Int, value: Boolean) {
        return localDataSource.updateFlag3(userId,value)
    }

    override suspend fun updateFlag4(userId: Int, value: Boolean) {
        return localDataSource.updateFlag4(userId,value)
    }

    override suspend fun updateFlag5(userId: Int, value: Boolean) {
        return localDataSource.updateFlag5(userId,value)
    }

    override suspend fun updateFlag6(userId: Int, value: Boolean) {
        return localDataSource.updateFlag6(userId,value)
    }

    override suspend fun updateFlag7(userId: Int, value: Boolean) {
        return localDataSource.updateFlag7(userId,value)
    }

    override suspend fun updateFlag8(userId: Int, value: Boolean) {
        return localDataSource.updateFlag8(userId,value)
    }

    override suspend fun updateFlag9(userId: Int, value: Boolean) {
        return localDataSource.updateFlag9(userId,value)
    }

    override suspend fun updateFlag10(userId: Int, value: Boolean) {
        return localDataSource.updateFlag10(userId,value)
    }

    override suspend fun updateFlag11(userId: Int, value: Boolean) {
        return localDataSource.updateFlag11(userId,value)
    }

    override suspend fun updateFlag12(userId: Int, value: Boolean) {
        return localDataSource.updateFlag12(userId,value)
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
    
    override fun getUsersPagingSource(groupId: Int, searchQuery: String): UserPagingSource {
        return localDataSource.getUsersPagingSource(groupId, searchQuery)
    }
}