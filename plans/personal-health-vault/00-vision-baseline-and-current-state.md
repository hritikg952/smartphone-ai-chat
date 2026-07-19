# Vision Baseline and Current-State Gap

**Status:** Implementation-ready planning baseline; MG-01 governance review deferred

**Repository snapshot:** 2026-07-19 on `codex/health-vault-mg-01-baseline`

**Product scope:** English-only self-profile, private local-only prototype; no enforced minimum age

This document is the bridge between the current Android application and the
Personal Health Vault roadmap. It records what exists now, what may be reused,
what must be isolated or removed, and which mini-goal owns every material gap.
The [MG-01 governance packet](mg-01/README.md) controls claims, data use,
safety, retention, and release approval.

## Product vision

Build an Android-first personal health vault that helps a person enter,
organize, view, and retrieve their own health and wellness records offline.
Over time it may cover medications, prescriptions, providers, reports, lab
values, vitals, allergies, immunizations, symptoms, mood, insurance, emergency
information, controlled sharing, integrations, and carefully bounded AI.

The product north star is a securely unlocked, encrypted, local-first vault.
The private fast-track prototype is intentionally narrower and is not yet an
encrypted vault. Until MG-03 and MG-04 close, development and demonstrations
must use synthetic or developer-controlled data and must not imply production
security.

## Approved prototype boundary

| Dimension | Current direction |
|---|---|
| Intended use | Personal record organization and wellness support |
| User | Individual managing only their own records; no minimum age restriction enforced in the prototype |
| Jurisdiction | India |
| Distribution | Private prototype; no public or external Play track authorized |
| Storage | Local-only; production encryption, backup, and recovery are deferred |
| Authentication | Prototype username/password flow; not an encryption claim |
| Network | No health-data backend, cloud OCR, remote analytics, sync, or sharing service |
| AI | Existing chat/model capability preserved but isolated from vault records |
| OCR | Future on-device adapter; draft output requires explicit user review |
| Integrations | Health Connect, ABDM/ABHA, wearables, iOS, Wallet, and cloud services deferred |

## Explicit non-goals

The prototype is not a clinical system of record, diagnostic tool, treatment or
dosage recommender, drug-interaction engine, emergency triage service, medical
device, provider portal, or substitute for professional care. It must not claim
encryption, zero knowledge, HIPAA/DPDP compliance, ABDM certification, or
government affiliation without approved evidence.

## Current repository baseline

The repository is a Kotlin/Jetpack Compose, single-activity Android app with
manual dependency injection and Clean Architecture-style packages. Its live
journey is still an AI chat/scanner prototype, not a health vault.

### Verified implementation snapshot

| Area | Current implementation | Evidence | Health-vault implication |
|---|---|---|---|
| Application identity | Package/application ID `com.smartphoneaichat`; visible label “AI Chat” | [`app/build.gradle.kts`](../../app/build.gradle.kts), [`strings.xml`](../../app/src/main/res/values/strings.xml) | Rename visible identity in MG-02; package decision remains D-002 |
| App shell | One `MainActivity`, Compose `NavHost`, bottom navigation | [`MainActivity.kt`](../../app/src/main/java/com/smartphoneaichat/MainActivity.kt) | Retain single-activity Compose approach; replace destinations/session model |
| Routes | Chat, Scanner, and placeholder Medicine Data | [`Screen.kt`](../../app/src/main/java/com/smartphoneaichat/ui/navigation/Screen.kt) | These are legacy routes, not the approved vault information architecture |
| Dependency composition | Application-owned lazy `AppContainer`; manual factories | [`App.kt`](../../app/src/main/java/com/smartphoneaichat/App.kt), [`AppContainer.kt`](../../app/src/main/java/com/smartphoneaichat/di/AppContainer.kt) | Retain constructor injection; define explicit lifetimes and feature boundaries |
| Presentation state | `ChatViewModel` owns conversations, models, downloads, attachments, dialogs, and notifications | [`ChatViewModel.kt`](../../app/src/main/java/com/smartphoneaichat/presentation/viewmodel/ChatViewModel.kt) | Do not turn it into an app-wide vault ViewModel; isolate behind legacy boundary |
| Persistence | Conversations use `InMemoryConversationRepository` | [`InMemoryConversationRepository.kt`](../../app/src/main/java/com/smartphoneaichat/data/conversation/InMemoryConversationRepository.kt) | No durable structured health storage exists |
| Local model | LiteRT-LM engine with models downloaded from Hugging Face | [`HuggingFaceModelFileManager.kt`](../../app/src/main/java/com/smartphoneaichat/data/model/HuggingFaceModelFileManager.kt) | Preserve as optional legacy capability; never make model availability a vault prerequisite |
| Client credential | `HF_TOKEN` is compiled into generated `BuildConfig` | [`app/build.gradle.kts`](../../app/build.gradle.kts) | Release-blocking credential pattern; isolate and remove before health distribution |
| Dependencies | LiteRT-LM uses `latest.release`; CameraX is bundled | [`app/build.gradle.kts`](../../app/build.gradle.kts) | Pin approved versions and remove unused legacy dependencies during migration |
| Permissions | `INTERNET` and `CAMERA` are declared | [`AndroidManifest.xml`](../../app/src/main/AndroidManifest.xml) | Core vault must work offline; each retained permission needs a declared purpose |
| Android backup | `android:allowBackup="true"` | [`AndroidManifest.xml`](../../app/src/main/AndroidManifest.xml) | Conflicts with D-008/prototype constraints; disable before any health-record workflow |
| Camera capture | JPEGs are written to app-private `filesDir/captures`; cleanup runs only when another capture is saved | [`ScannerViewModel.kt`](../../app/src/main/java/com/smartphoneaichat/presentation/viewmodel/ScannerViewModel.kt) | Not an approved document vault; synthetic images only until MG-04/MG-09 |
| Scanner UX | Requests camera immediately on route entry and labels capture as “Analyze” without OCR | [`ScannerScreen.kt`](../../app/src/main/java/com/smartphoneaichat/ui/screens/ScannerScreen.kt) | Must not imply medical or OCR analysis; later replace with purpose-specific import/review flow |
| Automated tests | JVM coverage exists for chat domain, use cases, repository, IDs, state, and ViewModel; no `androidTest` lane | [`app/src/test`](../../app/src/test) | Reuse testing patterns; add architecture, session, navigation, persistence, security, and emulator coverage |

