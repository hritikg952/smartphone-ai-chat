# MG-03 vault threat model

**Status:** Implemented prototype boundary; independent security review pending  
**Date:** 2026-07-20

## Assets and trust boundaries

Primary assets are the vault DEK, user credentials, decrypted health records,
encrypted records, record/profile metadata, and the Android Keystore wrapping
key. Trust boundaries exist between Compose input and presentation state,
presentation and `VaultKeyManager`, process memory and app-private storage,
app-private storage and Android backup, the app process and Android Keystore,
and unlocked routes versus locked/background execution.

The current data flow is:

1. Setup sends a transient username/password to `VaultKeyManager` on a worker
   dispatcher.
2. A random DEK is wrapped by the Android Keystore key. Only the verifier and
   wrapped envelope are persisted.
3. Unlock verifies the password before requesting envelope unwrap.
4. The process-local `VaultSession` supplies a temporary DEK copy to
   `VaultCipher` only while unlocked.
5. Lock, backgrounding, invalidation, or destruction clears the in-memory key;
   protected routes resolve back to unlock.

## Threat analysis

| Threat | Boundary/asset | Implemented mitigation | Residual risk |
|---|---|---|---|
| Lost device or offline filesystem extraction | Preferences and future ciphertext | Random DEK; non-exportable Keystore wrapping key; no plaintext DEK; backup disabled | Weak device lock, rooted device, or live-process compromise can weaken protection |
| Password guessing | Credential verifier | Unique salt, PBKDF2-HMAC-SHA256 work factor, constant-time verifier comparison, generic failure | No attempt counter or rate limit in the local prototype; weak passwords remain guessable after verifier extraction |
| Record/profile substitution | Encrypted health payload | AES-GCM AAD binds cipher version, profile, record, and schema version | Callers must supply stable, validated identifiers |
| Ciphertext or envelope modification | Stored encrypted bytes | GCM authentication and version checks fail closed | Storage corruption can make data unavailable; recovery is absent |
| Replay/downgrade | Versioned payload/envelope | Unknown versions rejected; AAD includes version and record identity | No monotonic rollback counter or migration ledger yet |
| Key invalidation/removal | Android Keystore | Typed `KeyInvalidated` state; stored envelope is retained | Without recovery, invalidation makes the vault inaccessible |
| Background or process exposure | In-memory DEK and protected UI | Immediate `onStop` lock; process restart begins locked; route guard removes protected UI | Java/ART and cipher providers may retain transient internal copies outside explicit zeroization control |
| Screenshot/recents disclosure | Sensitive UI | Activity uses `FLAG_SECURE` | Accessibility services or a compromised OS can still observe content |
| Backup/restore disclosure | Preferences and future files | `allowBackup=false` and `fullBackupContent=false` | OEM behavior must be checked on the release device matrix |
| Clipboard, logs, saved state, intents | Credentials/plaintext | No clipboard path; no secret logging; access password is cleared after every completed attempt; no secret saved-state type | Compose `String` input is immutable and cannot be reliably zeroized before garbage collection |
| Exported component/deep-link bypass | Protected routes | Only launcher Activity exported; allowlisted routes redirect while locked | Future exported components/background workers need the same session enforcement |
| Overlay/accessibility abuse | Credential input and visible records | Password visual transformation and secure window | Prototype has no overlay detection; accessibility with user authorization remains powerful |
| Malicious dependency/model | App process | Legacy AI/native runtime excluded from the default artifact and security core | Other dependencies still share the process and require dependency review |
| Compromised server | Vault content | Prototype is local-only and sends no vault payload to a service | Future sync must use a separately reviewed encrypted protocol |
| Destructive misuse | Key and envelope | Explicit destroy clears session, Keystore alias, and stored security envelope | Destroy is irreversible because recovery is unavailable |

## Explicitly out of scope

- A fully compromised unlocked Android OS or attacker-controlled code running as
  this application.
- Physical attacks against device hardware or a compromised Keystore/TEE.
- Biometric coercion, enrollment changes, and biometric lockout because the
  accepted prototype policy does not enable biometric authentication.
- Multi-device recovery, remote reset, sync, emergency projection, and sharing.
- Protection of real health records before MG-04 supplies encrypted repositories
  and file lifecycle controls.

## Required follow-up before distribution

- Independent mobile security review and penetration test.
- Password policy, rate limiting, PBKDF/KDF benchmark, and migration design.
- Decide whether Android biometric/device credential must gate every unwrap.
- Implement and test rollback-resistant key rotation or keep it explicitly
  unavailable.
- Test rooted/debuggable devices, reboot, low-memory/process death, OS upgrades,
  backup/restore behavior, accessibility, overlays, and representative hardware.
