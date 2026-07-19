# Intended Use and Claims Register

## Intended use

Personal Health Vault is an Android application for adults in India to enter,
organize, view, and retrieve their own personal health and wellness records on
their device. The private prototype is intended for personal record keeping and
product evaluation with synthetic or developer-controlled data.

It is not a clinical system of record and does not replace the original record
held by a healthcare provider. Users remain responsible for checking entries
against authoritative documents and consulting qualified professionals for
medical decisions.

## Intended users and environment

| Dimension | Baseline |
|---|---|
| User | Adult managing only their own records |
| Minimum age | Proposed 18+; approval open |
| Dependent/caregiver use | Not supported in the prototype |
| Jurisdiction | India only |
| Distribution | Private development prototype |
| Language | Proposed English for prototype; approval open |
| Connectivity | Core record management works offline |
| Accessibility | Android semantics, screen-reader labels, scalable text, adequate contrast, non-color cues, and touch-target checks are required from the first UI slice |

## Permitted claims

- “Organize your personal health records on your Android device.”
- “Works offline for supported record-management workflows.”
- “You review and control records before they are saved or shared.”
- “AI features are optional and separate from the health record” only while
  that technical separation is demonstrably true.
- “On-device OCR creates a draft for your review” only after the OCR workflow
  enforces review before commit.

## Prohibited claims

- The app diagnoses, predicts, prevents, monitors, or treats a disease.
- The app recommends a treatment, medication, dosage, or emergency action.
- The app guarantees accuracy, safety, medical completeness, or provider
  acceptance.
- OCR or AI output is clinically verified, authoritative, or automatically
  part of the medical record.
- The prototype is encrypted, zero knowledge, breach proof, HIPAA compliant,
  DPDP compliant, ABDM certified, government affiliated, or a regulated
  medical device without documented evidence and approval.
- Username/password alone secures or encrypts stored health data.
- Local storage means data cannot be lost, copied through an unreviewed backup
  path, or accessed on a compromised/unlocked device.
- The emergency surface can replace emergency services or professional care.

## Required limitation copy

| Decision point | Minimum message |
|---|---|
| Prototype onboarding | Private prototype; use synthetic/test data because production encryption is not implemented |
| Manual record entry | Check the value and source before saving |
| OCR review | Draft extracted from a document; verify every field against the source |
| AI entry point | AI may be wrong and cannot diagnose, prescribe, or change records without review |
| Emergency information | User-selected reference information; call local emergency services and seek professional care |
| Export/share preview | Review exactly what will leave the app and who can access it |
| Delete action | State which primary and derived data will be deleted and whether recovery is possible |

## Claims-change control

Any new store listing, onboarding text, AI capability, alert, trend,
recommendation, sensor-derived result, or external integration must be reviewed
against this register. A change that moves a feature toward diagnosis,
treatment, clinical decision support, or medical-device functionality reopens
MG-01 and requires jurisdiction-specific clinical/regulatory review before
release.
