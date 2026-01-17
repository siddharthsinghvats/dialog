package com.dialog.app.data.model

/**
 * Enum representing different types of diabetes.
 * Used for profile classification and display.
 */
enum class DiabetesType(val displayName: String, val displayNameHi: String) {
    TYPE_1("Type 1", "टाइप 1"),
    TYPE_2("Type 2", "टाइप 2"),
    GESTATIONAL("Gestational", "गर्भावधि"),
    PREDIABETIC("Pre-diabetic", "प्री-डायबिटिक")
}
