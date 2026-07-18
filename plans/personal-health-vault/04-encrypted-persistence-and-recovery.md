# MG-04 — Encrypted Persistence, File Vault, Backup, and Recovery

## Outcome

Provide durable, transactional, profile-scoped storage for structured health data and documents without plaintext artifacts or silent data loss.

## Dependencies

MG-02, MG-03; decisions D-007 and D-008.

## Scope

- Encrypted database, encrypted file store, schema migrations, atomic repository operations, data provenance, secure deletion behavior, backup/export format, restoration, corruption handling, and storage quotas.

## Core schema conventions

Every primary record includes a stable ID, `profileId`, created/updated timestamps, provenance/source, schema version, and deletion/sync metadata where applicable. Derived records include source IDs, extractor/rule/model version, confidence, user verification state, and invalidation state.

Use normalized tables for profiles, emergency data, medications/regimens/doses, prescriptions, providers, documents/pages, extracted observations, vitals, allergies, immunizations, journal entries, insurance, claims, consent receipts, audit events, share manifests, and search tokens. Large binaries never live as Room blobs.

## File-vault rules

- Stream import into a temporary encrypted file; verify size/type/hash; atomically publish only after DB commit coordination.
- Generate encrypted thumbnails; strip unnecessary EXIF/location metadata by default.
- Use opaque IDs, not health terms or user names, in filenames.
- Enforce allowlisted MIME types, file-size/page limits, decompression limits, and safe parser isolation.
- Track ownership/reference counts; orphan cleanup must never delete referenced files.
- Ensure delete removes thumbnails, OCR/AI derivatives, search entries, temp files, and pending work.

## Work packages

1. Evaluate database-encryption options for maintenance, Room compatibility, migration safety, memory behavior, licensing, and device coverage; record D-007.
2. Define schema v1 and repository interfaces by aggregate, not one generic database service.
3. Build encrypted database opening around `VaultSession`; the DB cannot open while locked.
4. Build streaming `EncryptedDocumentStore` with authenticated chunks or approved equivalent.
5. Implement transaction/outbox coordination for DB/file operations and crash recovery.
6. Add migration harness with golden databases/ciphertext from every released schema/crypto version.
7. Define backup format: manifest, schema/crypto version, encrypted records/files, integrity tree/hash, and recovery envelope; never include live Keystore keys.
8. Disable/exclude unsafe platform backup paths until restore is explicitly proven.
9. Implement restore preview, available-space checks, integrity validation, conflict policy, and atomic activation.
10. Add corruption detection, non-destructive safe mode, support bundle with no health content, and user-controlled reset.

## Tests

- Repository contract, concurrency, pagination, cascade, and profile-isolation tests.
- Kill-process/power-loss simulation across imports, deletes, migrations, backup, and restore.
- Confirm no plaintext fragments in DB, WAL/SHM, temp/cache, thumbnails, filenames, backups, or logs.
- Large document, storage-full, corrupt file, wrong key, old version, interrupted rotation, and rollback tests.

## Acceptance criteria

- [ ] Real records and documents persist across process/device restart only in encrypted form.
- [ ] Database and file vault remain inaccessible while locked.
- [ ] Schema/crypto migrations are forward-tested with retained fixtures.
- [ ] Backup/restore is authenticated and tested, or backup is safely disabled and disclosed.
- [ ] Deletion covers all derived artifacts and queued jobs.
- [ ] Storage failures never publish half-imported records.

## Exit gate

Feature teams receive stable encrypted repositories, migration fixtures, and a documented record/file lifecycle.

