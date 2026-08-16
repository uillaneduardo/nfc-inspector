package com.nfcinspector.app.domain.transport

import com.nfcinspector.app.domain.model.ReaderCapabilities
import com.nfcinspector.app.domain.model.ReaderSource

/**
 * Clean transport boundary decoupling NFC tag acquisition and byte transceive
 * operations from platform-specific APIs.
 */
interface NfcTransport {
    val source: ReaderSource
    val capabilities: ReaderCapabilities
    val isConnected: Boolean
}
