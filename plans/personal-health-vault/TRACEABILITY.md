# Master Requirement Traceability

This matrix ensures every requirement in the supplied master development plan has an owning mini-goal and proof target.

| Master requirement | Primary plan | Supporting plans | Proof target |
|---|---|---|---|
| Zero-knowledge client-side encryption | MG-03 | MG-04, MG-15, MG-17 | Threat model; key hierarchy; server cannot decrypt if/when service exists; pen test |
| Biometric access | MG-03 | MG-02, MG-17 | Instrumented auth/key invalidation/lifecycle suite |
| Offline emergency information | MG-07 | MG-03 to MG-06 | Offline locked-state E2E and exposure review |
| Lock-screen widget / Wallet pass | MG-07 | MG-01, MG-17 | Platform feasibility, issuer approval, privacy/security review |
| Active/discontinued medication tracking | MG-08 | MG-04, MG-05 | Regimen state-machine and chronology tests |
| Daily intake schedule | MG-08 | MG-06 | Recurrence/DST fixtures and accessible Today UI |
| Medication monographs/precautions/side effects | MG-08 | MG-01, MG-16 | Approved source/version/freshness and content review |
| Prescriptions and provider history | MG-08 | MG-09 | Encrypted links, provenance, timeline tests |
| Timeline report storage | MG-09 | MG-04 | Transactional import and chronological retrieval |
| Vitals dashboard | MG-10 | MG-06 | Source/unit/time-aware views and fixtures |
| Height/weight charts | MG-10 | MG-12 | Conversion/chart/accessibility tests |
| Allergy and immunization tracker | MG-10 | MG-07 | Domain/history and emergency projection tests |
| Apple Health | Separate iOS workstream | MG-01 | Not implementable in Android repository |
| Google Health Connect | MG-12 | MG-10, MG-17 | Permission, provenance, idempotent sync suite |
| Garmin/Fitbit | MG-12 later adapters | MG-01, MG-03, MG-17 | Vendor/API/service ADR and contract tests |
| Resting heart rate, sleep, steps context | MG-12 | MG-10, MG-16 | Least-privilege import with provenance |
| Five-second symptom/mood logger | MG-11 | MG-06 | Timed usability/accessibility study |
| Insurance cards/policies/deductibles/claims | MG-13 | MG-09, MG-15 | Masking/encryption/export tests |
| Lab report OCR/parser | MG-09 | MG-16, MG-17 | Golden corpus metrics; review-before-commit |
| Drug interaction alerts | MG-08 | MG-16, MG-17 | Deterministic authoritative source and clinician-reviewed fixtures |
| Symptom pattern spotting | MG-16 | MG-08, MG-10 to MG-12 | Reproducible association method, sources, safety evaluation |
| Doctor visit prep | MG-11 | MG-15, MG-16 | Deterministic packet, source list, user approval |
| Global search | MG-14 | MG-03 to MG-05 | Encrypted-index leakage review and performance suite |
| Proxy/dependent profiles | MG-05 | All feature plans | Scoped repository/authorization tests |
| Granular PDF sharing | MG-15 | MG-05, feature plans | Byte-level disclosure and PDF accessibility tests |
| Time-limited/password web links | MG-15 | MG-01, MG-03, MG-17 | Separate zero-knowledge service and abuse/deletion review |
| Home-screen emergency vital card | MG-06, MG-07 | MG-05, MG-10 | Locked/unlocked UI, minimum-data and accessibility tests |

## Cross-cutting requirement coverage

| Concern | Owner |
|---|---|
| Product claims, consent, retention, incident response | MG-01 |
| Dependency boundaries, navigation, app/session lifecycle | MG-02 |
| Key lifecycle and authentication | MG-03 |
| Persistence, files, backup, corruption, deletion | MG-04 |
| Profile authorization, consent receipts, audit | MG-05 |
| Accessibility and adaptive UI | MG-06 plus every feature |
| Testing, privacy evidence, release operations | MG-17 |
| Upgrade, legacy retirement, staged rollout | MG-18 |

## Completion rule

A master requirement is complete only when its primary plan exit gate is closed, supporting-plan dependencies are complete, and the proof target is stored with the release evidence. A checked UI box without data lifecycle, security, and failure-path proof does not satisfy traceability.

## Current implementation evidence — 2026-07-19

| Implemented seam | Owning plan | Evidence | Remaining boundary |
|---|---|---|---|
| Session destination resolution | MG-02 | Unit tests cover fresh, locked-returning, and unlocked-returning states. | Unlock/home screens and durable session persistence are not implemented. |
| Welcome → Credentials onboarding transition | MG-02 | ViewModel unit test and two connected Compose tests pass. | Credential fields, validation, storage, and unlock are not implemented. |
| Android connected-test lane | MG-20 | Focused suite passes on `Pixel_10(AVD) - 17`; visual onboarding smoke inspected. | AVD specification, scripts, retained regression artifacts, CI, and physical-device matrix remain open. |
