# Figma Design Context and Plan Alignment

**Source:** [Personal Health Vault design](https://www.figma.com/design/gWOUuvPHpGjnIhlbFr85FT/Design?node-id=0-1&p=f&t=zwUez6PyvfTmTAK6-0)  
**Reviewed:** 2026-07-19  
**Scope:** Design reference for the local, single-profile prototype. This document does not change the Figma file.

## Confirmed visual direction

The Figma design is aligned with the planned dark Android experience:

- 390 dp mobile artboards with a native Android-style top app bar and bottom navigation.
- Near-black canvas, dark elevated cards, restrained borders, and a muted teal primary action color.
- Manrope for display/headline text and Inter for body, labels, and controls.
- Compact card-led information hierarchy with readable type, clear iconography, 16 dp-style content gutters, and rounded surfaces.
- A subtle low-opacity teal radial depth treatment on the access screen. This is acceptable as a background detail; it should not become a bright or decorative gradient system.
- Bottom navigation: **Home**, **Records**, **Add**, and **Settings**.

## Screens captured in the design

| Figma screen | Plan alignment | Prototype interpretation |
|---|---|---|
| Unlock Vault | MG-02, MG-06, prototype access flow | Local username/password entry only; no biometric or recovery capability yet. |
| Home Dashboard | MG-06 | Today’s manually entered schedule, scan entry, quick vital entry, trend preview, and recent records. |
| Records Hub | MG-06, MG-08 to MG-10 | Search and categories for medications, reports, vitals, allergies, vaccines/immunizations, and notes. |
| Medications | MG-08 | Active/discontinued lists, regimen timing and dose display, manual Add New flow. |
| Vitals content | MG-10 | Trend/measurement presentation; values must retain date, unit, and source when implemented. |

The avatar represents the one local self profile. It must remain local-only and optional; it is not an account, cloud profile, or dependent-profile system.

## Design-system handoff to Compose

| Figma concept | Compose implementation target |
|---|---|
| 64 dp top app bar | `VaultTopAppBar` |
| Four-item bottom navigation | `VaultBottomNavigation` with typed routes |
| Elevated record cards | `RecordCategoryCard` / `VaultCard` |
| Active navigation pill | Material 3 navigation item with app color tokens |
| Teal primary action | `VaultPrimaryButton` and primary FAB/add action |
| Medication row/status chip | `MedicationListItem` and `StatusChip` |
| Home’s Today card | `TodayFocusCard` composed from manual schedule/record state |
| Weight trend card | `MetricTrendCard` with accessible text/table alternative |
| Scan Report entry | `DocumentImportEntryPoint`; the on-device OCR adapter remains optional/modular |

Use the existing Compose/Material 3 app as the implementation base. Convert visual rules into `ui/theme` tokens and reusable composables; do not import web/Tailwind code from Figma.

## Required prototype copy corrections

The Figma visual language is usable now, but the following copy must change before the prototype is implemented because security, recovery, and clinical-alert features are intentionally deferred:

| Current Figma copy/pattern | Prototype-safe replacement or rule |
|---|---|
| “Secure local access to your medical records.” | “Local access to your personal health records.” |
| “Data is encrypted and stored locally…” | “Stored locally on this device. Security upgrades are planned.” Do not claim encryption until MG-03/04 are complete. |
| “Forgot Credentials?” | Remove or hide. The prototype has no password recovery. |
| “Clinical Data” filter | Prefer “Health records” or a neutral record-type label to avoid an unintended clinical-service claim. |
| “3 Alerts” for allergies | Use “3 recorded allergies.” Alerts/interactions are a later feature. |
| Today’s medication task/check state | Display manually entered schedule information only. Do not imply reminders, notification scheduling, or adherence monitoring in the prototype. |
| “Health Vault” in compact app bars | Permitted as a compact label; use “Personal Health Vault” for unlock, settings/about, and store-facing identity. |

## Scope boundaries preserved by the design

- No chat, assistant panel, social content, clinician messaging, payment, insurance, wearable sync, cloud account, Wallet pass, or sharing workflow is part of the design reference.
- OCR appears only as a scan/report entry point and review flow; it must not silently create authoritative data.
- Medication, report, allergy, and vital content is record organization. The UI must not diagnose, prescribe, recommend dosage changes, or claim clinical monitoring.
- Demo names, medication values, avatars, counts, and chart data remain fictional placeholders until feature data exists.

## Implementation order

1. Create shared dark tokens, typography, card, status, and navigation components from the Figma system.
2. Implement unlock/access screen with the corrected prototype copy.
3. Implement the app shell and four destination routes.
4. Implement Records Hub and its category cards.
5. Implement medications, reports/import entry, and vitals as independent feature screens.
6. Return to the Figma file only when the prototype scope adds or changes a screen; maintain this document as the code/design contract.

