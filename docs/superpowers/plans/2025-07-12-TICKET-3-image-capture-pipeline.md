# TICKET-3: Image Capture Pipeline & Attachment Types — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Wire the ScannerScreen camera capture into the ChatScreen message pipeline: compress captured bitmap to JPEG on disk, pass the file path via Navigation result, display it as a real thumbnail in ChatInput, and prepare the Attachment domain model for real image data.

**Architecture:** Navigation Compose `savedStateHandle` result pattern passes the image path from ScannerScreen back to ChatScreen. `ChatViewModel.receiveCapturedImage()` creates a pending USER message with an `Attachment` referencing the real file path. `ChatInput` loads the bitmap from the file path and renders it as a thumbnail instead of a placeholder icon.

**Tech Stack:** Kotlin, Compose, CameraX (existing), `BitmapFactory` for JPEG decoding, Navigation Compose `savedStateHandle`.

## Global Constraints

- No comments in production code (per AGENTS.md conventions)
- No emojis in UI code
- All composables take `Modifier` parameter
- Use `_state.update { it.copy(...) }` pattern for all ViewModel state mutations
- Domain models in `domain/model/` must remain pure Kotlin (no Android framework dependencies on `Message`/`Conversation`; `Attachment` is okay since it already lives in the same file and ScannerScreen uses `Bitmap`)
- AGENTS.md "No comments" rule applies — no KDoc or inline comments in new production code

---

### Task 1: Update `Attachment` domain model with real image data

**Files:**
- Modify: `app/src/main/java/com/smartphoneaichat/domain/model/Message.kt`

**Interfaces:**
- Produces: `Attachment(fileName: String, mimeType: String, imageUri: String? = null)` — new optional `imageUri` field that holds the local file path of a captured/attached image (e.g. `/data/data/.../files/captures/capture_1712345678.jpg`).

- [ ] **Step 1: Add `imageUri` field to `Attachment`**

Open `app/src/main/java/com/smartphoneaichat/domain/model/Message.kt`. The current `Attachment` data class (lines 25-31):

```kotlin
data class Attachment(
    /** Display name of the attached file. */
    val fileName: String,

    /** Short MIME-type hint used to decide the thumbnail icon. */
    val mimeType: String
)
```

Replace with:

```kotlin
data class Attachment(
    val fileName: String,
    val mimeType: String,
    val imageUri: String? = null,
)
```

Note: Remove the KDoc comments (convention: no comments in production code).

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/smartphoneaichat/domain/model/Message.kt
git commit -m "feat: add imageUri field to Attachment domain model"
```

---

### Task 2: Add capture pipeline to `ScannerUiState` and `ScannerViewModel`

**Files:**
- Modify: `app/src/main/java/com/smartphoneaichat/presentation/state/ScannerUiState.kt`
- Modify: `app/src/main/java/com/smartphoneaichat/presentation/viewmodel/ScannerViewModel.kt`

**Interfaces:**
- Consumes: `Attachment.imageUri: String?` (from Task 1)
- Produces:
  - `ScannerUiState(capturedImagePath: String? = null, captureStatus: CaptureStatus = CaptureStatus.Idle, ...)` — new fields
  - `CaptureStatus` enum: `Idle, Saving, Saved, Error`
  - `ScannerViewModel.saveCapturedBitmap(bitmap: Bitmap)` — compresses to JPEG, writes to `filesDir/captures/`, updates state with path
  - `ScannerViewModel.resetCapture()` — clears captured state

- [ ] **Step 1: Add `CaptureStatus` enum and update `ScannerUiState`**

Open `app/src/main/java/com/smartphoneaichat/presentation/state/ScannerUiState.kt`. Current content:

```kotlin
package com.smartphoneaichat.presentation.state

import android.graphics.Bitmap

data class ScannerUiState(
    val cameraPermissionGranted: Boolean = false,
    val capturedBitmap: Bitmap? = null,
    val isAnalyzing: Boolean = false,
)
```

Replace with:

```kotlin
package com.smartphoneaichat.presentation.state

import android.graphics.Bitmap

