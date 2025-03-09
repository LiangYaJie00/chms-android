package com.example.chms_android.database

import android.content.Context
import androidx.room.Room
import com.example.chms_android.common.Constants

object DatabaseProvider {
    private var INSTANCE: AppDatabase? = null

    fun initDatabase(context: Context) {
        synchronized(this) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    Constants.DATABASE_NAME
                ).build()
            }
        }
    }

    fun getDatabase(): AppDatabase {
        return INSTANCE ?: throw IllegalStateException("Database must be initialized")
    }

//    fun getDatabase(context: Context): AppDatabase {
//        return INSTANCE ?: synchronized(this) {
//            val instance = Room.databaseBuilder(
//                context.applicationContext,
//                AppDatabase::class.java,
//                Constants.DATABASE_NAME
//            ).build()
//            INSTANCE = instance
//            instance
//        }
//    }

}