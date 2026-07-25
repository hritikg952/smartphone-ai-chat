package com.smartphoneaichat.data.persistence

import com.smartphoneaichat.domain.model.VaultAssociatedData
import com.smartphoneaichat.domain.model.VaultCryptoResult
import com.smartphoneaichat.domain.model.VaultEncryptedPayload
import com.smartphoneaichat.domain.repository.DocumentImportResult
import com.smartphoneaichat.domain.repository.EncryptedDocumentStore
import com.smartphoneaichat.domain.repository.VaultCipher
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.Base64

class LocalEncryptedDocumentStore(
    private val rootDirectory: Path,
    private val cipher: VaultCipher,
    private val maxBytes: Int = DEFAULT_MAX_BYTES,
    private val allowedMimeTypes: Set<String> = DEFAULT_ALLOWED_MIME_TYPES,
) : EncryptedDocumentStore {
    private val documentDirectory = rootDirectory.resolve("documents")
    private val indexFile = rootDirectory.resolve(INDEX_FILE)

    override fun import(
        profileId: String,
        documentId: String,
        mimeType: String,
        bytes: ByteArray,
    ): DocumentImportResult {
        require(profileId.isNotBlank())
        require(documentId.isNotBlank())
        if (mimeType !in allowedMimeTypes) return DocumentImportResult.UnsupportedType
        if (bytes.size > maxBytes) return DocumentImportResult.TooLarge

        val encrypted = cipher.encrypt(bytes, associatedData(profileId, documentId))
        if (encrypted == VaultCryptoResult.Locked) return DocumentImportResult.Locked
        if (encrypted !is VaultCryptoResult.Encrypted) return DocumentImportResult.Unavailable

        return runCatching {
            Files.createDirectories(documentDirectory)
            val opaqueName = opaqueName(profileId, documentId)
            val tempBlob = documentDirectory.resolve("$opaqueName.tmp")
            val finalBlob = documentDirectory.resolve("$opaqueName.blob")
            try {
                Files.write(tempBlob, encrypted.payload.encode().toByteArray(StandardCharsets.UTF_8))
                replaceFile(tempBlob, finalBlob)
                val updated = loadIndex()
                    .filterNot { it.profileId == profileId && it.documentId == documentId }
                    .plus(DocumentIndexEntry(profileId, documentId, mimeType, finalBlob.fileName.toString()))
                writeIndex(updated)
            } catch (error: Exception) {
                Files.deleteIfExists(tempBlob)
                Files.deleteIfExists(finalBlob)
                throw error
            }
            DocumentImportResult.Imported
        }.getOrDefault(DocumentImportResult.Unavailable)
    }

    override fun read(profileId: String, documentId: String): ByteArray? {
        val entry = runCatching { loadIndex() }.getOrDefault(emptyList()).firstOrNull {
            it.profileId == profileId && it.documentId == documentId
        } ?: return null
        val payload = runCatching {
            decodePayload(
                String(
                    Files.readAllBytes(documentDirectory.resolve(entry.blobName)),
                    StandardCharsets.UTF_8,
                ),
            )
        }.getOrNull() ?: return null
        return when (val decrypted = cipher.decrypt(payload, associatedData(profileId, documentId))) {
            is VaultCryptoResult.Plaintext -> decrypted.bytes
            else -> null
        }
    }

    override fun delete(profileId: String, documentId: String): Boolean = runCatching {
        val entries = loadIndex()
        val target = entries.firstOrNull {
            it.profileId == profileId && it.documentId == documentId
        } ?: return true
        Files.deleteIfExists(documentDirectory.resolve(target.blobName))
        writeIndex(entries.filterNot { it.profileId == profileId && it.documentId == documentId })
        true
    }.getOrDefault(false)

    private fun loadIndex(): List<DocumentIndexEntry> {
        if (!Files.exists(indexFile)) return emptyList()
        return Files.readAllLines(indexFile, StandardCharsets.UTF_8)
            .filter { it.isNotBlank() }
            .map { line -> DocumentIndexEntry.decode(line) }
    }

    private fun writeIndex(entries: List<DocumentIndexEntry>) {
        Files.createDirectories(rootDirectory)
        val tempIndex = rootDirectory.resolve("$INDEX_FILE.tmp")
        val body = entries.joinToString(separator = "\n", postfix = "\n") { it.encode() }
        Files.write(tempIndex, body.toByteArray(StandardCharsets.UTF_8))
        replaceFile(tempIndex, indexFile)
    }

    private fun associatedData(profileId: String, documentId: String): VaultAssociatedData =
        VaultAssociatedData(
            profileId = profileId,
            recordId = "document:$documentId",
            schemaVersion = DOCUMENT_SCHEMA_VERSION,
        )

    private fun opaqueName(profileId: String, documentId: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$profileId\u0000$documentId".toByteArray(StandardCharsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    private data class DocumentIndexEntry(
        val profileId: String,
        val documentId: String,
        val mimeType: String,
        val blobName: String,
    ) {
        fun encode(): String = listOf(
            INDEX_VERSION.toString(),
            profileId.b64(),
            documentId.b64(),
            mimeType.b64(),
            blobName.b64(),
        ).joinToString("\t")

        companion object {
            fun decode(line: String): DocumentIndexEntry {
                val parts = line.split("\t")
                require(parts.size == 5)
                require(parts[0].toInt() == INDEX_VERSION)
                return DocumentIndexEntry(
                    profileId = parts[1].unb64(),
                    documentId = parts[2].unb64(),
                    mimeType = parts[3].unb64(),
                    blobName = parts[4].unb64(),
                )
            }
        }
    }

    private companion object {
        const val INDEX_FILE = "document-index.v1"
        const val INDEX_VERSION = 1
        const val DOCUMENT_SCHEMA_VERSION = 1
        const val DEFAULT_MAX_BYTES = 25 * 1024 * 1024
        val DEFAULT_ALLOWED_MIME_TYPES = setOf(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "text/plain",
        )

        fun VaultEncryptedPayload.encode(): String = listOf(
            version.toString(),
            nonceBase64.b64(),
            ciphertextBase64.b64(),
        ).joinToString("\t")

        fun decodePayload(value: String): VaultEncryptedPayload {
            val parts = value.trim().split("\t")
            require(parts.size == 3)
            return VaultEncryptedPayload(
                version = parts[0].toInt(),
                nonceBase64 = parts[1].unb64(),
                ciphertextBase64 = parts[2].unb64(),
            )
        }

        fun String.b64(): String = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(toByteArray(StandardCharsets.UTF_8))

        fun String.unb64(): String = String(
            Base64.getUrlDecoder().decode(this),
            StandardCharsets.UTF_8,
        )
    }
}
