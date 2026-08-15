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

    private val _nfcStatus = MutableStateFlow<NfcStatus>(NfcStatus.ReadyWaiting)
    val nfcStatus: StateFlow<NfcStatus> = _nfcStatus.asStateFlow()

    private val _currentTag = MutableStateFlow<TagRecord?>(null)
    val currentTag: StateFlow<TagRecord?> = _currentTag.asStateFlow()

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
        _nfcStatus.value = NfcStatus.TagDetected(tagRecord)
        // Automatically save scan to offline local database
        viewModelScope.launch {
            repository.saveScan(tagRecord)
        }
    }

    fun onScanError(errorMessage: String) {
        _nfcStatus.value = NfcStatus.ScanError(errorMessage)
    }

    fun resetToWaiting() {
        _currentTag.value = null
        _nfcStatus.value = NfcStatus.ReadyWaiting
    }

    fun saveCurrentScanManually() {
        _currentTag.value?.let { tag ->
            viewModelScope.launch {
                repository.saveScan(tag)
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
