package com.example.chms_android.database

import android.content.Context
import androidx.room.Room
import com.example.chms_android.common.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseProvider {
    private var INSTANCE: AppDatabase? = null

    fun initDatabase(context: Context) {
        synchronized(this) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    Constants.DATABASE_NAME
                )
                .allowMainThreadQueries() // 仅用于开发阶段，生产环境应移除
                .build()
            }
        }
    }

    fun getDatabase(): AppDatabase {
        return INSTANCE ?: throw IllegalStateException("Database must be initialized")
    }

    // 添加一个安全的获取数据库方法，如果数据库未初始化，则初始化它
    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                Constants.DATABASE_NAME
            )
            .allowMainThreadQueries() // 仅用于开发阶段，生产环境应移除
            .build()
            INSTANCE = instance
            instance
        }
    }
    
    /**
     * 在后台线程中执行数据库操作
     * @param block 要执行的数据库操作
     * @return 操作结果
     */
    suspend fun <T> executeInBackground(block: suspend (AppDatabase) -> T): T {
        return withContext(Dispatchers.IO) {
            val database = getDatabase()
            block(database)
        }
    }
}