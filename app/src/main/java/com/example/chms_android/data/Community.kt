package com.example.chms_android.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(tableName = "community")
data class Community(
    @PrimaryKey(autoGenerate = false)
    val communityId: Int, // 对应 community_id
    val name: String, // 对应 name
    val address: String, // 对应 address
    val city: String, // 对应 city
    val state: String, // 对应 state
    val postalCode: String, // 对应 postal_code
    val contactNumber: String, // 对应 contact_number
    val email: String, // 对应 email
    val population: Int, // 对应 population
    val registerPopulation: Int, // 对应 register_population
    val createdAt: Date, // 对应 created_at
    val updatedAt: Date // 对应 updated_at
): Parcelable