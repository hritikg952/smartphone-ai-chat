# MG-03 vault lifecycle and credential behavior

**Status:** Implemented prototype behavior  
**Date:** 2026-07-20

This document records the expected runtime, install, and credential behavior for
the private Personal Health Vault prototype. It is intentionally product- and
QA-facing, so future recovery, biometric, password policy, sync, or legal work
can revisit the current behavior without rediscovering it from code.

## Current access model

The prototype uses a local username and password. There is no minimum age gate,
locale policy, recovery flow, password reset, biometric prompt, device credential
prompt, cloud account, or server account in MG-03.

The password is an application-level access gate. It is not the vault data key.
On vault creation, the app generates a random 256-bit data-encryption key (DEK),
wraps that DEK with an Android Keystore AES key, and stores only:

- username
- password-verifier salt
- password-verifier iteration count
- password verifier
- wrapped DEK envelope

The plaintext password is never intentionally persisted. The plaintext DEK is
held only in process memory while the vault is unlocked.

## Persisted and non-persisted state

| Item | Persisted after process restart | Persisted after uninstall | Notes |
|---|---:|---:|---|
| Onboarding-complete flag | Yes | No | App-private preferences; ignored unless a vault envelope exists. |
| Username | Yes | No | Stored with the verifier record. |
| Plaintext password | No | No | UI clears password after each completed create/unlock attempt. |
| Password verifier | Yes | No | PBKDF2-HMAC-SHA256 with a per-vault salt. |
| Plaintext vault DEK | No | No | Process-local only; zeroized on explicit lock/session transitions where code owns the byte array. |
| Wrapped vault DEK envelope | Yes | No | Stored in app-private preferences. |
| Android Keystore wrapping key | Yes, while app remains installed | Expected no | App-owned key alias is destroyed on explicit vault destroy and is not relied on across uninstall. |
| Future encrypted health records | Not implemented in MG-03 | No, under current no-backup policy | MG-04 must document actual record/file persistence. |

Android application backup is disabled for the prototype. The current expected
reinstall behavior is therefore a clean first-run vault setup, not restoration
of the previous local vault.

## Session states

| State | Meaning | User-facing route |
|---|---|---|
| `Absent` | No vault envelope is available in app storage. | Onboarding/create vault. |
| `Locked` | A vault exists, but the DEK is not in memory. | Unlock screen. |
| `Unlocked` | Credentials succeeded and the DEK is available to the process-local session. | Home/protected routes. |
| `Invalidated` | The stored envelope exists, but the Android Keystore wrapping key cannot unwrap it because the key was invalidated or removed. | Unlock flow reports key invalidated. |
| `Destroyed` | The vault session, stored envelope, and wrapping key were explicitly destroyed. | Onboarding/create vault. |

Protected routes must derive access from `VaultSessionState.Unlocked`, not from
UI booleans or navigation-only flags.

## Lifecycle behavior

### Fresh install

Expected behavior:

1. No vault envelope exists.
2. Session state starts as `Absent`.
3. App routes to onboarding.
4. User creates a local username/password.
5. On successful creation, the app writes the verifier and wrapped key envelope,
   unlocks the process-local session, marks onboarding complete, and routes to
   the protected app.

### Normal app restart

Expected behavior:

1. The wrapped envelope and verifier remain in app-private storage.
2. The plaintext DEK is not in memory because the process restarted.
3. Session state starts as `Locked`.
4. App routes to unlock.
5. User must enter the same username and password to unwrap the DEK and enter
   the vault.

### Backgrounding or switching away

Expected behavior:

1. `MainActivity.onStop()` locks the vault immediately.
2. The session clears its in-memory DEK.
3. Protected routes are no longer accessible.
4. Returning to the app requires unlock again.

There is no inactivity timer in MG-03. Backgrounding is the only automatic lock
trigger.

### App killed by the OS

Expected behavior:

1. Process memory is lost, including the plaintext DEK.
2. Stored verifier and wrapped envelope remain if the app is still installed.
3. Next launch behaves like a normal app restart and routes to unlock.

### Device reboot

Expected behavior:

1. Process memory is lost.
2. App-private verifier and wrapped envelope remain if the app is still
   installed.
3. Android Keystore should retain the wrapping key for the installed app.
4. Next launch routes to unlock.

### App uninstall and reinstall on the same phone

Expected behavior:

1. Android removes app-private preferences, including the verifier and wrapped
   vault envelope.
2. Android backup should not restore vault state because backup is disabled and
   data-extraction rules exclude app data.
3. The previous app-owned Keystore alias is not relied on after uninstall.
4. Reinstalled app behaves like a fresh install.

User consequence: the previous local vault is unrecoverable after uninstall
unless a future explicit export, backup, recovery, or sync design is added.

### App data cleared from Android settings

Expected behavior is the same as uninstall/reinstall for vault purposes:

1. Stored verifier and wrapped envelope are removed.
2. Onboarding-complete state is removed.
3. App returns to fresh onboarding.
4. Previous local vault data is unrecoverable under MG-03.

### App update

Expected behavior:

1. App-private preferences and Android Keystore alias remain available.
2. Process-local DEK is not assumed to survive.
3. User may need to unlock again after update/restart.
4. Unknown envelope or record cipher versions must fail closed until a reviewed
   migration exists.

## Credential behavior

### Creating credentials

Inputs:

