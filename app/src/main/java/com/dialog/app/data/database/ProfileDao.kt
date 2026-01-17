package com.dialog.app.data.database

import androidx.room.*
import com.dialog.app.data.model.Profile
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Profile operations.
 * Provides reactive Flow-based queries for UI updates.
 */
@Dao
interface ProfileDao {
    
    // ==================== QUERIES ====================
    
    /**
     * Get all profiles ordered by creation date.
     * Returns a Flow for reactive updates.
     */
    @Query("SELECT * FROM profiles ORDER BY createdAt DESC")
    fun getAllProfiles(): Flow<List<Profile>>
    
    /**
     * Get the currently selected profile.
     */
    @Query("SELECT * FROM profiles WHERE isSelected = 1 LIMIT 1")
    fun getSelectedProfile(): Flow<Profile?>
    
    /**
     * Get a profile by ID.
     */
    @Query("SELECT * FROM profiles WHERE id = :profileId")
    suspend fun getProfileById(profileId: Long): Profile?
    
    /**
     * Get profile count for checking if this is the first profile.
     */
    @Query("SELECT COUNT(*) FROM profiles")
    suspend fun getProfileCount(): Int
    
    // ==================== INSERTS ====================
    
    /**
     * Insert a new profile and return its ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile): Long
    
    // ==================== UPDATES ====================
    
    /**
     * Update an existing profile.
     */
    @Update
    suspend fun updateProfile(profile: Profile)
    
    /**
     * Select a profile (deselects all others first).
     */
    @Transaction
    suspend fun selectProfile(profileId: Long) {
        deselectAllProfiles()
        setProfileSelected(profileId, true)
    }
    
    @Query("UPDATE profiles SET isSelected = 0")
    suspend fun deselectAllProfiles()
    
    @Query("UPDATE profiles SET isSelected = :isSelected WHERE id = :profileId")
    suspend fun setProfileSelected(profileId: Long, isSelected: Boolean)
    
    // ==================== DELETES ====================
    
    /**
     * Delete a profile by ID.
     * Related glucose records are cascade-deleted automatically.
     */
    @Query("DELETE FROM profiles WHERE id = :profileId")
    suspend fun deleteProfile(profileId: Long)
    
    @Delete
    suspend fun deleteProfile(profile: Profile)
}
