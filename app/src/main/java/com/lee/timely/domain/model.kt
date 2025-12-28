package com.lee.timely.domain

import androidx.room.*

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "all_name") val allName: String,
    @ColumnInfo(name = "group_id") val groupId: Int,
    @ColumnInfo(name = "guardians_number") val guardiansNumber: String? = null,
    @ColumnInfo(name = "start_date") val startDate: String,
    @ColumnInfo(name = "student_number") val studentNumber: String? = null
)

@Entity(
    tableName = "academic_year_payments",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["uid"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["user_id", "academic_year", "month"], unique = true)]
)
data class AcademicYearPayment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "academic_year") val academicYear: String, // e.g., "2024/2025"
    @ColumnInfo(name = "month") val month: Int, // 1-12 (January-December)
    @ColumnInfo(name = "year") val year: Int, // e.g., 2024, 2025
    @ColumnInfo(name = "is_paid") val isPaid: Boolean = false,
    @ColumnInfo(name = "payment_date") val paymentDate: String? = null
)

@Entity(tableName = "school_year")
data class GradeYear(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val year: String
)

@Entity(
    tableName = "group_name",
    foreignKeys = [ForeignKey(
        entity = GradeYear::class,
        parentColumns = ["id"],
        childColumns = ["schoolYearId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["schoolYearId"])]
)
data class GroupName(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val groupName: String,
    val schoolYearId: Int
)
