package com.dialog.app.data.repository

import com.dialog.app.data.database.MeasurementLabelDao
import com.dialog.app.data.database.ProfileDao
import com.dialog.app.data.database.RecordDao
import com.dialog.app.data.model.GlucoseRecord
import com.dialog.app.data.model.GlucoseRecordWithLabel
import com.dialog.app.data.model.MeasurementLabel
import com.dialog.app.data.model.Profile
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Repository class that abstracts data operations.
 * Acts as a single source of truth for all data operations in the app.
 */
class DiaLogRepository(
    private val profileDao: ProfileDao,
    private val recordDao: RecordDao,
    private val labelDao: MeasurementLabelDao
) {
    
    // ==================== PROFILE OPERATIONS ====================
    
    /** Get all profiles as a Flow */
    fun getAllProfiles(): Flow<List<Profile>> = profileDao.getAllProfiles()
    
    /** Get the currently selected profile */
    fun getSelectedProfile(): Flow<Profile?> = profileDao.getSelectedProfile()
    
    /** Get a profile by ID */
    suspend fun getProfileById(profileId: Long): Profile? = profileDao.getProfileById(profileId)
    
    /** Create a new profile */
    suspend fun createProfile(profile: Profile): Long {
        val id = profileDao.insertProfile(profile)
        // If this is the first profile, auto-select it
        if (profileDao.getProfileCount() == 1) {
            profileDao.selectProfile(id)
        }
        return id
    }
    
    /** Update a profile */
    suspend fun updateProfile(profile: Profile) = profileDao.updateProfile(profile)
    
    /** Delete a profile */
    suspend fun deleteProfile(profileId: Long) = profileDao.deleteProfile(profileId)
    
    /** Select a profile (sets it as active) */
    suspend fun selectProfile(profileId: Long) = profileDao.selectProfile(profileId)
    
    /** Check if any profiles exist */
    suspend fun hasProfiles(): Boolean = profileDao.getProfileCount() > 0
    
    // ==================== RECORD OPERATIONS ====================
    
    /** Get all records for a profile with labels */
    fun getRecordsForProfile(profileId: Long): Flow<List<GlucoseRecordWithLabel>> =
        recordDao.getRecordsWithLabels(profileId)
    
    /** Get records filtered by date range */
    fun getRecordsByDateRange(
        profileId: Long,
        startTime: Long,
        endTime: Long
    ): Flow<List<GlucoseRecordWithLabel>> =
        recordDao.getRecordsByDateRange(profileId, startTime, endTime)
    
    /** Get records filtered by label */
    fun getRecordsByLabel(profileId: Long, labelId: Long): Flow<List<GlucoseRecordWithLabel>> =
        recordDao.getRecordsByLabel(profileId, labelId)
    
    /** Get records filtered by glucose range */
    fun getRecordsByGlucoseRange(
        profileId: Long,
        minLevel: Int,
        maxLevel: Int
    ): Flow<List<GlucoseRecordWithLabel>> =
        recordDao.getRecordsByGlucoseRange(profileId, minLevel, maxLevel)
    
    /** Get the latest record for a profile */
    fun getLatestRecord(profileId: Long): Flow<GlucoseRecordWithLabel?> =
        recordDao.getLatestRecord(profileId)

    /** Get the latest N records for a profile */
    fun getRecentRecords(profileId: Long, limit: Int): Flow<List<GlucoseRecordWithLabel>> =
        recordDao.getRecentRecords(profileId, limit)
    
    /** Get a specific record */
    suspend fun getRecordById(recordId: Long): GlucoseRecordWithLabel? =
        recordDao.getRecordById(recordId)
    
    /** Get today's reading count */
    fun getTodayRecordCount(profileId: Long): Flow<Int> {
        val startOfDay = getStartOfDay()
        return recordDao.getTodayRecordCount(profileId, startOfDay)
    }
    
    /** Get record count for a profile */
    fun getRecordCount(profileId: Long): Flow<Int> =
        recordDao.getRecordCount(profileId)
    
    /** Get average glucose for today */
    suspend fun getTodayAverageGlucose(profileId: Long): Double? {
        val startOfDay = getStartOfDay()
        val endOfDay = getEndOfDay()
        return recordDao.getAverageGlucose(profileId, startOfDay, endOfDay)
    }
    
    /** Get average glucose for this week */
    suspend fun getWeekAverageGlucose(profileId: Long): Double? {
        val startOfWeek = getStartOfWeek()
        val now = System.currentTimeMillis()
        return recordDao.getAverageGlucose(profileId, startOfWeek, now)
    }
    
    /** Get average glucose for date range */
    suspend fun getAverageGlucose(profileId: Long, startTime: Long, endTime: Long): Double? =
        recordDao.getAverageGlucose(profileId, startTime, endTime)
    
    /** Get max glucose for date range */
    suspend fun getMaxGlucose(profileId: Long, startTime: Long, endTime: Long): Int? =
        recordDao.getMaxGlucose(profileId, startTime, endTime)
    
    /** Get min glucose for date range */
    suspend fun getMinGlucose(profileId: Long, startTime: Long, endTime: Long): Int? =
        recordDao.getMinGlucose(profileId, startTime, endTime)
    
    /** Add a new glucose record */
    suspend fun addRecord(record: GlucoseRecord): Long =
        recordDao.insertRecord(record)
    
    /** Update a record */
    suspend fun updateRecord(record: GlucoseRecord) =
        recordDao.updateRecord(record)
    
    /** Delete a record */
    suspend fun deleteRecord(recordId: Long) =
        recordDao.deleteRecord(recordId)
    
    // ==================== LABEL OPERATIONS ====================
    
    /** Get all active measurement labels */
    fun getAllLabels(): Flow<List<MeasurementLabel>> = labelDao.getAllLabels()
    
    /** Get only custom labels */
    fun getCustomLabels(): Flow<List<MeasurementLabel>> = labelDao.getCustomLabels()
    
    /** Get a label by ID */
    suspend fun getLabelById(labelId: Long): MeasurementLabel? = labelDao.getLabelById(labelId)
    
    /** Create a custom label */
    suspend fun createCustomLabel(nameEn: String, nameHi: String? = null): Long {
        val sortOrder = labelDao.getNextSortOrder()
        val label = MeasurementLabel(
            nameEn = nameEn,
            nameHi = nameHi,
            isCustom = true,
            sortOrder = sortOrder
        )
        return labelDao.insertLabel(label)
    }
    
    /** Update a label */
    suspend fun updateLabel(label: MeasurementLabel) = labelDao.updateLabel(label)
    
    /** Delete a custom label (deactivates it) */
    suspend fun deleteCustomLabel(labelId: Long) = labelDao.deactivateLabel(labelId)
    
    /** Ensure default labels exist */
    suspend fun ensureDefaultLabels() {
        if (labelDao.getDefaultLabelCount() == 0) {
            labelDao.insertLabels(MeasurementLabel.getDefaultLabels())
        }
    }
    
    // ==================== HELPER FUNCTIONS ====================
    
    fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    fun getEndOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
    
    fun getStartOfWeek(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    fun getStartOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
