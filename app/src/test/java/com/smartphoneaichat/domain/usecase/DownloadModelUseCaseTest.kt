package com.smartphoneaichat.domain.usecase

import com.smartphoneaichat.domain.repository.ModelFileManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DownloadModelUseCaseTest {

    private val modelFileManager: ModelFileManager = mockk(relaxed = true)
    private val useCase = DownloadModelUseCase(modelFileManager)

    @Test
    fun success_delegatesToModelFileManager() = runTest {
        coEvery { modelFileManager.downloadModel(any(), any()) } returns Result.success(Unit)

        val result = useCase("gemma3-1b")

        assertTrue(result.isSuccess)
        coVerify { modelFileManager.downloadModel(match { it.id == "gemma3-1b" }, any()) }
    }

    @Test
    fun unknownModel_returnsFailure() = runTest {
        val result = useCase("nonexistent-model-id")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Unknown model"))
    }

    @Test
    fun forwardsProgressCallback() = runTest {
        val progressValues = mutableListOf<Float>()
        coEvery { modelFileManager.downloadModel(any(), any()) } answers {
            val callback = secondArg<(Float) -> Unit>()
            callback(0.25f)
            callback(0.5f)
            callback(1.0f)
            Result.success(Unit)
        }

        useCase("gemma4-e2b") { progress ->
            progressValues.add(progress)
        }

        assertEquals(listOf(0.25f, 0.5f, 1.0f), progressValues)
    }

    @Test
    fun propagatesDownloadFailure() = runTest {
        val error = RuntimeException("Network error")
        coEvery { modelFileManager.downloadModel(any(), any()) } returns Result.failure(error)

        val result = useCase("gemma3-1b")

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}