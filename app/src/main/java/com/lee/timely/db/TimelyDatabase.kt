package com.lee.timely.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lee.timely.model.GroupName
import com.lee.timely.model.GradeYear
import com.lee.timely.model.User

@Database(entities = [User::class , GradeYear::class , GroupName::class], version = 6)
abstract class TimelyDatabase : RoomDatabase() {

    abstract fun getTimelyDao(): TimelyDao

    companion object{
        private var INSTANCE  : TimelyDatabase? = null

        fun getInstance (context: Context) : TimelyDatabase{
            return INSTANCE ?: synchronized(this){
                val  instance = Room.databaseBuilder(
                    context.applicationContext , TimelyDatabase::class.java , "timely_dataBase"

                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

}