package com.dialog.app.data.database

import androidx.room.*
import com.dialog.app.data.model.MeasurementLabel
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for MeasurementLabel operations.
 */
@Dao
interface MeasurementLabelDao {
    
    // ==================== QUERIES ====================
    
    /**
     * Get all active labels ordered by sort order.
     */
    @Query("SELECT * FROM measurement_labels WHERE isActive = 1 ORDER BY sortOrder ASC")
    fun getAllLabels(): Flow<List<MeasurementLabel>>
    
    /**
     * Get only default (non-custom) labels.
     */
    @Query("SELECT * FROM measurement_labels WHERE isCustom = 0 AND isActive = 1 ORDER BY sortOrder ASC")
    fun getDefaultLabels(): Flow<List<MeasurementLabel>>
    
    /**
     * Get only custom labels.
     */
    @Query("SELECT * FROM measurement_labels WHERE isCustom = 1 AND isActive = 1 ORDER BY sortOrder ASC")
    fun getCustomLabels(): Flow<List<MeasurementLabel>>
    
    /**
     * Get a label by ID.
     */
    @Query("SELECT * FROM measurement_labels WHERE id = :labelId")
    suspend fun getLabelById(labelId: Long): MeasurementLabel?
    
    /**
     * Get the next sort order value for new labels.
     */
    @Query("SELECT COALESCE(MAX(sortOrder), 0) + 1 FROM measurement_labels")
    suspend fun getNextSortOrder(): Int
    
    /**
     * Check if default labels exist.
     */
    @Query("SELECT COUNT(*) FROM measurement_labels WHERE isCustom = 0")
    suspend fun getDefaultLabelCount(): Int
    
    // ==================== INSERTS ====================
    
    /**
     * Insert a single label.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabel(label: MeasurementLabel): Long
    
    /**
     * Insert multiple labels.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabels(labels: List<MeasurementLabel>)
    
    // ==================== UPDATES ====================
    
    /**
     * Update a label.
     */
    @Update
    suspend fun updateLabel(label: MeasurementLabel)
    
    // ==================== DELETES ====================
    
    /**
     * Soft delete a label (mark as inactive).
     * We soft delete to preserve historical records.
     */
    @Query("UPDATE measurement_labels SET isActive = 0 WHERE id = :labelId")
    suspend fun deactivateLabel(labelId: Long)
    
    /**
     * Hard delete a custom label.
     */
    @Query("DELETE FROM measurement_labels WHERE id = :labelId AND isCustom = 1")
    suspend fun deleteCustomLabel(labelId: Long)
}