- username must be nonblank
- password must be nonblank

Expected success behavior:

1. Create a verifier from username/password using PBKDF2-HMAC-SHA256.
2. Generate a random DEK.
3. Wrap the DEK with Android Keystore.
4. Persist verifier metadata and wrapped envelope.
5. Unlock the session.
6. Clear the password from UI state after the operation completes.

Expected failure behavior:

- blank username or blank password returns invalid credentials
- existing vault returns already exists
- Keystore or storage failure returns unavailable
- password input is cleared after a completed attempt

There is no password-strength rule in MG-03. This is a known prototype
limitation.

### Unlocking

Inputs:

- username
- password

Expected success behavior:

1. Load stored verifier and wrapped envelope.
2. Verify the username/password against the stored verifier.
3. If valid, unwrap the DEK through Android Keystore.
4. Store a process-local copy of the DEK in `VaultSession`.
5. Clear password input after completion.
6. Route to the protected app.

Expected failure behavior:

- wrong username or password returns invalid credentials
- missing or malformed vault storage returns unavailable
- missing, invalidated, or unusable Keystore key returns key invalidated or
  unavailable depending on the platform failure
- password input is cleared after a completed attempt
- protected routes remain unavailable

MG-03 does not include lockout, rate limiting, backoff, password hints, password
reset, or recovery.

### Locking

Expected behavior:

1. Clear the in-memory DEK.
2. Keep the verifier and wrapped envelope in storage.
3. Keep the username in UI state unless changed by the user.
4. Clear password input and transient errors on explicit lock from the app UI.
5. Route protected content back to unlock.

Locking is reversible with the same username/password while the stored envelope
and Keystore wrapping key remain valid.

### Lost password

Expected behavior:

1. User cannot unlock the vault.
2. The app cannot recover or reset the password.
3. The app cannot derive the plaintext DEK from stored data.
4. The only current way to use the app again is a destructive app-data clear or
   reinstall. A first-class reset flow is not implemented in MG-03.

User consequence: existing encrypted vault data is unrecoverable.

### Password change

Not implemented in MG-03.

Future expected design work:

- authenticate with the current password
- unwrap the DEK
- create a new verifier for the new password
- rewrap or retain the DEK envelope as appropriate
- keep encrypted health records readable under the same DEK
- define rollback and failure recovery behavior

### Password reset or recovery

Not implemented in MG-03.

Any future recovery design must explicitly define what secret can recover the
DEK, where that secret lives, who can access it, how it is revoked, and what
users are told before relying on it.

### Username change

Not implemented in MG-03.

In the current prototype, the username participates in credential verification.
Changing it later should be treated like a credential migration and reviewed
with password-change behavior.

## Encryption and data access behavior

Encryption and decryption are only allowed while the session is `Unlocked`.

Expected behavior:

- encrypt while locked returns locked
- decrypt while locked returns locked
- decrypt with wrong profile ID, record ID, schema version, corrupted nonce, or
  corrupted ciphertext returns invalid ciphertext
- decrypt with an unsupported payload version returns unsupported version
- cryptographic infrastructure failure returns unavailable

Associated data binds encrypted payloads to profile ID, record ID, schema
version, and cipher version. Callers must provide stable identifiers; otherwise
valid ciphertext may become undecryptable.

## Failure and edge cases

| Case | Expected behavior | Data consequence |
|---|---|---|
| Wrong password | Stay locked and show invalid-credentials error. | No data loss. |
| Wrong username | Stay locked and show invalid-credentials error. | No data loss. |
| Missing vault envelope | Report unavailable or route to onboarding when no vault exists. | Existing vault cannot be opened because security metadata is gone. |
| Malformed stored envelope | Fail closed as unavailable. | Data may be inaccessible until storage is repaired by a future tool. |
| Keystore key invalidated | Move session to invalidated and report key invalidated. | Data is inaccessible without future recovery. |
| Keystore unavailable | Report unavailable. | Retry may work if platform condition resolves. |
| App backgrounded during protected use | Lock immediately. | No data loss; unlock required. |
| Process death during create | If storage save completed, next launch sees a locked vault; otherwise fresh onboarding. | No plaintext DEK is persisted. |
| Process death during unlock | Next launch returns locked. | No data loss. |
| Uninstall/reinstall | Fresh onboarding. | Previous local vault unrecoverable. |
| Android app data cleared | Fresh onboarding. | Previous local vault unrecoverable. |

## UI and secret-handling expectations

- Password fields use password visual transformation.
- Password input is cleared after completed create/unlock attempts and explicit
  lock.
- The Activity uses `FLAG_SECURE` to reduce screenshots and recents disclosure.
- Secrets are not intentionally written to logs, intents, saved instance state,
  clipboard, or navigation arguments.
- PBKDF2 and Keystore operations run off the main thread.

Known limitation: Compose password text is represented as an immutable `String`
before being copied into a `CharArray`, so the app cannot guarantee immediate
zeroization of every transient password copy managed by the runtime.

## Future revisit checklist

Before moving beyond the private prototype, revisit:

- password policy, minimum length, strength guidance, rate limiting, and lockout
- biometric/device credential gating for Keystore unwrap
- password change and username change
- destructive vault reset UX
- encrypted export/import, recovery, or sync
- key rotation and version migration
- backup/restore tests on representative devices
- accessibility and overlay behavior
- independent mobile security review
