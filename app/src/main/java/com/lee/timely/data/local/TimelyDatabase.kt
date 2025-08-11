package com.lee.timely.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.ExperimentalRoomApi
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lee.timely.model.GroupName
import com.lee.timely.model.GradeYear
import com.lee.timely.model.User
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private const val TAG = "TimelyDatabase"
private const val DATABASE_NAME = "timely_database"
private const val DATABASE_VERSION = 11

@Database(
    entities = [User::class, GradeYear::class, GroupName::class],
    version = DATABASE_VERSION,
    exportSchema = false
)
abstract class TimelyDatabase : RoomDatabase() {

    abstract fun getTimelyDao(): TimelyDao

    companion object {
        @Volatile
        private var INSTANCE: TimelyDatabase? = null
        private val LOCK = Any()

        @Synchronized
        fun getInstance(context: Context): TimelyDatabase {
            // Double-check locking pattern
            return INSTANCE ?: synchronized(LOCK) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { 
                    INSTANCE = it 
                    Log.d(TAG, "Database instance created")
                }
            }
        }

        @OptIn(ExperimentalRoomApi::class)
        private fun buildDatabase(context: Context): TimelyDatabase {
            Log.d(TAG, "Building database...")
            return try {
                Room.databaseBuilder(
                    context,
                    TimelyDatabase::class.java,
                    DATABASE_NAME
                ).apply {
                    addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d(TAG, "Database created successfully")
                            // You can add initial data here if needed
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.d(TAG, "Database opened")
                        }
                    })
                    setQueryExecutor(Executors.newFixedThreadPool(4))
                    setAutoCloseTimeout(5, TimeUnit.SECONDS)
                    fallbackToDestructiveMigration()
                }.build().also {
                    Log.d(TAG, "Database built successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to build database", e)
                throw IllegalStateException("Failed to initialize database", e)
            }
        }
    }
}