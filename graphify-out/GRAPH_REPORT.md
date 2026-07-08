# Graph Report - .  (2026-07-04)

## Corpus Check
- Corpus is ~40,984 words - fits in a single context window. You may not need a graph.

## Summary
- 398 nodes · 603 edges · 24 communities (17 shown, 7 thin omitted)
- Extraction: 81% EXTRACTED · 19% INFERRED · 0% AMBIGUOUS · INFERRED: 115 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Message Sending & Streaming|Message Sending & Streaming]]
- [[_COMMUNITY_Conversation Repository|Conversation Repository]]
- [[_COMMUNITY_Architecture Docs & Design Rationale|Architecture Docs & Design Rationale]]
- [[_COMMUNITY_Model File Management|Model File Management]]
- [[_COMMUNITY_Download & Load Use Cases|Download & Load Use Cases]]
- [[_COMMUNITY_App Entry & Engine Init|App Entry & Engine Init]]
- [[_COMMUNITY_Notifications & Chat Input|Notifications & Chat Input]]
- [[_COMMUNITY_ViewModel & State Flow|ViewModel & State Flow]]
- [[_COMMUNITY_Message Model & Attachments|Message Model & Attachments]]
- [[_COMMUNITY_ID Generation|ID Generation]]
- [[_COMMUNITY_Project Docs & CICD|Project Docs & CI/CD]]
- [[_COMMUNITY_Repository Tests|Repository Tests]]
- [[_COMMUNITY_ID Generator Tests|ID Generator Tests]]
- [[_COMMUNITY_UI State Tests|UI State Tests]]
- [[_COMMUNITY_UI Components|UI Components]]
- [[_COMMUNITY_Load Model Tests|Load Model Tests]]
- [[_COMMUNITY_Download Model Tests|Download Model Tests]]
- [[_COMMUNITY_Conversation Title Service|Conversation Title Service]]
- [[_COMMUNITY_OpenCode Plugin Config|OpenCode Plugin Config]]

## God Nodes (most connected - your core abstractions)
1. `MessageText` - 45 edges
2. `ChatViewModel` - 30 edges
3. `Message` - 20 edges
4. `ConversationId` - 20 edges
5. `Conversation` - 19 edges
6. `ConversationTest` - 19 edges
7. `ChatViewModelTest` - 18 edges
8. `Architecture Markdown Document` - 18 edges
9. `SendMessageUseCaseTest` - 17 edges
10. `HuggingFaceModelFileManager` - 15 edges

## Surprising Connections (you probably didn't know these)
- `Architecture HTML Document` --semantically_similar_to--> `Architecture Markdown Document`  [INFERRED] [semantically similar]
  ARCHITECTURE.html → ARCHITECTURE.md
- `Agent Guide` --references--> `CI/CD Release Pipeline`  [EXTRACTED]
  AGENTS.md → .github/workflows/release.yml
- `Agent Guide` --references--> `Architecture Markdown Document`  [EXTRACTED]
  AGENTS.md → ARCHITECTURE.md
- `Architecture Markdown Document` --references--> `HuggingFace Model Download Pipeline`  [EXTRACTED]
  ARCHITECTURE.md → AGENTS.md
- `Code Review Report` --references--> `Future Plans`  [EXTRACTED]
  CODE_REVIEW.md → Future_Plans.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Clean Architecture Layer Dependencies** — chatui_state, chat_viewmodel, isp_interfaces, litert_inference_engine [INFERRED 0.85]
- **Six Migration Phases from Monolith to Clean Architecture** — phase0_id_generator_value_objects, phase1_package_structure_interfaces, phase2_conversation_encapsulation, phase3_use_cases, phase4_dependency_injection, phase5_conversation_repository [EXTRACTED 1.00]
- **ISP Interface Triad (InferenceEngine, ModelFileManager, ConversationRepository)** — inference_engine, model_file_manager, conversation_repository [EXTRACTED 1.00]

## Communities (24 total, 7 thin omitted)

### Community 0 - "Message Sending & Streaming"
Cohesion: 0.08
Nodes (9): MessageText, Flow, SendMessageUseCase, FakeInferenceEngine, Flow, String, MessageTextTest, ConversationTitleServiceTest (+1 more)

### Community 1 - "Conversation Repository"
Cohesion: 0.08
Nodes (15): InMemoryConversationRepository, List, Conversation, create(), Boolean, String, ConversationId, ConversationRepository (+7 more)

