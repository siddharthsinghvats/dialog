package com.dialog.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Profile entity representing a user profile in the app.
 * Each profile can have their own glucose records and settings.
 * 
 * Supports customizable target glucose ranges for personalized risk assessment.
 */
@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Display name for the profile */
    val name: String,
    
    /** Age of the person (optional) */
    val age: Int? = null,
    
    /** Type of diabetes */
    val diabetesType: DiabetesType = DiabetesType.TYPE_2,
    
    /** Avatar color as hex string (e.g., "#0D9488") */
    val avatarColor: String = "#0D9488",
    
    // Customizable glucose thresholds (mg/dL)
    /** Minimum normal glucose level */
    val targetGlucoseMin: Int = 70,
    
    /** Maximum normal glucose level */
    val targetGlucoseMax: Int = 140,
    
    /** Maximum borderline glucose level (above this is HIGH) */
    val borderlineMax: Int = 180,
    
    /** Timestamp when profile was created */
    val createdAt: Long = System.currentTimeMillis(),
    
    /** Whether this is the currently selected profile */
    val isSelected: Boolean = false
) {
    /**
     * Determines the risk level for a given glucose reading.
     * Uses the profile's custom thresholds.
     */
    fun getRiskLevel(glucoseLevel: Int): RiskLevel {
        return when {
            glucoseLevel < targetGlucoseMin -> RiskLevel.LOW
            glucoseLevel in targetGlucoseMin..targetGlucoseMax -> RiskLevel.NORMAL
            glucoseLevel in (targetGlucoseMax + 1)..borderlineMax -> RiskLevel.BORDERLINE
            else -> RiskLevel.HIGH
        }
    }
}

/**
 * Risk level for glucose readings.
 * Each level has associated colors for UI display.
 */
enum class RiskLevel(val displayName: String, val displayNameHi: String) {
    LOW("Low", "कम"),
    NORMAL("Normal", "सामान्य"),
    BORDERLINE("Borderline", "सीमारेखा"),
    HIGH("High", "अधिक")
}
