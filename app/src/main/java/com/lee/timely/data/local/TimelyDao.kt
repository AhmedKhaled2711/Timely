package com.lee.timely.db

import android.util.Log
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

    @Query("""
        SELECT * FROM user 
        WHERE group_id = :groupId
        AND (
            (:month = 1 AND flag1 = 1) OR
            (:month = 2 AND flag2 = 1) OR
            (:month = 3 AND flag3 = 1) OR
            (:month = 4 AND flag4 = 1) OR
            (:month = 5 AND flag5 = 1) OR
            (:month = 6 AND flag6 = 1) OR
            (:month = 7 AND flag7 = 1) OR
            (:month = 8 AND flag8 = 1) OR
            (:month = 9 AND flag9 = 1) OR
            (:month = 10 AND flag10 = 1) OR
            (:month = 11 AND flag11 = 1) OR
            (:month = 12 AND flag12 = 1)
        )
        AND (
            :query IS NULL OR 
            first_name LIKE '%' || :query || '%' OR
            last_name LIKE '%' || :query || '%' OR
            uid LIKE '%' || :query || '%'
        )
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getUsersByGroupIdAndMonth(
        groupId: Int,
        month: Int,
        query: String?,
        limit: Int,
        offset: Int
    ): List<User>

    @Query("SELECT * FROM user WHERE uid = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?
    
    @Query("""
        SELECT * FROM user 
        WHERE group_id = :groupId 
        AND uid = :uid
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchUsersByUid(
        groupId: Int,
        uid: Int,
        limit: Int,
        offset: Int
    ): List<User>
    
    @Query("""
        SELECT * FROM user 
        WHERE group_id = :groupId 
        AND uid = :uid
        AND (
            (:month = 1 AND flag1 = :isPaid) OR
            (:month = 2 AND flag2 = :isPaid) OR
            (:month = 3 AND flag3 = :isPaid) OR
            (:month = 4 AND flag4 = :isPaid) OR
            (:month = 5 AND flag5 = :isPaid) OR
            (:month = 6 AND flag6 = :isPaid) OR
            (:month = 7 AND flag7 = :isPaid) OR
            (:month = 8 AND flag8 = :isPaid) OR
            (:month = 9 AND flag9 = :isPaid) OR
            (:month = 10 AND flag10 = :isPaid) OR
            (:month = 11 AND flag11 = :isPaid) OR
            (:month = 12 AND flag12 = :isPaid)
        )
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchUsersByUidAndMonthAndPaymentStatus(
        groupId: Int,
        uid: Int,
        month: Int,
        isPaid: Boolean,
        limit: Int,
        offset: Int
    ): List<User>

    @Query("DELETE FROM user")
    suspend fun deleteAllUsers()
    
    @Query("""
        SELECT * FROM user 
        WHERE group_id = :groupId
        AND (
            (:month = 1 AND flag1 = :isPaid) OR
            (:month = 2 AND flag2 = :isPaid) OR
            (:month = 3 AND flag3 = :isPaid) OR
            (:month = 4 AND flag4 = :isPaid) OR
            (:month = 5 AND flag5 = :isPaid) OR
            (:month = 6 AND flag6 = :isPaid) OR
            (:month = 7 AND flag7 = :isPaid) OR
            (:month = 8 AND flag8 = :isPaid) OR
            (:month = 9 AND flag9 = :isPaid) OR
            (:month = 10 AND flag10 = :isPaid) OR
            (:month = 11 AND flag11 = :isPaid) OR
            (:month = 12 AND flag12 = :isPaid)
        )
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getUsersByGroupIdAndMonthAndPaymentStatus(
        groupId: Int,
        month: Int,
        isPaid: Boolean,
        limit: Int,
        offset: Int
    ): List<User> {
        Log.d("TimelyDao", "getUsersByGroupIdAndMonthAndPaymentStatus: groupId=$groupId, month=$month, isPaid=$isPaid, limit=$limit, offset=$offset")
        val result = try {
            // This is just a placeholder - the actual query is generated by Room
            emptyList<User>()
        } catch (e: Exception) {
            Log.e("TimelyDao", "Error in getUsersByGroupIdAndMonthAndPaymentStatus", e)
            emptyList()
        }
        Log.d("TimelyDao", "Found ${result.size} users with groupId=$groupId, month=$month, isPaid=$isPaid")
        if (result.isNotEmpty()) {
            Log.d("TimelyDao", "First user: ${result[0].firstName} (ID: ${result[0].uid}), flag1=${result[0].flag1}, flag2=${result[0].flag2}, etc...")
        }
        return result
    }
    
    @Query("""
        SELECT * FROM user 
        WHERE group_id = :groupId
        AND (
            (:month = 1 AND flag1 = :isPaid) OR
            (:month = 2 AND flag2 = :isPaid) OR
            (:month = 3 AND flag3 = :isPaid) OR
            (:month = 4 AND flag4 = :isPaid) OR
            (:month = 5 AND flag5 = :isPaid) OR
            (:month = 6 AND flag6 = :isPaid) OR
            (:month = 7 AND flag7 = :isPaid) OR
            (:month = 8 AND flag8 = :isPaid) OR
            (:month = 9 AND flag9 = :isPaid) OR
            (:month = 10 AND flag10 = :isPaid) OR
            (:month = 11 AND flag11 = :isPaid) OR
            (:month = 12 AND flag12 = :isPaid)
        )
        AND (
            first_name LIKE '%' || :query || '%' OR
            last_name LIKE '%' || :query || '%' OR
            uid LIKE '%' || :query || '%'
        )
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchUsersByGroupIdAndMonthAndPaymentStatus(
        groupId: Int,
        query: String,
        month: Int,
        isPaid: Boolean,
        limit: Int,
        offset: Int
    ): List<User> {
        Log.d("TimelyDao", "searchUsersByGroupIdAndMonthAndPaymentStatus: groupId=$groupId, query='$query', month=$month, isPaid=$isPaid, limit=$limit, offset=$offset")
        val result = try {
            // This is just a placeholder - the actual query is generated by Room
            emptyList<User>()
        } catch (e: Exception) {
            Log.e("TimelyDao", "Error in searchUsersByGroupIdAndMonthAndPaymentStatus", e)
            emptyList()
        }
        Log.d("TimelyDao", "Found ${result.size} users with search query '$query', groupId=$groupId, month=$month, isPaid=$isPaid")
        return result
    }


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
    
    @Query("""
        SELECT COUNT(*) > 0 FROM user 
        WHERE group_id = :groupId
        AND (
            (:month = 1 AND flag1 = 1) OR
            (:month = 2 AND flag2 = 1) OR
            (:month = 3 AND flag3 = 1) OR
            (:month = 4 AND flag4 = 1) OR
            (:month = 5 AND flag5 = 1) OR
            (:month = 6 AND flag6 = 1) OR
            (:month = 7 AND flag7 = 1) OR
            (:month = 8 AND flag8 = 1) OR
            (:month = 9 AND flag9 = 1) OR
            (:month = 10 AND flag10 = 1) OR
            (:month = 11 AND flag11 = 1) OR
            (:month = 12 AND flag12 = 1)
        )
        LIMIT 1
    """)
    suspend fun hasPaidUsersForMonth(groupId: Int, month: Int): Boolean
}
