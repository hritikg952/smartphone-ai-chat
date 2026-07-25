package com.smartphoneaichat.data.persistence

import com.smartphoneaichat.data.security.AesGcmVaultCipher
import com.smartphoneaichat.data.security.DefaultVaultSession
import com.smartphoneaichat.data.security.RandomBytes
import com.smartphoneaichat.domain.repository.DocumentImportResult
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class LocalEncryptedDocumentStoreTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun importAndRead_requireUnlockedVaultAndPublishOpaqueEncryptedFiles() {
        val lockedStore = LocalEncryptedDocumentStore(
            rootDirectory = tempDir,
            cipher = createCipher(unlocked = false),
        )

        assertEquals(
            DocumentImportResult.Locked,
            lockedStore.import(
                profileId = "profile-a",
                documentId = "lab-report-2026",
                mimeType = "application/pdf",
                bytes = "cholesterol 190".encodeToByteArray(),
            ),
        )

        val unlockedStore = LocalEncryptedDocumentStore(
            rootDirectory = tempDir,
            cipher = createCipher(unlocked = true),
        )
        assertEquals(
            DocumentImportResult.Imported,
            unlockedStore.import(
                profileId = "profile-a",
                documentId = "lab-report-2026",
                mimeType = "application/pdf",
                bytes = "cholesterol 190".encodeToByteArray(),
            ),
        )

        val restartedStore = LocalEncryptedDocumentStore(
            rootDirectory = tempDir,
            cipher = createCipher(unlocked = true),
        )

        assertArrayEquals(
            "cholesterol 190".encodeToByteArray(),
            restartedStore.read(profileId = "profile-a", documentId = "lab-report-2026"),
        )
        val publishedFiles = Files.walk(tempDir).use { stream ->
            stream.filter(Files::isRegularFile).toList()
        }
        assertFalse(publishedFiles.any { it.fileName.toString().contains("lab-report") })
        assertFalse(
            publishedFiles.any { String(Files.readAllBytes(it)).contains("cholesterol 190") },
        )
    }

    @Test
    fun import_whenDocumentIndexIsMalformed_failsClosedWithoutPublishingBlob() {
        Files.write(tempDir.resolve("document-index.v1"), "not-a-valid-index".encodeToByteArray())
        val store = LocalEncryptedDocumentStore(
            rootDirectory = tempDir,
            cipher = createCipher(unlocked = true),
        )

        assertEquals(
            DocumentImportResult.Unavailable,
            store.import(
                profileId = "profile-a",
                documentId = "document-1",
                mimeType = "application/pdf",
                bytes = "sensitive pdf body".encodeToByteArray(),
            ),
        )

        val publishedFiles = Files.walk(tempDir).use { stream ->
            stream.filter(Files::isRegularFile).toList()
        }
        assertEquals(listOf("document-index.v1"), publishedFiles.map { it.fileName.toString() })
    }

    private fun createCipher(unlocked: Boolean): AesGcmVaultCipher {
        val session = DefaultVaultSession()
        if (unlocked) session.unlock(ByteArray(32) { 8 })
        return AesGcmVaultCipher(
            session = session,
            randomBytes = IncrementingRandomBytes(),
        )
    }

    private class IncrementingRandomBytes : RandomBytes {
        private var value = 31

        override fun next(size: Int): ByteArray = ByteArray(size) { value++.toByte() }
    }
}