### Assets worth retaining

- Kotlin, coroutines, `StateFlow`, Compose, Material 3, and the single-activity
  approach.
- Domain interfaces, constructor injection, immutable state, value objects,
  aggregate methods, UUID generation, and use-case orchestration.
- Notification event pattern and deterministic fakes.
- JUnit 5, MockK, Turbine, and coroutine-test conventions.
- Dark theme primitives where they pass accessibility and adaptive-layout
  requirements.
- LiteRT-LM only as an optional future assistant adapter after MG-16 safety and
  grounding gates.

### Components to isolate, replace, or remove

- Isolate `ChatScreen`, `ChatUiState`, `ChatViewModel`, conversation models,
  chat repository, model menus, and model download behind a legacy feature
  boundary.
- Replace Chat/Scanner/Medicine Data as the primary navigation shell.
- Replace in-memory chat persistence with profile-scoped health repository
  contracts and, later, approved encrypted implementations. Do not repurpose
  `Conversation` or `Message` as health models.
- Replace the scanner capture directory with a lifecycle-controlled document
  import/store boundary; OCR output remains a derived draft.
- Remove the client Hugging Face token path, verify/rotate exposed credentials,
  and pin dependency versions before any health release.
- Remove permissions and dependencies that are not required by an enabled,
  reviewed feature.

## Gap and ownership matrix

Priority describes migration order and risk, not permission to bypass an
earlier exit gate.

| Priority | Gap | Current risk | Owning plan | Proof target |
|---|---|---|---|---|
| Deferred | MG-01 human ownership/approval | The prototype has no legal, privacy, clinical, or store sign-off and must not claim otherwise | MG-01 | Revisit before distribution, real-world use, off-device health processing, or compliance/medical claims |
| P0 | Backup enabled with no approved health backup design | App-private health files could enter an unintended system backup path | MG-03, MG-04, D-008 | Manifest/backup-rule tests and restore evidence |
| P0 | Client HF credential and unpinned AI dependency | Credential leakage and non-reproducible builds | MG-02, MG-16, MG-18 | Secret/artifact scan; signed model distribution and pinned dependency evidence |
| P0 | No vault security or encryption | Prototype cannot safely claim protection for real health records | MG-03, MG-04 | Threat model, key lifecycle, encrypted DB/files, recovery/deletion tests |
| P1 | Chat-centric startup/session/navigation | Protected health routes and lock state do not exist | MG-02, MG-06 | Session transition and locked-route navigation tests |
| P1 | No durable health schema/repositories | Records cannot survive process death or enforce profile/provenance boundaries | MG-04, MG-05, MG-19 | Repository contracts, migrations, profile-scope and deletion tests |
| P1 | No repeatable device/UI lane | JVM tests cannot prove Compose/device behavior | MG-20 | Documented AVD, connected tests, screenshots, logs |
| P1 | Camera capture is not a document lifecycle | Stale plaintext-like prototype images and ambiguous “Analyze” behavior | MG-04, MG-09 | Controlled import, file deletion, provenance, OCR review tests |
| P1 | App-wide chat ViewModel | New features could repeat god-state ownership | MG-02 | Per-feature state holders and dependency-rule tests |
| P2 | No consent/audit/provenance model | Optional processing and data origin cannot be demonstrated | MG-05 | Purpose-specific receipts, audit policy, provenance fixtures |
| P2 | No health-domain validation | Units, dates, source, chronology, and corrections are undefined in code | MG-08 to MG-11, MG-13 | Behavior-focused domain and repository tests |
| P2 | No private search/export boundary | Derived data and disclosure cannot be safely controlled | MG-14, MG-15 | Leakage review, byte-level export tests, deletion cascades |
| P3 | No integrations or grounded assistant | Roadmap capabilities are absent but must remain optional | MG-12, MG-16 | Consent, contract, safety, offline-fallback, and deletion evidence |

