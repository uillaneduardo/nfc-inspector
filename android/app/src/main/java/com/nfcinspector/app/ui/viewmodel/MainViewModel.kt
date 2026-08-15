package com.nfcinspector.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nfcinspector.app.data.model.NfcStatus
import com.nfcinspector.app.data.model.TagRecord
import com.nfcinspector.app.data.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repository: HistoryRepository) : ViewModel() {

    // Initial state is Checking until hardware adapter is confirmed
    private val _nfcStatus = MutableStateFlow<NfcStatus>(NfcStatus.Checking)
    val nfcStatus: StateFlow<NfcStatus> = _nfcStatus.asStateFlow()

    private val _currentTag = MutableStateFlow<TagRecord?>(null)
    val currentTag: StateFlow<TagRecord?> = _currentTag.asStateFlow()

    private val _isCurrentTagSaved = MutableStateFlow(false)
    val isCurrentTagSaved: StateFlow<Boolean> = _isCurrentTagSaved.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    val historyScans: StateFlow<List<TagRecord>> = repository.allScans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tag comparison state
    private val _compareTag1 = MutableStateFlow<TagRecord?>(null)
    val compareTag1: StateFlow<TagRecord?> = _compareTag1.asStateFlow()

    private val _compareTag2 = MutableStateFlow<TagRecord?>(null)
    val compareTag2: StateFlow<TagRecord?> = _compareTag2.asStateFlow()

    fun updateNfcStatus(status: NfcStatus) {
        _nfcStatus.value = status
    }

    fun onTagScanned(tagRecord: TagRecord) {
        _currentTag.value = tagRecord
        _isCurrentTagSaved.value = false
        _nfcStatus.value = NfcStatus.TagDetected(tagRecord)
        // Note: Automatic save removed. User chooses to save manually.
    }

    fun onScanError(errorMessage: String) {
        _nfcStatus.value = NfcStatus.ScanError(errorMessage)
    }

    fun resetToWaiting() {
        _currentTag.value = null
        _isCurrentTagSaved.value = false
        _nfcStatus.value = NfcStatus.ReadyWaiting
    }

    /**
     * Manual save with debounce protection and visual feedback.
     * Prevents accidental duplicates if user taps rapidly.
     */
    fun saveCurrentScanManually(onComplete: ((alreadySaved: Boolean) -> Unit)? = null) {
        val tag = _currentTag.value ?: return

        // Prevent rapid double-tap duplicate saves
        if (_isSaving.value) return
        if (_isCurrentTagSaved.value) {
            onComplete?.invoke(true)
            return
        }

        _isSaving.value = true
        viewModelScope.launch {
            try {
                repository.saveScan(tag)
                _isCurrentTagSaved.value = true
                onComplete?.invoke(false)
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun deleteScan(id: Long) {
        viewModelScope.launch {
            repository.deleteScan(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.deleteAllScans()
        }
    }

    fun selectForCompare(slot: Int, tag: TagRecord?) {
        if (slot == 1) {
            _compareTag1.value = tag
        } else {
            _compareTag2.value = tag
        }
    }
}

class MainViewModelFactory(private val repository: HistoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
