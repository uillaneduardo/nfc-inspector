package com.nfcinspector.app.nfc.android

import android.content.Context
import android.nfc.NfcAdapter
import com.nfcinspector.app.domain.model.ReaderCapabilities
import com.nfcinspector.app.domain.model.ReaderSource
import com.nfcinspector.app.domain.model.ReaderSourceType
import com.nfcinspector.app.domain.transport.NfcTransport

/**
 * Concrete implementation of NfcTransport for Android internal NFC controller.
 */
class AndroidNfcTransport(
    private val context: Context,
    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)
) : NfcTransport {

    override val source: ReaderSource = ReaderSource(
        sourceType = ReaderSourceType.ANDROID_NFC,
        readerName = "NFC Interno Android",
        readerId = "internal_android_adapter",
        transport = "android_nfc"
    )

    override val capabilities: ReaderCapabilities = ReaderCapabilities.ANDROID_INTERNAL_READER_MODE

    override val isConnected: Boolean
        get() = nfcAdapter?.isEnabled == true
}
