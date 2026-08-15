package com.nfcinspector.app.data.model

sealed interface NfcStatus {
    object Checking : NfcStatus
    object Unsupported : NfcStatus
    object Disabled : NfcStatus
    object ReadyWaiting : NfcStatus
    data class TagDetected(val tagRecord: TagRecord) : NfcStatus
    data class ScanError(val message: String) : NfcStatus
}
