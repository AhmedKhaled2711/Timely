package com.lee.timely.util

import java.text.SimpleDateFormat
import java.util.*

object AcademicYearUtils {
    
    /**
     * Gets the current academic year string in format "YYYY/YYYY+1"
     * Academic year in Egypt starts in August and ends in July
     */
    fun getCurrentAcademicYear(): String {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
        
        return if (currentMonth >= 8) { // August or later
            "$currentYear/${currentYear + 1}"
        } else { // January to July
            "${currentYear - 1}/$currentYear"
        }
    }
    
    /**
     * Gets academic year for a specific month and year
     */
    fun getAcademicYearForMonth(month: Int, year: Int): String {
        return if (month >= 8) { // August or later
            "$year/${year + 1}"
        } else { // January to July
            "${year - 1}/$year"
        }
    }
    
    /**
     * Gets all 12 months for an academic year with their proper years
     * Returns list of pairs: (month, year) for August to July
     */
    fun getAcademicYearMonths(academicYear: String): List<Pair<Int, Int>> {
        val years = academicYear.split("/").map { it.toInt() }
        val startYear = years[0]
        val endYear = years[1]
        
        return listOf(
            8 to startYear,  // August
            9 to startYear,  // September
            10 to startYear, // October
            11 to startYear, // November
            12 to startYear, // December
            1 to endYear,    // January
            2 to endYear,    // February
            3 to endYear,    // March
            4 to endYear,    // April
            5 to endYear,    // May
            6 to endYear,    // June
            7 to endYear     // July
        )
    }
    
    /**
     * Gets month display name with year for academic year display
     */
    fun getMonthYearDisplayName(month: Int, year: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month - 1) // Calendar.MONTH is 0-based
        calendar.set(Calendar.YEAR, year)
        
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    
    /**
     * Gets month short name with year
     */
    fun getMonthYearShortName(month: Int, year: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.YEAR, year)
        
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    
    /**
     * Gets all available academic years (current and previous ones)
     */
    fun getAvailableAcademicYears(count: Int = 5): List<String> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        
        val baseYear = if (currentMonth >= 8) currentYear else currentYear - 1
        
        return (0 until count).map { i ->
            val year = baseYear - i
            "$year/${year + 1}"
        }
    }
}
