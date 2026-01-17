package com.dialog.app.data.database

import androidx.room.*
import com.dialog.app.data.model.GlucoseRecord
import com.dialog.app.data.model.GlucoseRecordWithLabel
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for GlucoseRecord operations.
 * Supports filtering and provides reactive queries.
 */
@Dao
interface RecordDao {
    
    // ==================== QUERIES ====================
    
    /**
     * Get all records for a profile with their labels, ordered by measurement time.
     */
    @Transaction
    @Query("""
        SELECT * FROM glucose_records 
        WHERE profileId = :profileId 
        ORDER BY measuredAt DESC
    """)
    fun getRecordsWithLabels(profileId: Long): Flow<List<GlucoseRecordWithLabel>>
    
    /**
     * Get records filtered by date range.
     */
    @Transaction
    @Query("""
        SELECT * FROM glucose_records 
        WHERE profileId = :profileId 
        AND measuredAt >= :startTime 
        AND measuredAt <= :endTime 
        ORDER BY measuredAt DESC
    """)
    fun getRecordsByDateRange(
        profileId: Long,
        startTime: Long,
        endTime: Long
    ): Flow<List<GlucoseRecordWithLabel>>
    
    /**
     * Get records filtered by label.
     */
    @Transaction
    @Query("""
        SELECT * FROM glucose_records 
        WHERE profileId = :profileId 
        AND labelId = :labelId 
        ORDER BY measuredAt DESC
    """)
    fun getRecordsByLabel(profileId: Long, labelId: Long): Flow<List<GlucoseRecordWithLabel>>
    
    /**
     * Get records filtered by glucose level range (for risk level filtering).
     */
    @Transaction
    @Query("""
        SELECT * FROM glucose_records 
        WHERE profileId = :profileId 
        AND glucoseLevel >= :minLevel 
        AND glucoseLevel <= :maxLevel 
        ORDER BY measuredAt DESC
    """)
    fun getRecordsByGlucoseRange(
        profileId: Long,
        minLevel: Int,
        maxLevel: Int
    ): Flow<List<GlucoseRecordWithLabel>>
    
    /**
     * Get the latest record for a profile.
     */
    @Transaction
    @Query("""
        SELECT * FROM glucose_records 
        WHERE profileId = :profileId 
        ORDER BY measuredAt DESC 
        LIMIT 1
    """)
    fun getLatestRecord(profileId: Long): Flow<GlucoseRecordWithLabel?>
    
    /**
     * Get the latest N records for a profile.
     */
    @Transaction
    @Query("""
        SELECT * FROM glucose_records 
        WHERE profileId = :profileId 
        ORDER BY measuredAt DESC
        LIMIT :limit
    """)
    fun getRecentRecords(profileId: Long, limit: Int): Flow<List<GlucoseRecordWithLabel>>
    
    /**
     * Get a single record by ID.
     */
    @Transaction
    @Query("SELECT * FROM glucose_records WHERE id = :recordId")
    suspend fun getRecordById(recordId: Long): GlucoseRecordWithLabel?
    
    /**
     * Get today's record count for a profile.
     */
    @Query("""
        SELECT COUNT(*) FROM glucose_records 
        WHERE profileId = :profileId 
        AND measuredAt >= :startOfDay
    """)
    fun getTodayRecordCount(profileId: Long, startOfDay: Long): Flow<Int>
    
    /**
     * Get average glucose for a profile within a date range.
     */
    @Query("""
        SELECT AVG(glucoseLevel) FROM glucose_records 
        WHERE profileId = :profileId 
        AND measuredAt >= :startTime 
        AND measuredAt <= :endTime
    """)
    suspend fun getAverageGlucose(profileId: Long, startTime: Long, endTime: Long): Double?
    
    /**
     * Get total record count for a profile.
     */
    @Query("SELECT COUNT(*) FROM glucose_records WHERE profileId = :profileId")
    fun getRecordCount(profileId: Long): Flow<Int>
    
    // ==================== INSERTS ====================
    
    /**
     * Insert a new glucose record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GlucoseRecord): Long
    
    // ==================== UPDATES ====================
    
    /**
     * Update an existing record.
     */
    @Update
    suspend fun updateRecord(record: GlucoseRecord)
    
    // ==================== DELETES ====================
    
    /**
     * Delete a record by ID.
     */
    @Query("DELETE FROM glucose_records WHERE id = :recordId")
    suspend fun deleteRecord(recordId: Long)
    
    /**
     * Delete all records for a profile.
     */
    @Query("DELETE FROM glucose_records WHERE profileId = :profileId")
    suspend fun deleteAllRecordsForProfile(profileId: Long)
}
