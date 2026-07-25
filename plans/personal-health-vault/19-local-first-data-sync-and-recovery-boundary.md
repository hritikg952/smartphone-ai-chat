# MG-19 — Local-First Data, Optional Cloud, Sync, and Recovery Boundary

## Outcome

Make the Android device the authoritative health vault while preserving a deliberate, replaceable path for future encrypted backup, multi-device sync, recovery, sharing, and account services.

## Dependencies

MG-02 to MG-05; decisions D-004, D-006, D-007, D-008, and D-019.

## Product decision

There is no health-data backend in the prototype. The local encrypted record
store and private encrypted document store are the only source of truth.
Username/password is local vault access for the prototype, not a cloud identity
or recovery system.

Supabase is not ruled out. It is a future implementation option for account/device registration, encrypted object storage, encrypted sync transport, rate limiting, or sharing metadata. It must not become the canonical plaintext store for health records merely because it is convenient.

## Android-to-web mental model

| Familiar web concept | Health Vault Android equivalent | Future option |
|---|---|---|
| Frontend | Compose UI, ViewModels, feature state | No change |
| Backend application/API | Domain use cases and repository contracts running in the app | A narrowly scoped sync/account service |
| Primary database | `HealthRecordRepository` backed by encrypted app-private local storage; Room/SQLite deferred by D-007 | Encrypted cloud replica, never a replacement without a new decision |
| Object storage | App-private encrypted `EncryptedDocumentStore` with opaque blob names | Encrypted backup/sync object store |
| Authentication | Local vault unlock session | Optional cloud account/device identity |
| Background jobs | WorkManager, persisted local job/outbox records | Network sync only when opted in |

## Required boundaries

1. UI and feature code may depend only on domain repository contracts; it must not know whether data is local, synced, or backed up.
2. `Local*Repository` implementations own the canonical database/file operations.
3. Future network code belongs behind `SyncTransport`, `BackupTransport`, and `AccountService` contracts. Do not expose a generic Supabase client to feature code.
4. Every persisted record receives a stable UUID, `profileId`, timestamps, schema version, provenance, deletion marker, and future sync revision fields from the outset.
5. Documents are separate encrypted objects referenced by opaque IDs. Never make health documents public URLs.
6. Keep a durable local operation/outbox model only when sync is actually introduced; do not create fake network queues in the prototype.
7. All future remote payloads must be versioned, authenticated, replay-safe, size-limited, and auditable without logging health content.

## Future service options

| Option | Fit | Trade-off |
|---|---|---|
| Remain device-only | Prototype and privacy-first single-device use | Loss/theft recovery depends on future export/backup design |
| User-created encrypted export | Strong privacy and simple infrastructure | User is responsible for retaining backup and recovery material |
| End-to-end encrypted backup service | Good recovery and privacy | Key recovery, integrity, deletion, and support flows are difficult |
| End-to-end encrypted multi-device sync | Best cross-device experience without provider plaintext | Conflict handling and device authorization are substantial product work |
| Conventional cloud record backend | Fastest collaboration and web access | Provider/backend can access plaintext; major privacy/compliance responsibility |

## Supabase decision guide

Supabase is suitable only after this boundary exists. If used later, prefer Postgres rows and Storage objects that contain encrypted envelopes/blobs plus minimal non-sensitive routing metadata. Supabase Auth can identify an account and Row Level Security can limit access, but neither replaces end-to-end encryption. Do not send raw report text, embeddings, conversation history, medication data, or document images to it by default.

Self-hosted or alternative providers remain valid because the app owns interfaces and encrypted payload formats, not a vendor-specific schema.

## AI/RAG data requirements

- `AiConversation`, `AiMessage`, `DocumentChunk`, `Embedding`, and `RetrievalCitation` are profile-scoped local records.
- Each chunk keeps document/page/location provenance, extraction version, user-review state, and deletion linkage.
- Vector/index artifacts are rebuildable derivatives, never the sole copy of source health data.
- Deleting a document/profile must remove or invalidate associated chunks, embeddings, cached prompts, citations, and queued sync records.
- No cloud model, embedding API, or remote RAG retrieval is introduced without explicit opt-in, an updated data map, and safety/privacy review.

## Work packages

1. Define `ProfileRepository`, feature repositories, `DocumentStore`, `VaultSession`, and future `SyncTransport`/`BackupTransport`/`AccountService` contracts.
2. Design schema IDs, revisions, tombstones, provenance, and opaque document references; test profile isolation.
3. Add local-only implementations and fakes; keep all production feature reads/writes local-first.
4. Create a threat model for encrypted backup and multi-device sync: key hierarchy, device enrollment/removal, recovery, rollback, replay, conflict policy, deletion, and lost-device handling.
5. Define an encrypted export/backup envelope and restoration test plan before enabling any backup path.
6. Run a small provider spike only when cloud work is funded: compare Supabase, self-hosted service, and object storage against the approved payload/threat model—not against generic CRUD convenience.
7. Design sync as a separate release: local write first, durable operation record, bounded retry with WorkManager, idempotency, conflict UX, and observable sync state.

## Tests

- Repository contract tests pass unchanged with local fakes and a future transport fake.
- Local reads/writes work in airplane mode and after process death.
- Every query and mutation is scoped by `profileId`.
- Fixtures prove deletion/invalidation cascades for documents, RAG artifacts, and conversation history.
- Before any cloud enablement: packet/payload inspection proves no plaintext health content, keys, or sensitive logs leave the device.
- Backup/restore and sync tests cover wrong key, replay, rollback, duplicate operations, conflicts, interrupted transfer, and removed device.

## Acceptance criteria

- [ ] The prototype has no required server, account, internet permission, or cloud health-data flow.
- [x] One local source of truth exists behind repository interfaces.
- [x] The schema supports multiple future family profiles without cross-profile queries.
- [ ] AI/RAG data is local, profile-scoped, attributable, and deletable.
- [x] A provider can be introduced or replaced without feature/UI rewrites.
- [x] Backup and sync remain disabled until their encryption, recovery, and test gates pass.

Progress note — 2026-07-20: MG-04 introduced local repository contracts and
prototype encrypted local implementations. Profile repository, AI/RAG records,
sync transports, encrypted backup envelope, and cloud provider work remain
future scope.

## Exit gate

Feature development can proceed against stable local contracts. A future cloud decision has an explicit security boundary, implementation sequence, and test obligations instead of becoming an accidental backend.
