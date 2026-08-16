package com.nfcinspector.app.domain

import com.nfcinspector.app.domain.operation.NfcError
import com.nfcinspector.app.domain.operation.NfcOperationResult
import org.junit.Assert.*
import org.junit.Test

class NfcOperationResultTest {

    @Test
    fun testSuccessResult() {
        val result: NfcOperationResult<String> = NfcOperationResult.Success("045AB21A")
        assertTrue(result.isSuccess)
        assertFalse(result.isFailure)
        assertEquals("045AB21A", result.getOrNull())
        assertEquals("045AB21A", result.getOrDefault("fallback"))
    }

    @Test
    fun testFailureResult() {
        val error = NfcError.TagLost()
        val result: NfcOperationResult<String> = NfcOperationResult.Failure(error)
        assertFalse(result.isSuccess)
        assertTrue(result.isFailure)
        assertNull(result.getOrNull())
        assertEquals("fallback", result.getOrDefault("fallback"))
    }

    @Test
    fun testErrorTypes() {
        val authErr = NfcError.AuthenticationFailed(sector = 3, keyType = "Key A")
        assertTrue(authErr.message.contains("setor 3"))

        val unsuppErr = NfcError.UnsupportedTechnology("ISO-15693")
        assertTrue(unsuppErr.message.contains("ISO-15693"))

        val transErr = NfcError.TransportError(java.io.IOException("Socket closed"))
        assertTrue(transErr.message.contains("Socket closed"))
    }
}
