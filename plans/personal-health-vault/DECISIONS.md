# Decision Register

Resolve decisions before the mini-goal that lists them as prerequisites. Accepted decisions should include date, owner, rationale, alternatives, and consequences.

| ID | Decision | Default planning assumption | Required by | Status |
|---|---|---|---|---|
| D-001 | Intended use and launch jurisdictions | Personal record organization only; no diagnosis/treatment claims; India; private prototype | MG-01 | Accepted — 2026-07-18 |
| D-002 | Android application/package rename | Product name is Personal Health Vault; “Health Vault” is allowed only as a compact app-bar label; package-identity decision deferred | MG-02 | Partially accepted |
| D-003 | Single module vs staged multi-module | Introduce core/feature boundaries incrementally, not in one rewrite | MG-02 | Open |
| D-004 | Local-only MVP vs account/sync | Local-only prototype; interfaces remain ready for future integrations | MG-03/04 | Accepted — 2026-07-18 |
| D-005 | Vault unlock and inactivity policy | Prototype local username/password; no inactivity timeout; revisit before public release | MG-03 | Accepted for prototype |
| D-006 | Recovery model | Reserved/optional recovery fields only; no recovery mechanism in prototype | MG-03/04 | Deferred |
| D-007 | Database encryption implementation | Evaluate maintained Room-compatible encryption; approve via security spike | MG-04 | Open |
| D-008 | Android Auto Backup | Backup/restore deferred; system backup disabled until a tested design exists | MG-04 | Accepted for prototype |
| D-009 | Dependent authorization/guardianship evidence | Self profile only | MG-05 | Deferred |
| D-010 | Emergency projection fields and exposure | User-selectable minimum: name, blood group, severe allergies, critical conditions, emergency contacts | MG-07 | Open |
| D-011 | Google Wallet private pass | Feasibility and issuer approval required; not an MVP dependency | MG-07 | Open |
| D-012 | Drug knowledge provider | Licensed/authoritative dataset or API with update SLA and jurisdiction coverage | MG-08/16 | Open |
| D-013 | OCR engine | On-device, modular/plug-and-play, separately documented; no cloud fallback | MG-09 | Accepted — 2026-07-18 |
| D-014 | Health Connect direction | No Health Connect integration in prototype | MG-12 | Deferred |
| D-015 | Cloud share links | Defer until zero-knowledge service threat model and deletion proof exist | MG-15 | Open |
| D-016 | On-device model portfolio | Preserve existing chat/model implementation in an archived feature area; defer changes | MG-16 | Deferred |
| D-017 | Telemetry | Basic local, content-free diagnostic logs; no remote analytics | MG-17 | Accepted for prototype |
| D-018 | Existing chats/model files | Archive/preserve current chat and model code for later work | MG-18 | Accepted — 2026-07-18 |
| D-019 | Future service provider and sync model | Local database is authoritative. Any cloud service, including Supabase, is optional and must only receive explicitly approved encrypted backup/sync payloads until a separate security decision approves more. | MG-19 | Accepted for prototype — 2026-07-19 |
| D-020 | Android device-test lane | Establish a reproducible emulator/connected-test lane before feature implementation; exact AVD/device matrix remains open. | MG-20 | Open |

## Decision record template

```text
Decision ID:
Date / owner:
Context:
Chosen option:
Alternatives considered:
Security/privacy/safety impact:
Migration and reversibility:
Evidence / references:
```
