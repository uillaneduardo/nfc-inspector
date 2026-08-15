package com.nfcinspector.app

import android.app.Application
import com.nfcinspector.app.data.local.AppDatabase
import com.nfcinspector.app.data.repository.HistoryRepository

class NfcInspectorApplication : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    val repository: HistoryRepository by lazy {
        HistoryRepository(database.tagDao())
    }

    override fun onCreate() {
        super.onCreate()
    }
}
