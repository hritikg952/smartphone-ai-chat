# ADR 0002 — Vault cryptographic boundary

**Status:** Accepted for the private prototype  
**Date:** 2026-07-20

## Context

MG-03 requires the Health Vault to keep its data-encryption key (DEK) out of
persistent plaintext storage and to expose plaintext only inside an authorized,
process-local session. Prototype decision D-005 selects a local username and
password rather than Android biometric or device-credential authentication. No
recovery or rotation workflow is available under D-006.

## Decision

- Generate a new 256-bit DEK with `SecureRandom`. The DEK is never derived from
  the password and is never written to preferences, saved state, logs, or
  intents.
- Generate a non-exportable 256-bit AES wrapping key in Android Keystore. Wrap
  the DEK with AES-GCM and bind the envelope to the constant, versioned context
  `personal-health-vault:key-envelope:v1`.
- Authenticate the prototype username/password with a unique 128-bit salt and
  PBKDF2-HMAC-SHA256 verifier using 310,000 iterations. Compare verifier bytes
  in constant time. Persist only the username, salt, work factor, verifier, and
  wrapped DEK envelope.
- Treat password authentication as an application policy gate before Keystore
  unwrap. The Keystore key itself does not request biometric/device-credential
  authorization because that would conflict with D-005. Consequently, this
  design prevents offline key export but does not defend against arbitrary code
  execution inside the unlocked application/OS process.
- Hold a private copy of the unwrapped DEK only in `DefaultVaultSession`.
  `lock()`, invalidation, and destruction overwrite the held byte array before
  releasing it. Process restart begins locked.
- Encrypt future record payloads with AES-256-GCM, a fresh 96-bit nonce, and a
  128-bit authentication tag. Associated data uses length-delimited profile ID,
  record ID, schema version, and cipher version fields, preventing record or
  profile substitution.
- Version both key envelopes and record ciphertext. Unknown versions and
  authentication failures fail closed without deleting stored ciphertext.
- Lock immediately from `MainActivity.onStop()`. There is no inactivity timer
  in the private prototype.
- Apply `FLAG_SECURE` to the sensitive Activity and disable Android application
  backup. The active vault does not write secrets to the clipboard or saved
  instance state.
- Recovery and key rotation are unavailable. Setup and unlock screens state
  that a lost password cannot be recovered.

## Alternatives considered

- Deriving the DEK directly from the password was rejected because short
  user-chosen passwords are unsuitable as data keys and cannot be rotated
  independently.
- Storing the DEK in SharedPreferences, even encrypted only by an app-known
  constant, was rejected because filesystem extraction would recover it.
- Requiring `BiometricPrompt` or the device credential was deferred by the
  accepted prototype access decision. It should replace or augment the local
  password gate before distribution.
- Adding a recovery-wrapped DEK was deferred because a partially implemented
  recovery path would expand the attack surface and create misleading UX.

## Consequences

- Offline extraction of app-private preferences does not reveal the DEK or
  record plaintext without the Android Keystore key.
- Wrong credentials, modified ciphertext, wrong associated data, missing keys,
  and unsupported versions fail closed.
- A fully compromised OS or arbitrary code executing as this application can
  invoke the Keystore key and is outside the protection boundary.
- PBKDF2 work must run off the main thread and its work factor must be reviewed
  and migrated as device performance and password guidance change.
- Biometric/device-credential authentication, rotation, recovery, hardware
  attestation requirements, and cryptographic version migration remain separate
  reviewed changes rather than silent extensions of v1.
