package com.example.chms_android.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.sql.Timestamp
import java.util.Date
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "doctorAvailableTime")
data class DoctorAvailableTime(
    @PrimaryKey(autoGenerate = false)
    var timeId: Int = 0,
    var doctorId: Int? = null,
    var availableDate: Date? = null,
    var startTime: Date? = null,
    var endTime: Date? = null,
    var isBooked: Boolean = false,
    var createTime: Timestamp? = null,
    var updateTime: Timestamp? = null
) : Parcelable {
    // 默认构造函数，这是Room将使用的构造函数
    @Ignore
    constructor() : this(
        0, null, null, null, null, false, null, null
    )

    // 使用@Ignore注解标记其他构造函数
    @Ignore
    constructor(
        timeId: Int,
        doctorId: Int?,
        availableDate: Date?,
        startTime: Date?,
        endTime: Date?
    ) : this(
        timeId, doctorId, availableDate, startTime, endTime, false, null, null
    )
}
