# MG-04 encrypted persistence lifecycle

**Status:** Implemented prototype boundary  
**Date:** 2026-07-20

MG-04 adds the first durable local health-data stores behind the MG-03 vault
cipher. The implementation is deliberately narrow: structured record content
and document bytes persist encrypted on disk, remain unavailable while locked,
and expose repository contracts that can later be backed by Room/SQLCipher.

## Structured records

- Public contract: `HealthRecordRepository`.
- Concrete implementation: `EncryptedHealthRecordRepository`.
- Every write includes stable `id`, `profileId`, record `type`, timestamps,
  provenance, schema version, and plaintext bytes supplied by the caller.
- The repository encrypts the record body with `VaultCipher` using associated
  data bound to `profileId`, record ID, and schema version.
- The backing file is `health-records.v1` under the app vault storage root.
- Reads, writes, listing, and deletion are profile-scoped.
- Listing sorts by most recent `updatedAtEpochMillis` and supports limit/offset
  pagination.
- If the vault is locked, save returns `Locked` and reads/listing return no
  plaintext records.

## Document vault

- Public contract: `EncryptedDocumentStore`.
- Concrete implementation: `LocalEncryptedDocumentStore`.
- Document bytes are encrypted through `VaultCipher` with associated data bound
  to profile ID and document ID.
- Published blob filenames are opaque SHA-256-derived names, not user names,
  document labels, or health terms.
- The document index maps profile/document IDs to the opaque blob and MIME type.
- Current MIME allowlist: PDF, JPEG, PNG, and plain text.
- The prototype enforces a default 25 MiB per-document limit.
- Delete removes the encrypted blob and index entry.

## Coordination

`VaultStorageCoordinator` coordinates document imports with their associated
structured record. It imports the encrypted document first, then saves the
record. If the record write fails, it deletes the published document blob and
index entry before returning failure. This prevents a known half-published file
case in the prototype.

This is not a full database transaction manager. Crash recovery, durable
outbox, multi-step migration rollback, and power-loss simulation remain future
MG-04 hardening work.

## Backup and recovery

- Public contract: `VaultBackupPolicy`.
- Concrete implementation: `PrototypeVaultBackupPolicy`.
- Platform backup remains disabled by manifest/data-extraction configuration.
- Export returns disabled.
- Restore returns unavailable.

No recovery material, backup envelope, cloud sync payload, or restore preview is
created in this prototype slice.

## Known limitations

- D-007 accepts this encrypted file-backed prototype; Room/SQLCipher remains
  deferred until schema and migration needs justify it.
- Metadata such as profile ID, record ID, type, document ID, and MIME type is
  still used by local indexes. Future threat modeling must decide which metadata
  should also be encrypted or blinded.
- Atomic file replacement is used for current writes, but full kill-process and
  storage-full simulation is still required.
- Thumbnail, OCR, parser isolation, derivative cleanup, storage quotas, and
  support bundles are not implemented yet.
