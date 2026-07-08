package com.smartphoneaichat.data.model

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.smartphoneaichat.domain.model.AVAILABLE_MODELS
import com.smartphoneaichat.domain.model.ModelInfo
import com.smartphoneaichat.domain.repository.ModelFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class HuggingFaceModelFileManager(private val context: Context) : ModelFileManager {

    @Volatile
    private var isDownloadCancelled = false

    private var engine: Engine? = null

    var activeModelId: String? = null
        private set

    private val modelDir: File
        get() = File(context.filesDir, "models")

    val isInitialized: Boolean
        get() = engine != null

    fun getModelFile(modelInfo: ModelInfo): File =
        File(modelDir, modelInfo.fileName)

    override fun isDownloaded(modelInfo: ModelInfo): Boolean =
        getModelFile(modelInfo).exists()

    override fun listDownloadedModelIds(): List<String> =
        AVAILABLE_MODELS.asSequence().filter { isDownloaded(it) }.map { it.id }.toList()

    override suspend fun downloadModel(modelInfo: ModelInfo, onProgress: (Float) -> Unit): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val token = com.smartphoneaichat.BuildConfig.HF_TOKEN
                if (token.isEmpty()) {
                    return@withContext Result.failure(
                        RuntimeException(
                            "HuggingFace token is not configured. See KEY_SETUP.md for instructions.",
                        )
                    )
                }

                modelDir.mkdirs()
                val url = URL("https://huggingface.co/${modelInfo.hfRepo}/resolve/main/${modelInfo.fileName}")
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.connectTimeout = 30_000
                connection.readTimeout = 60_000
                connection.connect()

                if (connection.responseCode != 200) {
                    return@withContext Result.failure(
                        RuntimeException("Download failed: HTTP ${connection.responseCode}")
                    )
                }

                val totalBytes = connection.contentLengthLong
                val input = connection.inputStream
                val output = FileOutputStream(getModelFile(modelInfo))
                val buffer = ByteArray(8192)
                var bytesRead = 0L
                var read: Int

                while (input.read(buffer).also { read = it } != -1) {
                    if (isDownloadCancelled) {
                        output.close()
                        input.close()
                        getModelFile(modelInfo).delete()
                        isDownloadCancelled = false
                        return@withContext Result.failure(
                            RuntimeException("Download cancelled by user")
                        )
                    }
                    output.write(buffer, 0, read)
                    bytesRead += read
                    if (totalBytes > 0) {
                        onProgress(bytesRead.toFloat() / totalBytes)
                    }
                }

                output.close()
                input.close()
                Result.success(Unit)
            } catch (e: Exception) {
                getModelFile(modelInfo).delete()
                Result.failure(e)
            }
        }
    }

    override fun cancelDownload() {
        isDownloadCancelled = true
    }

    override suspend fun loadModel(modelInfo: ModelInfo, onProgress: (Float) -> Unit): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                if (!isDownloaded(modelInfo)) {
                    return@withContext Result.failure(
                        IllegalStateException("Model file not found. Download first.")
                    )
                }

                val engineConfig = EngineConfig(
                    modelPath = getModelFile(modelInfo).absolutePath,
                    backend = Backend.CPU(),
                    cacheDir = context.cacheDir.absolutePath
                )
                engine = Engine(engineConfig)
                engine!!.initialize()
                activeModelId = modelInfo.id
                onProgress(1f)
                Result.success(Unit)
            } catch (e: Exception) {
                unloadModel()
                Result.failure(e)
            }
        }
    }

    override fun unloadModel() {
        try {
            engine?.close()
        } catch (_: Exception) {}
        engine = null
        activeModelId = null
    }

    override fun deleteModelFile(modelInfo: ModelInfo): Boolean {
        if (activeModelId == modelInfo.id) unloadModel()
        return if (getModelFile(modelInfo).exists()) getModelFile(modelInfo).delete() else true
    }

    internal fun getEngine(): Engine? = engine
}