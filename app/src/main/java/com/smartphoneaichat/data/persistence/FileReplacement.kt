package com.smartphoneaichat.data.persistence

import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

internal fun replaceFile(tempFile: Path, targetFile: Path) {
    try {
        Files.move(
            tempFile,
            targetFile,
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE,
        )
    } catch (_: AtomicMoveNotSupportedException) {
        Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING)
    }
}
