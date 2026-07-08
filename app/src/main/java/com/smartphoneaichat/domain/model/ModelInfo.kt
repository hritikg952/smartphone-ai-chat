package com.smartphoneaichat.domain.model

data class ModelInfo(
    val id: String,
    val displayName: String,
    val hfRepo: String,
    val fileName: String
)

val AVAILABLE_MODELS = listOf(
    ModelInfo(
        id = "gemma3-1b",
        displayName = "Gemma 3 1B",
        hfRepo = "litert-community/Gemma3-1B-IT",
        fileName = "gemma3-1b-it-int4.litertlm"
    ),
    ModelInfo(
        id = "gemma4-e2b",
        displayName = "Gemma 4 E2B",
        hfRepo = "litert-community/gemma-4-E2B-it-litert-lm",
        fileName = "gemma-4-E2B-it.litertlm"
    )
)

fun modelInfoById(id: String): ModelInfo? =
    AVAILABLE_MODELS.find { it.id == id }