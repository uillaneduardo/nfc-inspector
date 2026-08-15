package com.nfcinspector.app.nfc.lab

import android.nfc.cardemulation.HostApduService
import android.os.Bundle

/**
 * NFC Lab Architecture Foundation
 * 
 * Reservado para futuros experimentos de emulação e testes de protocolo proprietário
 * de laboratório (Host Card Emulation - HCE).
 * 
 * DIRETRIZES DE SEGURANÇA:
 * - Não realiza clonagem de cartões bancários ou credenciais físicas capturadas.
 * - Utiliza AID (Application Identifier) proprietário exclusivo de diagnóstico.
 */
abstract class BaseNfcLabApduService : HostApduService() {

    companion object {
        // Exemplo de AID de laboratório para testes de protocolo experimental
        val LAB_CUSTOM_AID = "F0010203040506"
        val STATUS_SUCCESS = byteArrayOf(0x90.toByte(), 0x00.toByte())
        val STATUS_UNKNOWN_CMD = byteArrayOf(0x6E.toByte(), 0x00.toByte())
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) return STATUS_UNKNOWN_CMD
        // Responder apenas a comandos do protocolo de laboratório
        return handleLabCommand(commandApdu)
    }

    abstract fun handleLabCommand(apdu: ByteArray): ByteArray

    override fun onDeactivated(reason: Int) {
        // Limpeza de sessão de teste
    }
}
