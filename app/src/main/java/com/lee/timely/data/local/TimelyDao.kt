package com.lee.timely.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    @Query("""
        SELECT * FROM user 
        WHERE group_id = :groupId 
        AND (first_name LIKE '%' || :query || '%' 
        OR last_name LIKE '%' || :query || '%')
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchUsersByGroupId(groupId: Int, query: String, limit: Int, offset: Int): List<User>

    @Query("SELECT COUNT(*) FROM user WHERE group_id = :groupId")
    suspend fun getUsersCountByGroupId(groupId: Int): Int

    @Query("SELECT * FROM user WHERE uid = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    @Query("DELETE FROM user")
    suspend fun deleteAllUsers()


    // --- SchoolYear Queries ---
    @Insert
    suspend fun insertSchoolYear(schoolYear: GradeYear)
    
    @Update
    suspend fun updateSchoolYear(schoolYear: GradeYear)
    
    @Delete
    suspend fun deleteSchoolYear(schoolYear: GradeYear)

    @Query("SELECT * FROM school_year")
    fun getAllSchoolYears(): Flow<List<GradeYear>>

    @Query("DELETE FROM school_year")
    suspend fun deleteAllSchoolYears()


    // --- Group Queries ---
    @Query("SELECT * FROM group_name WHERE schoolYearId = :schoolYearId")
    fun getGroupsForSchoolYearId(schoolYearId: Int): Flow<List<GroupName>>

    @Query("SELECT * FROM group_name WHERE id = :groupId LIMIT 1")
    fun getGroupById(groupId: Int): Flow<GroupName?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupName)
    
    @Update
    suspend fun updateGroup(group: GroupName)
    
    @Delete
    suspend fun deleteGroup(group: GroupName)

    @Query("DELETE FROM group_name")
    suspend fun deleteAllGroups()

    @Query("SELECT * FROM group_name")
    fun getAllGroups(): Flow<List<GroupName>>


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

    @Query("UPDATE user SET flag7 = :value WHERE uid = :userId")
    suspend fun updateFlag7(userId: Int, value: Boolean)

    @Query("UPDATE user SET flag8 = :value WHERE uid = :userId")
    suspend fun updateFlag8(userId: Int, value: Boolean)

    @Query("UPDATE user SET flag9 = :value WHERE uid = :userId")
    suspend fun updateFlag9(userId: Int, value: Boolean)

    @Query("UPDATE user SET flag10 = :value WHERE uid = :userId")
    suspend fun updateFlag10(userId: Int, value: Boolean)

    @Query("UPDATE user SET flag11 = :value WHERE uid = :userId")
    suspend fun updateFlag11(userId: Int, value: Boolean)

    @Query("UPDATE user SET flag12 = :value WHERE uid = :userId")
    suspend fun updateFlag12(userId: Int, value: Boolean)

    @Update
    suspend fun updateUser(user: User)
}