### Community 2 - "Architecture Docs & Design Rationale"
Cohesion: 0.09
Nodes (34): Architecture HTML Document, Architecture Markdown Document, ChatViewModelFactory, Clean Architecture Refactoring, Conversation Aggregate Root, ConversationRepository Interface, ConversationTitleService Pure Domain Service, ConversationId Inline Value Class (+26 more)

### Community 3 - "Model File Management"
Cohesion: 0.11
Nodes (15): HuggingFaceModelFileManager, Boolean, List, Result, String, Unit, ModelInfo, Boolean (+7 more)

### Community 4 - "Download & Load Use Cases"
Cohesion: 0.09
Nodes (9): DownloadModelUseCase, Result, String, Unit, Result, String, Unit, LoadModelUseCase (+1 more)

### Community 5 - "App Entry & Engine Init"
Cohesion: 0.07
Nodes (17): Boolean, Flow, String, LiteRtInferenceEngine, AppContainer, ChatViewModelFactory, InferenceEngine, Boolean (+9 more)

### Community 6 - "Notifications & Chat Input"
Cohesion: 0.09
Nodes (22): AppNotificationEvent, AppNotificationManager, Error, Success, ChatInput(), Modifier, String, ModelLoaderDialog() (+14 more)

### Community 7 - "ViewModel & State Flow"
Cohesion: 0.12
Nodes (7): AndroidViewModel, String, modelInfoById(), ChatViewModel, String, Job, StateFlow

### Community 8 - "Message Model & Attachments"
Cohesion: 0.11
Nodes (5): List, Attachment, ChatRole, Message, ConversationTest

### Community 9 - "ID Generation"
Cohesion: 0.12
Nodes (5): UuidIdGenerator, MessageId, IdGenerator, FakeIdGenerator, MessageIdTest

### Community 10 - "Project Docs & CI/CD"
Cohesion: 0.15
Nodes (16): Agent Guide, ChatViewModel God Class, ChatUiState Data Class, Code Review Issues, Code Review Report, Medicine Scanning and Prescription Analysis, Future Plans, CI/CD Release Pipeline (+8 more)

### Community 14 - "UI Components"
Cohesion: 0.22
Nodes (7): ChatBubble(), Boolean, Modifier, Boolean, Modifier, String, ThinkingSection()

## Knowledge Gaps
- **16 isolated node(s):** `@opencode-ai/plugin`, `ChatRole`, `CI/CD Release Pipeline`, `Architecture HTML Document`, `API Key Setup Guide` (+11 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **7 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ChatViewModel` connect `ViewModel & State Flow` to `Conversation Repository`, `Download & Load Use Cases`, `App Entry & Engine Init`, `Notifications & Chat Input`, `Message Model & Attachments`, `ID Generation`, `UI State Tests`?**
  _High betweenness centrality (0.207) - this node is a cross-community bridge._
- **Why does `MessageText` connect `Message Sending & Streaming` to `Message Model & Attachments`, `Conversation Title Service`, `Repository Tests`, `ViewModel & State Flow`?**
  _High betweenness centrality (0.173) - this node is a cross-community bridge._
- **Why does `ChatScreen()` connect `Notifications & Chat Input` to `Conversation Repository`, `App Entry & Engine Init`, `UI Components`, `ViewModel & State Flow`?**
  _High betweenness centrality (0.152) - this node is a cross-community bridge._
- **Are the 42 inferred relationships involving `MessageText` (e.g. with `.attachImage()` and `.sendMessage()`) actually correct?**
  _`MessageText` has 42 INFERRED edges - model-reasoned connections that need verification._
- **Are the 15 inferred relationships involving `Message` (e.g. with `.invoke()` and `.attachImage()`) actually correct?**
  _`Message` has 15 INFERRED edges - model-reasoned connections that need verification._
- **Are the 8 inferred relationships involving `ConversationId` (e.g. with `.createWithBlankString_throwsIllegalArgumentException()` and `.createWithNonBlankString_succeeds_valueReturned()`) actually correct?**
  _`ConversationId` has 8 INFERRED edges - model-reasoned connections that need verification._
- **What connects `@opencode-ai/plugin`, `ChatRole`, `CI/CD Release Pipeline` to the rest of the system?**
  _22 weakly-connected nodes found - possible documentation gaps or missing edges._