package com.dialog.app.data.database

import androidx.room.TypeConverter
import com.dialog.app.data.model.DiabetesType

/**
 * Room type converters for custom types.
 */
class Converters {
    
    @TypeConverter
    fun fromDiabetesType(type: DiabetesType): String = type.name
    
    @TypeConverter
    fun toDiabetesType(value: String): DiabetesType = DiabetesType.valueOf(value)
}
