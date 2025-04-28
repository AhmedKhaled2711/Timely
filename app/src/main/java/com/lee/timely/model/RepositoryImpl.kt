package com.lee.timely.model

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

    override suspend fun deleteSchoolYear(schoolYear: GradeYear) {
        return localDataSource.deleteSchoolYear(schoolYear)
    }


    override suspend fun insertGroup(group: GroupName) {
        return localDataSource.insertGroup(group)
    }

    override suspend fun deleteGroup(group: GroupName) {
        return localDataSource.deleteGroup(group)
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
}