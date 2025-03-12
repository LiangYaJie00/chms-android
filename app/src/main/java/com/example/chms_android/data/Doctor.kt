package com.example.chms_android.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(tableName = "doctor")
data class Doctor(
    @PrimaryKey(autoGenerate = false)
    var doctorId: Int,
    var name: String,
    var gender: Int,
    var dateOfBirth: Date,
    var phoneNumber: String,
    var email: String,
    var specialization: String,
    var communityName: String,
    var address: String,
    var createdAt: Date,
    var updatedAt: Date
): Parcelable