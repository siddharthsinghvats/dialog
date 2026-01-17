package com.dialog.app.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Entity representing a glucose reading/record.
 * Each record belongs to a profile and has an associated measurement label.
 */
@Entity(
    tableName = "glucose_records",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MeasurementLabel::class,
            parentColumns = ["id"],
            childColumns = ["labelId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["labelId"]),
        Index(value = ["measuredAt"])
    ]
)
data class GlucoseRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Foreign key to Profile */
    val profileId: Long,
    
    /** Glucose level in mg/dL */
    val glucoseLevel: Int,
    
    /** Foreign key to MeasurementLabel (nullable if label is deleted) */
    val labelId: Long? = null,
    
    /** Optional notes from the user */
    val notes: String? = null,
    
    /** Timestamp when the reading was taken (user-selected time) */
    val measuredAt: Long,
    
    /** Timestamp when the record was created in the app */
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Data class that combines a GlucoseRecord with its associated MeasurementLabel.
 * Used for displaying records in the UI with label information.
 */
data class GlucoseRecordWithLabel(
    @Embedded val record: GlucoseRecord,
    @Relation(
        parentColumn = "labelId",
        entityColumn = "id"
    )
    val label: MeasurementLabel?
)