enum class CaptureStatus { Idle, Saving, Saved, Error }

data class ScannerUiState(
    val cameraPermissionGranted: Boolean = false,
    val capturedBitmap: Bitmap? = null,
    val isAnalyzing: Boolean = false,
    val capturedImagePath: String? = null,
    val captureStatus: CaptureStatus = CaptureStatus.Idle,
    val captureError: String? = null,
)
```

- [ ] **Step 2: Add `saveCapturedBitmap` and `resetCapture` to `ScannerViewModel`**

Open `app/src/main/java/com/smartphoneaichat/presentation/viewmodel/ScannerViewModel.kt`. Current content:

```kotlin
package com.smartphoneaichat.presentation.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import com.smartphoneaichat.presentation.state.ScannerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(ScannerUiState())
    val state: StateFlow<ScannerUiState> = _state.asStateFlow()

    fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(cameraPermissionGranted = granted) }
    }

    fun onBitmapCaptured(bitmap: Bitmap) {
        _state.update { it.copy(capturedBitmap = bitmap, isAnalyzing = false) }
    }

    fun setAnalyzing(analyzing: Boolean) {
        _state.update { it.copy(isAnalyzing = analyzing) }
    }

    fun clearCapturedBitmap() {
        _state.update { it.copy(capturedBitmap = null) }
    }

    override fun onCleared() {
        super.onCleared()
        _state.update { it.copy(capturedBitmap = null) }
    }
}
```

Replace with:

```kotlin
package com.smartphoneaichat.presentation.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartphoneaichat.presentation.state.CaptureStatus
import com.smartphoneaichat.presentation.state.ScannerUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(ScannerUiState())
    val state: StateFlow<ScannerUiState> = _state.asStateFlow()

    fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(cameraPermissionGranted = granted) }
    }

    fun onBitmapCaptured(bitmap: Bitmap) {
        _state.update { it.copy(capturedBitmap = bitmap, isAnalyzing = false) }
    }

    fun setAnalyzing(analyzing: Boolean) {
        _state.update { it.copy(isAnalyzing = analyzing) }
    }

    fun clearCapturedBitmap() {
        _state.update { it.copy(capturedBitmap = null) }
    }

    fun saveCapturedBitmap(onSaved: (String) -> Unit) {
        val bitmap = _state.value.capturedBitmap ?: return
        _state.update { it.copy(captureStatus = CaptureStatus.Saving, captureError = null) }

        viewModelScope.launch {
            try {
                val path = withContext(Dispatchers.IO) {
                    val capturesDir = File(getApplication<Application>().filesDir, "captures")
                    if (!capturesDir.exists()) capturesDir.mkdirs()

                    val filename = "capture_${System.currentTimeMillis()}.jpg"
                    val file = File(capturesDir, filename)
                    FileOutputStream(file).use { fos ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
                    }
                    file.absolutePath
                }
                _state.update {
                    it.copy(
                        capturedImagePath = path,
                        captureStatus = CaptureStatus.Saved,
                    )
                }
                onSaved(path)
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        captureStatus = CaptureStatus.Error,
                        captureError = e.message ?: "Failed to save image",
                    )
                }
            }
        }
    }

    fun resetCapture() {
        _state.update {
            it.copy(
                capturedBitmap = null,
                capturedImagePath = null,
                captureStatus = CaptureStatus.Idle,
                captureError = null,
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        _state.update { it.copy(capturedBitmap = null) }
    }
}
```

- [ ] **Step 3: Verify the changes compile**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL (no compilation errors in ScannerViewModel or ScannerUiState)

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/smartphoneaichat/presentation/state/ScannerUiState.kt app/src/main/java/com/smartphoneaichat/presentation/viewmodel/ScannerViewModel.kt
git commit -m "feat: add image save pipeline to ScannerViewModel with CaptureStatus"
```

---

### Task 3: Update `ScannerScreen` — save image, navigate back with result

**Files:**
- Modify: `app/src/main/java/com/smartphoneaichat/ui/screens/ScannerScreen.kt`

**Interfaces:**
- Consumes:
  - `ScannerViewModel.saveCapturedBitmap(onSaved: (String) -> Unit)` (from Task 2)
  - `ScannerUiState.captureStatus`, `captureError`, `capturedImagePath` (from Task 2)
- Produces: `ScannerScreen(viewModel: ScannerViewModel, onImageCaptured: (String) -> Unit, modifier: Modifier = Modifier)` — new `onImageCaptured` callback parameter

The `onImageCaptured` callback is invoked after successful save with the file path. The caller (MainActivity) will set the navigation result and pop back.

- [ ] **Step 1: Add `onImageCaptured` parameter and wire the capture pipeline**

Open `app/src/main/java/com/smartphoneaichat/ui/screens/ScannerScreen.kt`.

**Change 1:** Update the function signature (line 54-57). Replace:

```kotlin
fun ScannerScreen(
    viewModel: ScannerViewModel,
    modifier: Modifier = Modifier,
) {
```

With:

```kotlin
fun ScannerScreen(
    viewModel: ScannerViewModel,
    onImageCaptured: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
```

**Change 2:** Replace the "Analyze" button's `onCaptureSuccess` callback (lines 173-177). Currently:

```kotlin
override fun onCaptureSuccess(image: ImageProxy) {
    val bitmap = imageProxyToBitmap(image)
    image.close()
    viewModel.onBitmapCaptured(bitmap)
}
```

Replace with:

```kotlin
override fun onCaptureSuccess(image: ImageProxy) {
    val bitmap = imageProxyToBitmap(image)
    image.close()
    viewModel.onBitmapCaptured(bitmap)
    viewModel.saveCapturedBitmap { path ->
        onImageCaptured(path)
    }
}
```

**Change 3:** Replace the status text below the button (lines 195-203). Currently:

```kotlin
Text(
    text = when {
        state.isAnalyzing -> "Capturing..."
        state.capturedBitmap != null -> "Captured!"
        else -> "Point camera at text"
    },
    style = MaterialTheme.typography.bodyMedium,
    color = TextSecondary,
)
```

Replace with:

```kotlin
Text(
    text = when {
        state.captureStatus == CaptureStatus.Saving -> "Saving image..."
        state.captureStatus == CaptureStatus.Saved -> "Captured!"
        state.captureStatus == CaptureStatus.Error -> state.captureError ?: "Capture failed"
        state.isAnalyzing -> "Capturing..."
        state.capturedBitmap != null -> "Image captured"
        else -> "Point camera at text"
    },
    style = MaterialTheme.typography.bodyMedium,
    color = if (state.captureStatus == CaptureStatus.Error) AccentRed else TextSecondary,
)
```

**Change 4:** Add the import for `CaptureStatus` and `AccentRed`. After line 49, add:

```kotlin
import com.smartphoneaichat.presentation.state.CaptureStatus
import com.smartphoneaichat.ui.theme.AccentRed
```

Full imports block should now include both new imports:

```kotlin
import com.smartphoneaichat.presentation.state.CaptureStatus
import com.smartphoneaichat.ui.theme.AccentBlue
import com.smartphoneaichat.ui.theme.AccentRed
```

(adjust import order alphabetically)

**Change 5:** Disable the Analyze button during save. Update the button's `enabled` (line 185) from:

```kotlin
enabled = !state.isAnalyzing,
```

To:

```kotlin
enabled = !state.isAnalyzing && state.captureStatus != CaptureStatus.Saving,
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL (with ScannerScreen signature change — will fail at call site in MainActivity until Task 5)

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/smartphoneaichat/ui/screens/ScannerScreen.kt
git commit -m "feat: wire capture save pipeline into ScannerScreen with onImageCaptured callback"
```

---

### Task 4: Add `receiveCapturedImage` to `ChatViewModel`

**Files:**
- Modify: `app/src/main/java/com/smartphoneaichat/presentation/viewmodel/ChatViewModel.kt`

**Interfaces:**
- Consumes: `Attachment.imageUri: String?` (from Task 1)
- Produces: `ChatViewModel.receiveCapturedImage(path: String)` — creates a pending USER `Message` with an `Attachment(fileName=<extracted>, mimeType="image/jpeg", imageUri=path)`, appends it to the active conversation (same pattern as existing `attachImage()`)

- [ ] **Step 1: Add `receiveCapturedImage` method**

Open `app/src/main/java/com/smartphoneaichat/presentation/viewmodel/ChatViewModel.kt`. After the existing `removePendingAttachment()` method (line 179), insert the new method:

```kotlin
    fun receiveCapturedImage(path: String) {
        val stateSnapshot = _state.value
        val conv = stateSnapshot.activeConversation ?: return

        val filename = path.substringAfterLast("/")

        val attachment = Attachment(
            fileName = filename,
            mimeType = "image/jpeg",
            imageUri = path
        )

        val pendingMessage = Message(
            id = idGenerator.generateMessageId(),
            role = ChatRole.USER,
            text = MessageText(""),
            attachment = attachment
        )

        val updatedConv = conv.addMessage(pendingMessage)

        _state.update { replaceConversation(updatedConv) }
        notifications.show(AppNotificationEvent.Success("Image captured: $filename"))
    }
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/smartphoneaichat/presentation/viewmodel/ChatViewModel.kt
git commit -m "feat: add receiveCapturedImage to ChatViewModel for scanner-to-chat image flow"
```

---

### Task 5: Wire `MainActivity` navigation result passing

**Files:**
- Modify: `app/src/main/java/com/smartphoneaichat/MainActivity.kt`

**Interfaces:**
- Consumes:
  - `ScannerScreen(onImageCaptured: (String) -> Unit, ...)` (from Task 3)
  - `ChatViewModel.receiveCapturedImage(path: String)` (from Task 4)
  - `ChatScreen(viewModel)` — existing
- Produces: Navigation result pattern wired between Scanner and Chat routes via `savedStateHandle`

- [ ] **Step 1: Update Scanner composable to pass `onImageCaptured` callback and Chat composable to pass `savedStateHandle`**

Open `app/src/main/java/com/smartphoneaichat/MainActivity.kt`. The current NavHost block (lines 95-114) should be updated.

**Change 1:** Update the `composable(Screen.Scanner.route)` block (lines 105-109). Replace:

```kotlin
                        composable(Screen.Scanner.route) {
                            val scannerFactory = remember { ScannerViewModelFactory(app) }
                            val scannerViewModel: ScannerViewModel = viewModel(factory = scannerFactory)
                            ScannerScreen(viewModel = scannerViewModel)
                        }
```

With:

```kotlin
                        composable(Screen.Scanner.route) {
                            val scannerFactory = remember { ScannerViewModelFactory(app) }
                            val scannerViewModel: ScannerViewModel = viewModel(factory = scannerFactory)
                            ScannerScreen(
                                viewModel = scannerViewModel,
                                onImageCaptured = { path ->
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("captured_image_path", path)
                                    navController.popBackStack()
                                }
                            )
                        }
```

**Change 2:** Update the `composable(Screen.Chat.route)` block (lines 102-104). Replace:

```kotlin
                        composable(Screen.Chat.route) {
                            ChatScreen(viewModel = viewModel)
                        }
```

With:

```kotlin
                        composable(Screen.Chat.route) { entry ->
                            ChatScreen(
                                viewModel = viewModel,
                                savedStateHandle = entry.savedStateHandle,
                            )
                        }
```

- [ ] **Step 2: Update `ChatScreen` to accept and observe `savedStateHandle`**

Open `app/src/main/java/com/smartphoneaichat/ui/screens/ChatScreen.kt`. 

**Change 1:** Update the function signature (line 77). Replace:

```kotlin
fun ChatScreen(viewModel: ChatViewModel) {
```

With:

```kotlin
fun ChatScreen(
    viewModel: ChatViewModel,
    savedStateHandle: androidx.lifecycle.SavedStateHandle,
) {
```

**Change 2:** Add import at top:

```kotlin
import androidx.lifecycle.SavedStateHandle
```

**Change 3:** After line 83 (`val conversationTitle = ...`), add the `LaunchedEffect` to observe the saved image path. Insert:

```kotlin
    LaunchedEffect(Unit) {
        savedStateHandle.get<String>("captured_image_path")?.let { path ->
            viewModel.receiveCapturedImage(path)
            savedStateHandle.remove<String>("captured_image_path")
        }
    }
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/smartphoneaichat/MainActivity.kt app/src/main/java/com/smartphoneaichat/ui/screens/ChatScreen.kt
git commit -m "feat: wire navigation result passing for scanner-to-chat image pipeline"
```

---

### Task 6: Show real image thumbnail in `ChatInput` when `imageUri` is present

**Files:**
- Modify: `app/src/main/java/com/smartphoneaichat/ui/components/ChatInput.kt`

**Interfaces:**
- Consumes: `Attachment.imageUri: String?` (from Task 1)
- Produces: `ChatInput` renders actual image bitmap from the local file path instead of a placeholder icon when `imageUri` is non-null

- [ ] **Step 1: Replace placeholder icon with real image when `imageUri` is available**

Open `app/src/main/java/com/smartphoneaichat/ui/components/ChatInput.kt`.

**Change 1:** Add required imports at the top of the file (after line 28):

```kotlin
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.io.File
```

**Change 2:** Replace the thumbnail placeholder section (lines 97-111). Currently:

```kotlin
                    // Thumbnail placeholder
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
```

Replace with:

```kotlin
                    if (att.imageUri != null && File(att.imageUri).exists()) {
                        val bitmap = remember(att.imageUri) {
                            BitmapFactory.decodeFile(att.imageUri)
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Captured image",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            ThumbnailPlaceholder()
                        }
                    } else {
                        ThumbnailPlaceholder()
                    }
```

**Change 3:** Extract the placeholder into its own composable to avoid duplication. Add this at the bottom of the file (before the final closing brace or alongside other private composables):

Actually, looking at the file structure — ChatInput is a single composable function. Extract the placeholder as a private composable. Add after the `ChatInput` function's closing brace:

```kotlin
@Composable
private fun ThumbnailPlaceholder() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = null,
            tint = AccentBlue,
            modifier = Modifier.size(22.dp)
        )
    }
}
```

And remove the now-unused `Icon` import if it's only used there (it's also used for `Send`, `Close`, `AttachFile` — keep it).

