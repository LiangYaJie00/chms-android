package com.example.chms_android.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.chms_android.data.Appointment
import com.example.chms_android.data.AppointmentStatus
import com.example.chms_android.data.AppointmentType
import java.util.Date
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAppointment(appointment: Appointment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAppointments(appointments: List<Appointment>)

    @Update
    fun updateAppointment(appointment: Appointment)

    @Query("SELECT * FROM appointment WHERE appointmentId = :appointmentId")
    fun getAppointmentById(appointmentId: Int): Appointment?

    @Query("SELECT * FROM appointment")
    fun getAllAppointments(): List<Appointment>

    @Query("SELECT * FROM appointment WHERE doctorId = :doctorId")
    fun getAppointmentsByDoctorId(doctorId: Int): List<Appointment>

    @Query("SELECT * FROM appointment WHERE patientId = :patientId")
    fun getAppointmentsByPatientId(patientId: Int): List<Appointment>

    @Query("SELECT * FROM appointment WHERE status = :status")
    fun getAppointmentsByStatus(status: AppointmentStatus): List<Appointment>

    @Query("SELECT * FROM appointment WHERE doctorId = :doctorId AND status = :status")
    fun getAppointmentsByDoctorIdAndStatus(doctorId: Int, status: AppointmentStatus): List<Appointment>

    @Query("SELECT * FROM appointment WHERE patientId = :patientId AND status = :status")
    fun getAppointmentsByPatientIdAndStatus(patientId: Int, status: AppointmentStatus): List<Appointment>

    @Query("SELECT * FROM appointment WHERE appointmentDate = :date")
    fun getAppointmentsByDate(date: Date): List<Appointment>

    @Query("SELECT * FROM appointment WHERE appointmentDate = :date AND doctorId = :doctorId")
    fun getAppointmentsByDateAndDoctorId(date: Date, doctorId: Int): List<Appointment>

    @Query("SELECT * FROM appointment WHERE appointmentDate = :date AND patientId = :patientId")
    fun getAppointmentsByDateAndPatientId(date: Date, patientId: Int): List<Appointment>

    @Query("SELECT * FROM appointment WHERE appointmentType = :type AND appointmentDate = :date AND status = :status")
    fun getAppointmentsByTypeAndDateAndStatus(type: AppointmentType, date: Date, status: AppointmentStatus): List<Appointment>

    @Query("UPDATE appointment SET notificationSent = :sent WHERE appointmentId = :appointmentId")
    fun updateNotificationStatus(appointmentId: Int, sent: Boolean): Int

    @Query("UPDATE appointment SET status = :status WHERE appointmentId = :appointmentId")
    fun updateAppointmentStatus(appointmentId: Int, status: AppointmentStatus): Int

    @Query("UPDATE appointment SET notes = :notes WHERE appointmentId = :appointmentId")
    fun updateAppointmentNotes(appointmentId: Int, notes: String?): Int

    @Query("DELETE FROM appointment WHERE appointmentId = :appointmentId")
    fun deleteAppointment(appointmentId: Int): Int

    /**
     * 查询指定医生和患者之间的所有预约
     */
    @Query("SELECT * FROM appointment WHERE doctorId = :doctorId AND patientId = :patientId ORDER BY appointmentDate DESC, startTime DESC")
    fun getAppointmentsByDoctorAndPatientId(doctorId: Int, patientId: Int): List<Appointment>
}
