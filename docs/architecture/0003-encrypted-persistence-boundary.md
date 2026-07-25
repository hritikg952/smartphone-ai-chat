# ADR 0003 — Encrypted persistence boundary

**Status:** Accepted for the private prototype  
**Date:** 2026-07-20

## Context

MG-04 needs durable local storage for structured health records and documents,
but D-007 had not yet approved a Room-compatible encrypted database stack.
Adding plaintext Room now would create the exact health-data exposure MG-04 is
meant to prevent, while choosing SQLCipher or another database layer without a
schema/migration spike would make future migrations harder.

## Decision

- Introduce domain contracts for profile-scoped structured records,
  out-of-row document storage, backup policy, and coordinated record/document
  imports.
- Store structured record bodies through the MG-03 `VaultCipher` boundary.
  The prototype backing file is append/replace style and stores versioned
  ciphertext plus routing metadata; plaintext record bodies are never written.
- Store document bytes as encrypted blobs under opaque filenames. The document
  index keeps the profile/document mapping and MIME type; large document bytes
  never live inside structured records.
- Require the vault to be unlocked for record writes, record reads, document
  imports, and document reads. Locked encryption/decryption results fail closed.
- Coordinate document imports with record writes. If the document blob publishes
  but the record write fails, the coordinator deletes the published blob and
  index entry before returning failure.
- Keep Android platform backup disabled and expose a prototype backup policy
  that reports export disabled and restore unavailable. Backup/recovery format
  design remains a later reviewed change.
- Defer Room/SQLCipher until the app needs richer query capability, schema
  migrations, and released migration fixtures.

## Consequences

- MG-05 and feature teams can write against stable local repository contracts
  without depending on a concrete database choice.
- Health record and document content can survive process restart only while
  encrypted at rest and can be read only after vault unlock.
- The prototype storage is intentionally simple. It is not the final normalized
  Room schema, does not provide full transactional database semantics, and must
  be migrated or replaced before broad feature expansion.
- Backup, restore, sync, key rotation, rollback resistance, storage quotas,
  thumbnail/OCR derivative lifecycle, and golden migration fixtures remain
  explicit follow-up work rather than implied behavior.
