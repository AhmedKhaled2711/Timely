package com.lee.timely.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lee.timely.model.GroupName
import com.lee.timely.model.GradeYear
import com.lee.timely.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelyDao {

    // --- User Queries ---
    @Query("SELECT * FROM user")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM user LIMIT :limit OFFSET :offset")
    suspend fun getUsersPaginated(limit: Int, offset: Int): List<User>

    @Insert
    suspend fun insertUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM user WHERE group_id = :groupId")
    fun getUsersByGroupId(groupId: Int): Flow<List<User>>

    @Query("SELECT * FROM user WHERE group_id = :groupId LIMIT :limit OFFSET :offset")
    suspend fun getUsersByGroupIdPaginated(groupId: Int, limit: Int, offset: Int): List<User>

    @Query("SELECT COUNT(*) FROM user WHERE group_id = :groupId")
    suspend fun getUsersCountByGroupId(groupId: Int): Int


    // --- SchoolYear Queries ---
    @Insert
    suspend fun insertSchoolYear(schoolYear: GradeYear)

    @Delete
    suspend fun deleteSchoolYear(schoolYear: GradeYear)

    @Query("SELECT * FROM school_year")
    fun getAllSchoolYears(): Flow<List<GradeYear>>


    // --- Group Queries ---
    @Query("SELECT * FROM group_name WHERE schoolYearId = :schoolYearId")
    fun getGroupsForSchoolYearId(schoolYearId: Int): Flow<List<GroupName>>

    @Query("SELECT * FROM group_name WHERE id = :groupId LIMIT 1")
    fun getGroupById(groupId: Int): Flow<GroupName?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupName)

    @Delete
    suspend fun deleteGroup(group: GroupName)


    // --- User Flag Updates ---
    @Query("UPDATE user SET flag1 = :value WHERE uid = :userId")
    suspend fun updateFlag1(userId: Int, value: Boolean)

    @Query("UPDATE user SET flag2 = :value WHERE uid = :userId")
    suspend fun updateFlag2(userId: Int, value: Boolean)

    @Query("UPDATE user SET flag3 = :value WHERE uid = :userId")
    suspend fun updateFlag3(userId: Int, value: Boolean)

    @Query("UPDATE user SET flag4 = :value WHERE uid = :userId")
    suspend fun updateFlag4(userId: Int, value: Boolean)

    @Query("UPDATE user SET flag5 = :value WHERE uid = :userId")
    suspend fun updateFlag5(userId: Int, value: Boolean)

    @Query("UPDATE user SET flag6 = :value WHERE uid = :userId")
    suspend fun updateFlag6(userId: Int, value: Boolean)
}
