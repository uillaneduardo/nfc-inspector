package com.nfcinspector.app.nfc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import com.nfcinspector.app.data.model.NfcStatus
import com.nfcinspector.app.data.model.TagRecord

class NfcManager(
    private val context: Context,
    private val onTagScanned: (TagRecord) -> Unit,
    private val onError: (String) -> Unit
) : NfcAdapter.ReaderCallback {

    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)

    fun checkNfcStatus(): NfcStatus {
        if (nfcAdapter == null) {
            return NfcStatus.Unsupported
        }
        if (!nfcAdapter.isEnabled) {
            return NfcStatus.Disabled
        }
        return NfcStatus.ReadyWaiting
    }

    fun startReaderMode(activity: Activity) {
        val adapter = nfcAdapter ?: return
        if (!adapter.isEnabled) return

        val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or
                NfcAdapter.FLAG_READER_NFC_V or
                NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS

        val options = Bundle()
        // 200ms presence check delay for stable tag discovery
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 200)

        try {
            adapter.enableReaderMode(activity, this, flags, options)
        } catch (e: Exception) {
            onError("Erro ao iniciar modo leitor NFC: ${e.localizedMessage}")
        }
    }

    fun stopReaderMode(activity: Activity) {
        val adapter = nfcAdapter ?: return
        try {
            adapter.disableReaderMode(activity)
        } catch (_: Exception) {
            // Ignored on teardown
        }
    }

    override fun onTagDiscovered(tag: Tag?) {
        if (tag == null) return
        triggerHapticFeedback()
        try {
            val record = NfcTagParser.parseTag(tag)
            onTagScanned(record)
        } catch (e: Exception) {
            val msg = if (e is java.io.IOException || e.javaClass.simpleName.contains("TagLost")) {
                "A tag foi afastada antes da conclusão da leitura. Mantenha o cartão estável e tente novamente."
            } else {
                "Falha na comunicação com a tag: ${e.localizedMessage ?: "Erro desconhecido"}"
            }
            onError(msg)
        }
    }

    private fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(50)
                }
            }
        } catch (_: Exception) {
            // Safe fallback if vibration permission or hardware unavailable
        }
    }

    companion object {
        fun openNfcSettings(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_NFC_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback for devices without explicit ACTION_NFC_SETTINGS
                try {
                    val fallbackIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(fallbackIntent)
                } catch (_: Exception) {
                    // Settings activity not found
                }
            }
        }
    }
}
