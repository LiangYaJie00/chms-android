package com.example.chms_android.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Patient(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val gender: String,
    val age: Int,
    val address: String,
    val medicalHistory: String = "",
    val avatarUrl: String? = null
) : Parcelable