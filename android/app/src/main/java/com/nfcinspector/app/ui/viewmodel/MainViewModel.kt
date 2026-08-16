package com.nfcinspector.app.ui.viewmodel

import android.nfc.Tag
import android.nfc.TagLostException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nfcinspector.app.data.model.NfcStatus
import com.nfcinspector.app.data.model.TagRecord
import com.nfcinspector.app.data.repository.HistoryRepository
import com.nfcinspector.app.nfc.mifare.MifareClassicInspector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MainViewModel(private val repository: HistoryRepository) : ViewModel() {

    // Initial state is Checking until hardware adapter is confirmed
    private val _nfcStatus = MutableStateFlow<NfcStatus>(NfcStatus.Checking)
    val nfcStatus: StateFlow<NfcStatus> = _nfcStatus.asStateFlow()

    private val _currentTag = MutableStateFlow<TagRecord?>(null)
    val currentTag: StateFlow<TagRecord?> = _currentTag.asStateFlow()

    private var activeRawTag: Tag? = null

    private val _isMifareInspecting = MutableStateFlow(false)
    val isMifareInspecting: StateFlow<Boolean> = _isMifareInspecting.asStateFlow()

    private val _mifareInspectionStatusMessage = MutableStateFlow<String?>(null)
    val mifareInspectionStatusMessage: StateFlow<String?> = _mifareInspectionStatusMessage.asStateFlow()

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

    fun onTagScanned(tagRecord: TagRecord, rawTag: Tag? = null) {
        activeRawTag = rawTag
        _currentTag.value = tagRecord
        _isCurrentTagSaved.value = false
        _mifareInspectionStatusMessage.value = null
        _nfcStatus.value = NfcStatus.TagDetected(tagRecord)
        // Note: Automatic save removed. User chooses to save manually.
    }

    fun onScanError(errorMessage: String) {
        _nfcStatus.value = NfcStatus.ScanError(errorMessage)
    }

    fun resetToWaiting() {
        activeRawTag = null
        _currentTag.value = null
        _isCurrentTagSaved.value = false
        _mifareInspectionStatusMessage.value = null
        _nfcStatus.value = NfcStatus.ReadyWaiting
    }

    /**
     * Explicit on-demand MIFARE Classic sector inspection.
     * Executes entirely on IO dispatcher, keeping tag state and updating UI reactively.
     */
    fun inspectMifareSectors(customKeyA: ByteArray? = null, customKeyB: ByteArray? = null) {
        if (_isMifareInspecting.value) return

        val tag = activeRawTag
        val current = _currentTag.value

        if (tag == null || current?.mifareClassic == null) {
            _mifareInspectionStatusMessage.value = "Mantenha a tag encostada no aparelho e tente novamente."
            return
        }

        viewModelScope.launch {
            _isMifareInspecting.value = true
            _mifareInspectionStatusMessage.value = "Inspecionando setores... Mantenha o cartão estável no leitor."

            try {
                val newMemoryMap = withContext(Dispatchers.IO) {
                    MifareClassicInspector.inspectMifare(
                        tag = tag,
                        customKeyA = customKeyA,
                        customKeyB = customKeyB,
                        testDefaultKeys = true
                    )
                }

                if (newMemoryMap != null) {
                    val updatedMfc = current.mifareClassic.copy(memoryMap = newMemoryMap)
                    val updatedRecord = current.copy(mifareClassic = updatedMfc)
                    _currentTag.value = updatedRecord
                    _nfcStatus.value = NfcStatus.TagDetected(updatedRecord)

                    _mifareInspectionStatusMessage.value = when {
                        newMemoryMap.authenticatedSectorsCount == newMemoryMap.sectorCount ->
                            "Inspeção concluída: Todos os ${newMemoryMap.sectorCount} setores foram autenticados com sucesso."
                        newMemoryMap.authenticatedSectorsCount > 0 ->
                            "Inspeção concluída: ${newMemoryMap.authenticatedSectorsCount}/${newMemoryMap.sectorCount} setores autenticados (${newMemoryMap.totalBlocksReadCount}/${newMemoryMap.blockCount} blocos lidos)."
                        else ->
                            "Inspeção concluída: Nenhum setor respondeu às chaves padrão de fábrica."
                    }
                } else {
                    _mifareInspectionStatusMessage.value = "Falha ao acessar interface MIFARE Classic. Mantenha o cartão próximo e tente novamente."
                }
            } catch (tle: TagLostException) {
                _mifareInspectionStatusMessage.value = "A tag foi afastada antes do fim da inspeção. Aproxime a tag e tente novamente."
            } catch (ioe: IOException) {
                _mifareInspectionStatusMessage.value = "Comunicação NFC interrompida. Mantenha o cartão encostado e tente novamente."
            } catch (e: Exception) {
                _mifareInspectionStatusMessage.value = "Erro na inspeção: ${e.localizedMessage ?: "Falha de comunicação"}"
            } finally {
                _isMifareInspecting.value = false
            }
        }
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