## Target architecture direction

```text
Compose feature UI
        ↓
feature state holder → feature use cases → domain repository contracts
                                              ↓
               local repositories / document store / vault session
                                              ↓
         approved database, files, security, and optional adapters
```

The prototype may initially use local implementations behind these contracts,
but it must not describe unencrypted storage as secure. MG-03/MG-04 later replace
or harden implementations without requiring feature/UI rewrites.

Suggested module direction after MG-02 measures build and ownership costs:

```text
:app
:core:model        :core:domain       :core:database
:core:security     :core:files        :core:ui
:core:testing      :core:search       :core:ai
:feature:home      :feature:profiles  :feature:emergency
:feature:medications  :feature:reports  :feature:vitals
:feature:journal   :feature:insurance :feature:sharing
:integration:healthconnect
```

The exact Gradle split is a decision, not a requirement. Dependency direction,
feature ownership, replaceable adapters, and test seams are required.

## Migration invariants

1. Keep the app buildable after every vertical slice; avoid a big-bang rewrite.
2. Use MG-01 as a development reference; its deferred governance review does
   not block private prototype implementation.
3. Use synthetic/developer-controlled data until the security and persistence
   gates authorize real sensitive records.
4. Preserve legacy chat/model behavior behind an internal boundary until MG-18
   decides migration/removal; never import chat as a health record.
5. The core vault must launch, navigate, store supported local records, and run
   its tests with AI/model download disabled and without network access.
6. UI and feature code depend on domain contracts, never directly on Room,
   files, CameraX, OCR, LiteRT, Health Connect, or a cloud SDK.
7. Every future record is profile-scoped and carries stable identity,
   provenance, creation/update time, and deletion semantics.
8. Deletion includes source records, documents, thumbnails, OCR/search/AI
   derivatives, temporary exports, and future queued work.
9. Claims, data inventory, permissions, vendors, and store declarations must be
   updated when runtime behavior changes.
10. Every behavior change follows the repository's TDD red → green workflow at
    a user-confirmed public seam.

## Success definitions

### Private prototype success

- Visible identity and primary navigation have pivoted to Personal Health Vault.
- A single self-profile can exercise the selected local record workflow using
  synthetic data.
- Local username/password demonstrates the session flow without being described
  as encryption.
- Android backup is disabled, health data is not transmitted, and diagnostics
  contain no health content.
- Legacy chat remains intact but outside the main journey.
- OCR, integrations, sharing, and AI-to-vault access remain disabled until their
  later gates.

### Product north-star success

A user can create and safely unlock an encrypted local vault, manage authorized
profiles, record and retrieve core health data offline, expose only chosen
emergency fields, verify imported/OCR data, search privately, export a granular
packet, and use optional grounded assistance without AI becoming the source of
truth. This definition is not the private prototype's completion claim.

## Baseline completion record

- [x] Product vision and prototype/non-prototype boundaries are separated.
- [x] Current code, permissions, storage, credentials, routes, and test posture
  are recorded with repository evidence.
- [x] Reusable assets and legacy isolation/removal targets are explicit.
- [x] Every material gap has an owning mini-goal and proof target.
- [x] Migration invariants and prototype/north-star success definitions are
  documented.
- [x] MG-01 development assumptions reflect the owner's current direction.
- [ ] MG-01 reviewers approve the intended use and high-risk assumptions.
  Deferred until the owner plans distribution or real-world use.
- [ ] Owners and due dates are assigned to open high-severity governance items.
  Deferred and non-blocking for private prototype development.

This baseline is complete as a planning artifact and authorizes proceeding with
private prototype development. It does not authorize claims of legal
compliance, medical approval, clinical validation, or release readiness.
