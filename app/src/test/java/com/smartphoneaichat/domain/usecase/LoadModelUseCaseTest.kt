package com.smartphoneaichat.domain.usecase

import com.smartphoneaichat.domain.repository.ModelFileManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LoadModelUseCaseTest {

    private val modelFileManager: ModelFileManager = mockk(relaxed = true)
    private val useCase = LoadModelUseCase(modelFileManager)

    @Test
    fun success_unloadsPreviousModelFirst() = runTest {
        coEvery { modelFileManager.loadModel(any(), any()) } returns Result.success(Unit)

        useCase("gemma3-1b")

        verify(exactly = 1) { modelFileManager.unloadModel() }
    }

    @Test
    fun success_callsLoadModelAfterUnload() = runTest {
        coEvery { modelFileManager.loadModel(any(), any()) } returns Result.success(Unit)

        useCase("gemma4-e2b")

        verify { modelFileManager.unloadModel() }
        coVerify { modelFileManager.loadModel(match { it.id == "gemma4-e2b" }, any()) }
    }

    @Test
    fun unknownModel_returnsFailure() = runTest {
        val result = useCase("invalid-model")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun forwardsProgressCallback() = runTest {
        val progressValues = mutableListOf<Float>()
        coEvery { modelFileManager.loadModel(any(), any()) } answers {
            val callback = secondArg<(Float) -> Unit>()
            callback(0.5f)
            callback(1.0f)
            Result.success(Unit)
        }

        useCase("gemma3-1b") { progress ->
            progressValues.add(progress)
        }

        assertEquals(listOf(0.5f, 1.0f), progressValues)
    }

    @Test
    fun propagatesLoadFailure() = runTest {
        val error = RuntimeException("Initialization error")
        coEvery { modelFileManager.loadModel(any(), any()) } returns Result.failure(error)

        val result = useCase("gemma3-1b")

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}