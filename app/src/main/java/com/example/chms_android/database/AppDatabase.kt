package com.example.chms_android.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.chms_android.common.Constants
import com.example.chms_android.dao.CommunityDao
import com.example.chms_android.dao.DailyHealthReportDao
import com.example.chms_android.dao.DoctorAdviceDao
import com.example.chms_android.dao.DoctorDao
import com.example.chms_android.dao.HealthInfoDao
import com.example.chms_android.dao.ReportAnalysisDao
import com.example.chms_android.dao.UserDao
import com.example.chms_android.data.Community
import com.example.chms_android.data.DailyHealthReport
import com.example.chms_android.data.Doctor
import com.example.chms_android.data.DoctorAdvice
import com.example.chms_android.data.HealthInfo
import com.example.chms_android.data.ReportAnalysis
import com.example.chms_android.data.User
import com.example.chms_android.data.converter.BigDecimalConverter
import com.example.chms_android.data.converter.DateConverter
import com.example.chms_android.data.converter.LocalDateTimeConverter
import com.example.chms_android.data.converter.TimestampConverter

@Database(entities = [User::class,
    Doctor::class,
    Community::class,
    HealthInfo::class,
    DailyHealthReport::class,
    ReportAnalysis::class,
    DoctorAdvice::class],
    version = Constants.DATABASE_VERSION, exportSchema = false)
@TypeConverters(LocalDateTimeConverter::class, TimestampConverter::class, BigDecimalConverter::class, DateConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun doctorDao(): DoctorDao
    abstract fun communityDao(): CommunityDao
    abstract fun healthInfoDao(): HealthInfoDao
    abstract fun dailyHealthReportDao(): DailyHealthReportDao
    abstract fun reportAnalysisDao(): ReportAnalysisDao
    abstract fun doctorAdviceDao(): DoctorAdviceDao
}