# MG-03 — Vault Cryptography, Key Lifecycle, and Biometric Access

## Outcome

Define and implement a reviewable cryptographic boundary so plaintext health data is available only during an authorized unlocked session.

## Dependencies

MG-01, MG-02; decisions D-004, D-005, D-006.

## Threat model

Cover lost/stolen device, offline filesystem extraction, malicious backup restore, rooted/debugged devices, memory/log leakage, exported components, screenshots, clipboard, overlay/accessibility abuse, biometric enrollment change, key invalidation, downgrade, tampered model/data files, compromised server, malicious dependent user, and accidental sharing.

Explicitly document what is not defended against, including a fully compromised unlocked OS unless stronger controls are selected.

## Key hierarchy

- Generate a random vault data-encryption key (DEK); never derive it directly from a short PIN.
- Wrap the DEK with a Keystore-backed key restricted to approved authentication.
- If recovery/multi-device access is approved, create a separate user-held recovery wrapping path using a memory-hard KDF, unique salt, versioned parameters, and authenticated encryption.
- Use per-file keys or derived subkeys with domain separation; bind ciphertext to record/profile/version metadata as associated data.
- Persist only versioned key envelopes, non-secret salts, and ciphertext.
- Keep emergency projection keys/data outside the vault key hierarchy and minimize their scope.

Exact algorithms, libraries, authentication validity windows, hardware-backed requirements, and recovery UX require a security ADR and independent review. Follow current Android Keystore and cryptography guidance in `REFERENCES.md`.

## Work packages

1. Produce data-flow and STRIDE-style threat models with trust boundaries.
2. Define key states: absent, created, wrapped, unlocked, locked, invalidated, rotation pending, recovery required, destroyed.
3. Implement `VaultKeyManager`, `VaultSession`, `AuthenticationGateway`, and cryptographic version metadata behind interfaces.
4. Use `BiometricPrompt`/device credential to authorize key use; handle no hardware, no enrollment, lockout, cancellation, and enrollment changes.
5. Auto-lock on process background/timeout and immediately clear in-memory caches/state; define interruption-safe writes.
6. Add screenshot/recents redaction policy for sensitive screens and clipboard restrictions for secrets.
7. Design key rotation and cryptographic migration with resumability and rollback protection.
8. Design recovery enrollment, confirmation, secure display/export, loss warning, and recovery testing.
9. Add tamper/version validation and fail closed without destroying recoverable ciphertext.

## Tests and security verification

- Known-answer and round-trip tests; corrupted ciphertext/AAD/version rejection.
- Instrumented biometric success, cancellation, lockout, key invalidation, reboot, background timeout, and process-death tests.
- Verify keys/plaintext never appear in logs, crash reports, saved state, screenshots, clipboard, backups, or exported intents.
- Negative tests for wrong profile/record associated data and replayed envelopes.
- Independent design review and mobile penetration test before release.

## Acceptance criteria

- [ ] Approved threat model and crypto ADR exist.
- [ ] Vault DEK is random and never stored plaintext.
- [ ] Keystore authentication gates unwrap/use according to policy.
- [ ] Lock clears all feature state and blocks deep links/background work from reading plaintext.
- [ ] Recovery and rotation are either implemented/tested or explicitly unavailable with clear user warning.
- [ ] Crypto agility/version migration is proven.
- [ ] Independent review has no unresolved critical/high findings.

## Exit gate

MG-04 may persist real health data only after the security boundary passes destructive, invalidation, and process-lifecycle tests.

