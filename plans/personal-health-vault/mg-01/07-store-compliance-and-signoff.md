# Store Compliance and Reviewer Sign-off

## Distribution baseline

The current target is a private development prototype. This does not authorize
public, closed-test, or production distribution. Before any external Play track,
the release owner must re-audit the actual binary, SDKs, permissions, network
traffic, privacy copy, and every enabled feature.

## Google Play evidence map

| Requirement | Prototype position | Evidence required before Play distribution |
|---|---|---|
| Health apps declaration | Not submitted for private local development | Declare every enabled category, likely including healthcare services/management and each enabled medication, emergency, or health-tracking category |
| Health Content and Services policy | Claims bounded by MG-01 | Public and in-app privacy policy, accurate limitations, professional-care reminder where applicable, no misleading/harmful functionality |
| Data safety form | No health-data transmission intended | Binary/SDK/network audit; disclose all off-device collection by app or SDKs; explain security and deletion accurately |
| Privacy policy | Drafting checklist only | Active public non-geofenced HTML URL plus in-app link/text, operator/contact, full personal/sensitive-data handling |
| Permissions | Core prototype should minimize permissions | Manifest-to-feature matrix, just-in-time disclosure, remove unused permissions |
| Medical functionality | No medical-device or clinical decision claims | Counsel/clinical classification; approval/certification evidence if classification changes |

The Play forms must describe the shipped binary, not the planned architecture.
Local processing is not “collection” in the specific Play Data safety definition
unless data is transmitted off device, but it remains sensitive processing that
must be handled transparently and consistently with other Play policies.

## India review checklist

- Identify the legal operator/data fiduciary and privacy/grievance contacts.
- Confirm the DPDP Act and final DPDP Rules provisions and phased commencement
  dates applicable on the intended release date.
- Review notice language, specified purposes, consent/withdrawal, data-principal
  rights, security safeguards, processor terms, breach process, retention and
  erasure for the actual processing model.
- Confirm the age threshold and whether any child/dependent processing exists.
- Confirm whether any feature or claim triggers medical-device, clinical,
  telemedicine, pharmacy, insurance, or other sector-specific obligations.
- Confirm whether local-only/private use changes any obligations without
  assuming it eliminates them.
- Reopen the review for cloud, external sharing, Health Connect, ABDM/ABHA,
  wearable, AI service, or public distribution.

## Authoritative references checked 2026-07-19

- [Digital Personal Data Protection Act, 2023 — India Code](https://www.indiacode.nic.in/indiacode/handle/123456789/22037?view_type=browse)
- [Final DPDP Rules 2025 and enforcement timeline — MeitY](https://www.meity.gov.in/documents/act-and-policies/digital-personal-data-protection-rules-2025-gDOxUjMtQWa)
- [Google Play Health apps declaration](https://support.google.com/googleplay/android-developer/answer/14738291)
- [Google Play Health Content and Services policy](https://support.google.com/googleplay/android-developer/answer/16679511)
- [Google Play Data safety form](https://support.google.com/googleplay/android-developer/answer/10787469)

These sources are an operational baseline, not a substitute for specialist
advice. Re-check them at each release because law, commencement schedules, and
store policy can change.

## Counsel and reviewer sign-off record

| Review | Reviewer / qualification | Scope | Outcome | Date | Open findings / evidence |
|---|---|---|---|---|---|
| Product | TBD | Intended use, users, scope, claims | Open | — | — |
| Security | TBD | Data boundary, vendors, incident response | Open | — | — |
| India privacy/legal | TBD | DPDP applicability, notices, rights, release model | Open | — | — |
| Clinical safety | TBD | Classification, limitations, safety escalation | Open | — | — |
| Store compliance | TBD | Shipped binary, declarations, privacy policy | Not required for private prototype; open before distribution | — | — |

## Sign-off statement

```text
I reviewed the MG-01 governance packet against the stated India-first private
prototype scope. My approval applies only to the documented version and does
not extend to deferred features, external distribution, or changed data flows.

Reviewer / role / qualification:
Packet commit or version:
Approved / approved with conditions / rejected:
Conditions and due dates:
Date / signature or evidence link:
```