- [ ] **Step 2: Verify compilation**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/smartphoneaichat/ui/components/ChatInput.kt
git commit -m "feat: show real image thumbnail in ChatInput when Attachment has imageUri"
```

---

## Self-Review Checklist

**1. Spec coverage — acceptance criteria mapping:**

| Acceptance Criteria | Covered By |
|---|---|
| `Attachment` model updated with real image data field | Task 1 — adds `imageUri: String?` |
| Captured image saved to internal storage | Task 2 — `saveCapturedBitmap()` writes JPEG to `filesDir/captures/` |
| Image thumbnail appears in chat input after capture | Task 6 — `ChatInput` renders bitmap from `imageUri` |
| Image is attached as a user message ready for inference | Task 4 — `receiveCapturedImage()` creates pending USER message with attachment |
| Edge cases: camera denied, capture fails, storage full | Task 2 — `CaptureStatus.Error` with `captureError` string; existing permission denied UI from TICKET-2 |

**2. Placeholder scan:** No TBD, TODO, or vague instructions. All code is concrete.

**3. Type consistency:**
- `Attachment.imageUri: String?` — defined in Task 1, consumed in Tasks 4 and 6 ✓
- `CaptureStatus` enum — defined in Task 2, consumed in Task 3 ✓
- `ScannerViewModel.saveCapturedBitmap(onSaved: (String) -> Unit)` — defined in Task 2, consumed in Task 3 ✓
- `ChatViewModel.receiveCapturedImage(path: String)` — defined in Task 4, consumed in Task 5 ✓
- `ScannerScreen(onImageCaptured: (String) -> Unit, ...)` — defined in Task 3, consumed in Task 5 ✓
- `ChatScreen(savedStateHandle: SavedStateHandle, ...)` — defined in Task 5, consumed in Task 5 ✓