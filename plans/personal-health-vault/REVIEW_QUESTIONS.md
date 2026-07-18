# Questions for Owner Review

Use this document to review and answer the decisions needed before implementation. You do not need to answer everything at once. Work from top to bottom because later answers depend on earlier choices.

For each question, replace `Pending` with your answer, notes, or `Defer`. Confirmed decisions will later be copied into `DECISIONS.md` with their rationale and consequences.

## Review 1 — Product direction

### Q-001: What is the initial intended use?

Should the first release focus strictly on organizing personal health records and wellness information, or should it make medical/clinical decision-support claims?

**Recommended starting point:** Personal record organization and wellness only. No diagnosis, treatment, dosage, or emergency-triage claims.

**Your answer:** Only personal record organization. AI that is implemented right now, keep it on backtrack. The AI chat will be use later so keep the chatting module intact. it will be use later.

### Q-002: Who is the first target user?

Examples: individual adults, caregivers managing parents, parents managing children, users with chronic conditions, or a broad consumer audience.

**Recommended starting point:** Adults managing their own records, with dependent profiles added after the self-profile workflow is stable.

**Your answer:** Personal records for now. 

### Q-003: Where will the first public version launch?

List the initial countries/jurisdictions and supported languages. Privacy, medical-function, insurance, emergency, and drug-reference requirements depend on this answer.

**Your answer:** Just India for now. But how will this impact development overall?

### Q-004: What should the application be called?

Decide whether to rename only the user-facing product or also change the Android application/package identity from `com.smartphoneaichat`.

**Recommended starting point:** Rename the product immediately; preserve the package temporarily unless store continuity is intentionally abandoned.

**Your answer:** Rename it to Personal Health Vault. 

## Review 2 — Storage, account, and recovery

### Q-005: Should the MVP be local-only?

Options include local-only without an account, optional account/cloud backup, or full multi-device synchronization.

**Recommended starting point:** Local-only MVP designed for future encrypted synchronization.

**Your answer:** Local only but code should be easy to modify for later integrations. 

### Q-006: How should users recover a vault?

A zero-knowledge design means the application operator cannot simply reset the encryption key. Possible recovery methods include a user-held recovery phrase/key, an encrypted recovery file, or no recovery.

**Recommended starting point:** User-held recovery secret plus an encrypted export/backup, with a mandatory recovery test during onboarding.

**Your answer:** Teach me more on this with architectural map. I have no idea on this. 

### Q-007: What should unlock the vault?

Choose whether to allow strong biometrics, device PIN/password, an app-specific passphrase, or a combination. Also choose the inactivity timeout.

**Recommended starting point:** Strong biometric or device credential, with a short configurable grace period and immediate manual lock.

**Your answer:** just username password for now. No timeout for now. 

### Q-008: Should Android system backup be disabled initially?

**Recommended starting point:** Exclude vault data until the encrypted backup and restore format has been independently tested.

**Your answer:** Teach me more on this with architectural map. I have no idea on this. 

## Review 3 — Profiles and emergency access

### Q-009: Which dependent profiles should be supported first?

Examples: children, parents, spouse/partner, or any user-defined dependent. Decide whether the app needs evidence of guardianship or only a user declaration in the first release.

**Recommended starting point:** User-declared caregiver relationship for local-only storage, clearly labeled as unverified.

**Your answer:** Just self record for now.

### Q-010: Which emergency fields may be visible without unlocking the vault?

Candidate fields: preferred name, blood group, severe allergies, critical conditions, critical medications, emergency contacts, and an owner-authored note.

**Recommended starting point:** Make every field opt-in and exclude reports, identifiers, insurance details, and AI-generated information.

**Your answer:** Will contemplate on this later. 

### Q-011: Is Google Wallet support required for the MVP?

Sensitive health passes require Google approval and additional service/signing work. Lock-screen behavior also varies by platform and device.

**Recommended starting point:** Ship the secure offline in-app emergency card first; treat Wallet and lock-screen surfaces as later integrations.

**Your answer:** later. just a simple offline app for now.

### Q-012: Is an iOS application part of the ultimate roadmap?

Apple Health, Face ID, Apple Wallet, and iOS widgets cannot be implemented inside this Android repository.

**Your answer:** Later. 

## Review 4 — Health features and external data

### Q-013: Which modules define the first usable release?

