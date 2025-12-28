package com.lee.timely.data.local

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lee.timely.domain.AcademicYearPayment
import com.lee.timely.domain.GroupName
import com.lee.timely.domain.GradeYear
import com.lee.timely.domain.User
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

    @Query("SELECT * FROM user WHERE group_id = :groupId ORDER BY all_name ASC")
    fun getUsersByGroupId(groupId: Int): Flow<List<User>>

    @Query("SELECT * FROM user WHERE group_id = :groupId ORDER BY all_name ASC LIMIT :limit OFFSET :offset")
    suspend fun getUsersByGroupIdPaginated(groupId: Int, limit: Int, offset: Int): List<User>

    @Query("""
        SELECT * FROM user 
        WHERE group_id = :groupId 
        AND (all_name LIKE '%' || :query || '%' OR CAST(uid AS TEXT) LIKE '%' || :query || '%' OR student_number LIKE '%' || :query || '%') 
        ORDER BY all_name ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchUsersByGroupId(groupId: Int, query: String, limit: Int, offset: Int): List<User>

    @Query("SELECT COUNT(*) FROM user WHERE group_id = :groupId")
    suspend fun getUsersCountByGroupId(groupId: Int): Int

    @Query("""
        SELECT u.* FROM user u
        INNER JOIN academic_year_payments ay ON u.uid = ay.user_id
        WHERE u.group_id = :groupId
        AND ay.academic_year = :academicYear
        AND ay.month = :month
        AND ay.is_paid = 1
        AND (
            :query IS NULL OR 
            u.all_name LIKE '%' || :query || '%' OR
            CAST(u.uid AS TEXT) LIKE '%' || :query || '%'
        )
        ORDER BY u.all_name ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getUsersByGroupIdAndMonth(
        groupId: Int,
        academicYear: String,
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
        SELECT u.* FROM user u
        INNER JOIN academic_year_payments ay ON u.uid = ay.user_id
        WHERE u.group_id = :groupId 
        AND u.uid = :uid
        AND ay.academic_year = :academicYear
        AND ay.month = :month
        AND ay.is_paid = :isPaid
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchUsersByUidAndMonthAndPaymentStatus(
        groupId: Int,
        uid: Int,
        academicYear: String,
        month: Int,
        isPaid: Boolean,
        limit: Int,
        offset: Int
    ): List<User>

    @Query("DELETE FROM user")
    suspend fun deleteAllUsers()
    
    @Query("""
        SELECT u.* FROM user u
        INNER JOIN academic_year_payments ay ON u.uid = ay.user_id
        WHERE u.group_id = :groupId
        AND ay.academic_year = :academicYear
        AND ay.month = :month
        AND ay.is_paid = :isPaid
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getUsersByGroupIdAndMonthAndPaymentStatus(
        groupId: Int,
        academicYear: String,
        month: Int,
        isPaid: Boolean,
        limit: Int,
        offset: Int
    ): List<User>
    
    @Query("""
        SELECT u.* FROM user u
        INNER JOIN academic_year_payments ay ON u.uid = ay.user_id
        WHERE u.group_id = :groupId
        AND ay.academic_year = :academicYear
        AND ay.month = :month
        AND ay.is_paid = :isPaid
        AND (
            u.all_name LIKE '%' || :query || '%' OR
            CAST(u.uid AS TEXT) LIKE '%' || :query || '%'
        )
        ORDER BY u.all_name ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchUsersByGroupIdAndMonthAndPaymentStatus(
        groupId: Int,
        academicYear: String,
        query: String,
        month: Int,
        isPaid: Boolean,
        limit: Int,
        offset: Int
    ): List<User>


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
    @Update
    suspend fun updateUser(user: User)
    
    // --- Academic Year Payment Queries ---
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: AcademicYearPayment)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<AcademicYearPayment>)
    
    @Query("SELECT * FROM academic_year_payments WHERE user_id = :userId AND academic_year = :academicYear ORDER BY year, month")
    fun getPaymentsByUserAndAcademicYear(userId: Int, academicYear: String): Flow<List<AcademicYearPayment>>
    
    @Query("SELECT * FROM academic_year_payments WHERE user_id = :userId AND academic_year = :academicYear AND month = :month")
    suspend fun getPaymentByUserAndMonth(userId: Int, academicYear: String, month: Int): AcademicYearPayment?
    
    @Query("UPDATE academic_year_payments SET is_paid = :isPaid, payment_date = :paymentDate WHERE user_id = :userId AND academic_year = :academicYear AND month = :month")
    suspend fun updatePaymentStatus(userId: Int, academicYear: String, month: Int, isPaid: Boolean, paymentDate: String? = null)
    
    @Query("SELECT COUNT(*) > 0 FROM academic_year_payments WHERE user_id = :userId AND academic_year = :academicYear AND month = :month AND is_paid = 1")
    suspend fun isPaymentMade(userId: Int, academicYear: String, month: Int): Boolean
    
    @Query("SELECT COUNT(*) > 0 FROM academic_year_payments WHERE user_id = :userId AND academic_year = :academicYear")
    suspend fun hasPaymentsForAcademicYear(userId: Int, academicYear: String): Boolean
    
    @Query("DELETE FROM academic_year_payments WHERE user_id = :userId AND academic_year = :academicYear")
    suspend fun deletePaymentsForAcademicYear(userId: Int, academicYear: String)
    
    @Query("""
        SELECT COUNT(*) > 0 FROM academic_year_payments ay
        INNER JOIN user u ON ay.user_id = u.uid
        WHERE u.group_id = :groupId
        AND ay.academic_year = :academicYear
        AND ay.month = :month
        AND ay.is_paid = 1
        LIMIT 1
    """)
    suspend fun hasPaidUsersForMonthInAcademicYear(groupId: Int, academicYear: String, month: Int): Boolean
    
    @Query("SELECT * FROM academic_year_payments WHERE user_id = :userId AND academic_year = :academicYear")
    suspend fun getUserPayments(userId: Int, academicYear: String): List<AcademicYearPayment>
}
