package com.smartphoneaichat.presentation.viewmodel

import android.app.Application
import com.smartphoneaichat.data.id.FakeIdGenerator
import com.smartphoneaichat.domain.model.value.ConversationId
import com.smartphoneaichat.domain.model.value.MessageId
import com.smartphoneaichat.domain.repository.ConversationRepository
import com.smartphoneaichat.domain.repository.IdGenerator
import com.smartphoneaichat.domain.repository.InferenceEngine
import com.smartphoneaichat.domain.repository.ModelFileManager
import com.smartphoneaichat.domain.usecase.DownloadModelUseCase
import com.smartphoneaichat.domain.usecase.LoadModelUseCase
import com.smartphoneaichat.domain.usecase.SendMessageUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        idGenerator: IdGenerator = FakeIdGenerator(),
        conversationRepository: ConversationRepository = mockk(relaxed = true),
        modelFileManager: ModelFileManager = mockk(relaxed = true),
        inferenceEngine: InferenceEngine = mockk(relaxed = true),
    ): ChatViewModel {
        every { modelFileManager.listDownloadedModelIds() } returns emptyList()
        coEvery { conversationRepository.getAll() } returns emptyList()

        return ChatViewModel(
            sendMessageUseCase = SendMessageUseCase(inferenceEngine, idGenerator),
            downloadModelUseCase = DownloadModelUseCase(modelFileManager),
            loadModelUseCase = LoadModelUseCase(modelFileManager),
            inferenceEngine = inferenceEngine,
            modelFileManager = modelFileManager,
            conversationRepository = conversationRepository,
            idGenerator = idGenerator,
            application = mockk(relaxed = true),
        )
    }

    @Test
    fun toggleSidebar_flipsIsSidebarOpen() = runTest(testDispatcher) {
        val vm = createViewModel()
        val initial = vm.state.value.isSidebarOpen

        vm.toggleSidebar()
        assertNotEquals(initial, vm.state.value.isSidebarOpen)
    }

    @Test
    fun closeSidebar_setsIsSidebarOpenToFalse() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.toggleSidebar()
        assertTrue(vm.state.value.isSidebarOpen)

        vm.closeSidebar()
        assertFalse(vm.state.value.isSidebarOpen)
    }

    @Test
    fun selectConversation_setsActiveConversationId() = runTest(testDispatcher) {
        val vm = createViewModel()
        val id = ConversationId("conv-test")

        vm.selectConversation(id)
        assertEquals(id, vm.state.value.activeConversationId)
    }

    @Test
    fun selectConversation_closesSidebar() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.toggleSidebar()

        vm.selectConversation(ConversationId("conv-test"))
        assertFalse(vm.state.value.isSidebarOpen)
    }

    @Test
    fun newConversation_createsConversationWithDefaultTitle() = runTest(testDispatcher) {
        val vm = createViewModel()
        val conversationsBefore = vm.state.value.conversations.size

        vm.newConversation()

        assertEquals(conversationsBefore + 1, vm.state.value.conversations.size)
        assertEquals("New Chat", vm.state.value.conversations.last().title)
    }

    @Test
    fun newConversation_setsActiveConversationToNewOne() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.newConversation()

        assertEquals(vm.state.value.conversations.last().id, vm.state.value.activeConversationId)
    }

    @Test
    fun newConversation_savesToRepository() = runTest(testDispatcher) {
        val repo = mockk<ConversationRepository>(relaxed = true)
        coEvery { repo.getAll() } returns emptyList()
        val vm = createViewModel(conversationRepository = repo)
        vm.newConversation()

        coVerify(timeout = 1000) { repo.save(any()) }
    }

    @Test
    fun deleteConversation_removesFromList() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.newConversation()
        val idToDelete = vm.state.value.activeConversationId!!
        val countBefore = vm.state.value.conversations.size

        vm.deleteConversation(idToDelete)

        assertEquals(countBefore - 1, vm.state.value.conversations.size)
    }

    @Test
    fun deleteLastConversation_createsFreshConversation() = runTest(testDispatcher) {
        val vm = createViewModel()
        val idToDelete = vm.state.value.activeConversationId!!

        vm.deleteConversation(idToDelete)

        assertEquals(1, vm.state.value.conversations.size)
        assertEquals("New Chat", vm.state.value.conversations.first().title)
    }

    @Test
    fun toggleThinkingExpanded_addsMessageIdToSet() = runTest(testDispatcher) {
        val vm = createViewModel()
        val msgId = MessageId("test-msg")

        vm.toggleThinkingExpanded(msgId)
        assertTrue(vm.state.value.thinkingExpandedIds.contains(msgId))
    }

    @Test
    fun toggleThinkingExpanded_removesMessageIdFromSet() = runTest(testDispatcher) {
        val vm = createViewModel()
        val msgId = MessageId("test-msg")
        vm.toggleThinkingExpanded(msgId)

        vm.toggleThinkingExpanded(msgId)
        assertFalse(vm.state.value.thinkingExpandedIds.contains(msgId))
    }

    @Test
    fun confirmDeleteModel_setsDeleteConfirmationState() = runTest(testDispatcher) {
        val vm = createViewModel()

        vm.confirmDeleteModel("gemma3-1b")
        assertTrue(vm.state.value.showDeleteConfirmation)
        assertEquals("gemma3-1b", vm.state.value.deleteTargetModelId)
    }

    @Test
    fun cancelDeleteModel_clearsDeleteConfirmationState() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.confirmDeleteModel("gemma3-1b")

        vm.cancelDeleteModel()
        assertFalse(vm.state.value.showDeleteConfirmation)
        assertNull(vm.state.value.deleteTargetModelId)
    }

    @Test
    fun dismissModelSelector_clearsSelectorState() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.dismissModelSelector()
        assertFalse(vm.state.value.showModelSelector)
        assertTrue(vm.state.value.modelSelectorModels.isEmpty())
    }
}