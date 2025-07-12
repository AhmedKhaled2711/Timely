package com.lee.timely.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lee.timely.model.GroupName
import com.lee.timely.model.GradeYear
import com.lee.timely.model.User
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

//@Database(entities = [User::class , GradeYear::class , GroupName::class], version = 6)
//abstract class TimelyDatabase : RoomDatabase() {
//
//    abstract fun getTimelyDao(): TimelyDao
//
//    companion object{
//        private var INSTANCE  : TimelyDatabase? = null
//
//        fun getInstance (context: Context) : TimelyDatabase{
//            return INSTANCE ?: synchronized(this){
//                val  instance = Room.databaseBuilder(
//                    context.applicationContext , TimelyDatabase::class.java , "timely_dataBase"
//
//                )
//                .fallbackToDestructiveMigration()
//                .build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//
//}

@Database(
    entities = [User::class, GradeYear::class, GroupName::class],
    version = 10,
    exportSchema = false
)
abstract class TimelyDatabase : RoomDatabase() {

    abstract fun getTimelyDao(): TimelyDao

    companion object {
        @Volatile
        private var INSTANCE: TimelyDatabase? = null

        fun getInstance(context: Context): TimelyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimelyDatabase::class.java,
                    "timely_database"
                )
                    .addCallback(DatabaseCallback())
                    .setQueryExecutor(Executors.newFixedThreadPool(4)) // Optimized thread pool
                    .setAutoCloseTimeout(5, TimeUnit.SECONDS)
                    .enableMultiInstanceInvalidation()
                    .fallbackToDestructiveMigration() // Consider proper migrations for production
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback()
}