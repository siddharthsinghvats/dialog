package com.dialog.app

import android.app.Application
import com.dialog.app.data.database.AppDatabase
import com.dialog.app.data.repository.DiaLogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application class for DiaLog.
 * Initializes the database and repository as singletons.
 */
class DiaLogApp : Application() {
    
    /** Application-wide coroutine scope */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    /** Lazy-initialized database */
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }
    
    /** Lazy-initialized repository */
    val repository: DiaLogRepository by lazy {
        DiaLogRepository(
            profileDao = database.profileDao(),
            recordDao = database.recordDao(),
            labelDao = database.measurementLabelDao()
        )
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Ensure default labels are populated
        applicationScope.launch {
            repository.ensureDefaultLabels()
        }
    }
}
