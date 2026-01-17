package com.dialog.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dialog.app.data.model.GlucoseRecord
import com.dialog.app.data.model.MeasurementLabel
import com.dialog.app.data.model.Profile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Room database for the DiaLog app.
 * 
 * Entities:
 * - Profile: User profiles with custom glucose thresholds
 * - GlucoseRecord: Glucose readings with timestamps and labels
 * - MeasurementLabel: Pre-defined and custom measurement context labels
 */
@Database(
    entities = [
        Profile::class,
        GlucoseRecord::class,
        MeasurementLabel::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun profileDao(): ProfileDao
    abstract fun recordDao(): RecordDao
    abstract fun measurementLabelDao(): MeasurementLabelDao
    
    companion object {
        private const val DATABASE_NAME = "dialog_database"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Get the singleton database instance.
         * Pre-populates default measurement labels on first creation.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Callback to pre-populate default measurement labels on database creation.
         */
        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                
                // Pre-populate default labels
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultLabels(database.measurementLabelDao())
                    }
                }
            }
            
            private suspend fun populateDefaultLabels(dao: MeasurementLabelDao) {
                val defaultLabels = MeasurementLabel.getDefaultLabels()
                dao.insertLabels(defaultLabels)
            }
        }
    }
}