Choose the smallest release set from emergency card, medications, prescriptions/providers, reports, vitals, allergies, immunizations, symptoms/mood, insurance, search, and sharing.

**Recommended starting point:** Profiles, emergency card, medications, vitals, allergies, immunizations, and encrypted manual document storage.

**Your answer:** Encrytion later. 

### Q-014: Should medication reminders and adherence tracking be included initially?

Reminders introduce notification privacy, time-zone, reboot, battery, and exact-alarm behavior.

**Your answer:** Later.

### Q-015: Which drug-reference and interaction-data source should be used?

This requires jurisdiction coverage, licensing, update frequency, offline behavior, and clinical validation. Generative AI alone is not acceptable for interaction alerts.

**Your answer:** Later.

### Q-016: Should OCR work entirely on-device?

Cloud OCR can improve some documents but introduces transmission, vendor, consent, deletion, and jurisdiction requirements.

**Recommended starting point:** On-device OCR first, with no hidden network fallback.

**Your answer:** On device. The OCR should be modular. Plug and Play. Also it should be documented separately too so in other applications this can be applied. 

### Q-017: Which Health Connect data should be read initially?

Possible initial types: weight, height, resting heart rate, heart rate, steps, sleep, and blood pressure.

**Recommended starting point:** Read-only weight, height, resting heart rate, steps, and sleep after explicit per-type consent.

**Your answer:** Read-only? user can change anytime they want. 

### Q-018: Are direct Garmin and Fitbit integrations required?

Direct integrations may require vendor approval, OAuth, a backend, rate-limit handling, and additional privacy review. They are separate from Health Connect.

**Your answer:** Later. Its a dream. 

## Review 5 — Sharing and AI

### Q-019: Which sharing formats are essential?

Options include local PDF, encrypted archive, direct Android share, QR/emergency card, or an expiring web link.

**Recommended starting point:** Previewed PDF plus an encrypted archive. Defer web links until a separate zero-knowledge service exists.

**Your answer:** Teach me more on this. 

### Q-020: Should AI remain fully optional?

**Recommended starting point:** Yes. Every record-management workflow must work without downloading or loading a model.

**Your answer:** Yes. 

### Q-021: Which AI capability should be implemented first?

Options include report extraction assistance, grounded questions about selected records, interaction explanations, symptom-pattern summaries, or doctor-visit preparation.

**Recommended starting point:** Doctor-visit preparation from a deterministic source packet, followed by grounded questions with citations.

**Your answer:** We will work on this later. This needs a thorough session of discussion. So, Later.

### Q-022: Should AI conversation history be stored?

**Recommended starting point:** Do not persist prompts or outputs by default. Allow a user to explicitly save a useful summary as a separately labeled note.

**Your answer:** Idk, later.

### Q-023: How should model files be distributed?

The current client-side Hugging Face token approach must not remain. Choose an approved, licensed distribution mechanism with signed manifests and integrity checks.

**Your answer:** Later. keep things as is for MVP for now. 

## Review 6 — Migration and release

### Q-024: What should happen to the current chat experience?

Options: remove it, retain it temporarily behind a feature flag, or transform it into the later grounded health assistant.

**Recommended starting point:** Isolate it during foundation work, then replace it with the grounded assistant after MG-16.

**Your answer:** Yes, archive it for now.

### Q-025: What should happen to existing conversations and model downloads?

Current conversations are in-memory and generally do not survive process restart. Existing downloaded model files may remain on devices.

**Recommended starting point:** Do not import conversations as health records. Delete or re-validate model files against the new signed model manifest.

**Your answer:** the AI chat is archived for now. so we will work on this later. 

### Q-026: What telemetry is acceptable?

Options include no telemetry, local-only diagnostics, opt-in redacted operational metrics, or broader analytics.

**Recommended starting point:** Local diagnostics only; consider opt-in, content-free operational telemetry after privacy review.

**Your answer:** I dont know. Later. Basic logs for now.

### Q-027: What is the desired rollout path?

Examples: private prototype, internal testing, closed beta, or eventual public Play Store release.

**Your answer:** private prototype.

## Review summary

Use this section when a review round is complete.

```text
Review date:
Questions answered:
Questions deferred:
New requirements:
Requirements removed or changed:
Decisions ready to transfer into DECISIONS.md:
Notes for the next planning revision:
```

