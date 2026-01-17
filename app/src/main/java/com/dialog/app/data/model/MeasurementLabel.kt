package com.dialog.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a measurement label/context.
 * Pre-populated with common labels and supports user-created custom labels.
 * 
 * Examples: "Fasting", "After Lunch (1h)", "Bedtime", etc.
 */
@Entity(tableName = "measurement_labels")
data class MeasurementLabel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** English name of the label */
    val nameEn: String,
    
    /** Hindi name of the label (optional) */
    val nameHi: String? = null,
    
    /** Whether this is a user-created custom label */
    val isCustom: Boolean = false,
    
    /** Sort order for display */
    val sortOrder: Int = 0,
    
    /** Whether label is visible/active */
    val isActive: Boolean = true
) {
    companion object {
        /**
         * Returns the default pre-populated measurement labels.
         * These are inserted on first app launch.
         */
        fun getDefaultLabels(): List<MeasurementLabel> = listOf(
            MeasurementLabel(
                id = 1,
                nameEn = "Fasting",
                nameHi = "खाली पेट",
                sortOrder = 1
            ),
            MeasurementLabel(
                id = 2,
                nameEn = "Before Breakfast",
                nameHi = "नाश्ते से पहले",
                sortOrder = 2
            ),
            MeasurementLabel(
                id = 3,
                nameEn = "After Breakfast (1h)",
                nameHi = "नाश्ते के 1 घंटे बाद",
                sortOrder = 3
            ),
            MeasurementLabel(
                id = 4,
                nameEn = "After Breakfast (2h)",
                nameHi = "नाश्ते के 2 घंटे बाद",
                sortOrder = 4
            ),
            MeasurementLabel(
                id = 5,
                nameEn = "Before Lunch",
                nameHi = "दोपहर के भोजन से पहले",
                sortOrder = 5
            ),
            MeasurementLabel(
                id = 6,
                nameEn = "After Lunch (1h)",
                nameHi = "दोपहर के भोजन के 1 घंटे बाद",
                sortOrder = 6
            ),
            MeasurementLabel(
                id = 7,
                nameEn = "After Lunch (2h)",
                nameHi = "दोपहर के भोजन के 2 घंटे बाद",
                sortOrder = 7
            ),
            MeasurementLabel(
                id = 8,
                nameEn = "Before Dinner",
                nameHi = "रात के खाने से पहले",
                sortOrder = 8
            ),
            MeasurementLabel(
                id = 9,
                nameEn = "After Dinner (1h)",
                nameHi = "रात के खाने के 1 घंटे बाद",
                sortOrder = 9
            ),
            MeasurementLabel(
                id = 10,
                nameEn = "After Dinner (2h)",
                nameHi = "रात के खाने के 2 घंटे बाद",
                sortOrder = 10
            ),
            MeasurementLabel(
                id = 11,
                nameEn = "Bedtime",
                nameHi = "सोने से पहले",
                sortOrder = 11
            ),
            MeasurementLabel(
                id = 12,
                nameEn = "Random",
                nameHi = "कभी भी",
                sortOrder = 12
            )
        )
    }
}
