package com.lee.timely.model

import androidx.room.*

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name") val lastName: String,
    @ColumnInfo(name = "group_id") val groupId: Int,
    @ColumnInfo(name = "flag1") val flag1: Boolean = false,
    @ColumnInfo(name = "flag2") val flag2: Boolean = false,
    @ColumnInfo(name = "flag3") val flag3: Boolean = false,
    @ColumnInfo(name = "flag4") val flag4: Boolean = false,
    @ColumnInfo(name = "flag5") val flag5: Boolean = false,
    @ColumnInfo(name = "flag6") val flag6: Boolean = false,
    @ColumnInfo(name = "flag7") val flag7: Boolean = false,
    @ColumnInfo(name = "flag8") val flag8: Boolean = false,
    @ColumnInfo(name = "flag9") val flag9: Boolean = false,
    @ColumnInfo(name = "flag10") val flag10: Boolean = false,
    @ColumnInfo(name = "flag11") val flag11: Boolean = false,
    @ColumnInfo(name = "flag12") val flag12: Boolean = false,
    @ColumnInfo(name = "guardians_number") val guardiansNumber: String? = null,
    @ColumnInfo(name = "start_date") val startDate: String,
    @ColumnInfo(name = "student_number") val studentNumber: String? = null
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
