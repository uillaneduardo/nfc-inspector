package com.nfcinspector.app.domain.operation

/**
 * Domain-level result wrapper for NFC operations to prevent leaking
 * platform-specific exceptions across architectural boundaries.
 */
sealed interface NfcOperationResult<out T> {
    data class Success<T>(val data: T) : NfcOperationResult<T>
    data class Failure(val error: NfcError) : NfcOperationResult<Nothing>

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Failure -> defaultValue
    }
}

/**
 * Structured domain errors representing distinct NFC failure modes.
 */
sealed class NfcError(open val message: String) {
    data class TagLost(
        override val message: String = "A tag foi afastada durante a operação. Mantenha o cartão estável no sensor."
    ) : NfcError(message)

    data class AuthenticationFailed(
        val sector: Int? = null,
        val keyType: String? = null,
        override val message: String = if (sector != null) "Falha na autenticação do setor $sector." else "Falha na autenticação com a tag."
    ) : NfcError(message)

    data class UnsupportedTechnology(
        val technologyName: String,
        override val message: String = "Tecnologia não suportada pelo hardware ou leitor: $technologyName"
    ) : NfcError(message)

    data class ReaderDisconnected(
        override val message: String = "O leitor NFC foi desconectado ou desativado."
    ) : NfcError(message)

    data class PermissionDenied(
        override val message: String = "Permissão necessária de NFC não concedida."
    ) : NfcError(message)

    data class TransportError(
        val cause: Throwable? = null,
        override val message: String = "Erro na camada de transporte NFC: ${cause?.localizedMessage ?: "Falha de I/O"}"
    ) : NfcError(message)

    data class Timeout(
        override val message: String = "Tempo limite excedido na comunicação com a tag."
    ) : NfcError(message)
}
