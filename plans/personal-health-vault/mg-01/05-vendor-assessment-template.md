# Vendor Assessment Template

No vendor may receive health data or enter a release build until this assessment
is approved by product, security/privacy, and legal. An on-device SDK is still a
vendor dependency and must be checked for network behavior, bundled trackers,
updates, and supply-chain risk.

## Identification

| Field | Response |
|---|---|
| Vendor/product/version | TBD |
| Business owner / technical owner | TBD |
| Capability and necessity | TBD |
| Alternatives, including no vendor | TBD |
| Runtime, SDK, API, or hosted service | TBD |
| Jurisdictions and subprocessors | TBD |
| Contract/DPA/license links | TBD |
| Assessment date / next review | TBD |

## Data and purpose

- Exact fields and files accessible to the vendor:
- Whether data leaves the device:
- Purpose and user benefit:
- Required vs optional processing:
- Identifiers, metadata, IP addresses, and diagnostics observed:
- Training, profiling, advertising, resale, or secondary-use restrictions:
- Retention, backups, cache, and deletion SLA:
- User consent/notice and withdrawal behavior:
- Data localization or cross-border transfer:

## Security and operational evidence

- Encryption in transit and at rest; key ownership:
- Authentication, authorization, tenant isolation, and administrator access:
- Network endpoints and offline behavior verified:
- SDK permissions, manifest entries, trackers, and transitive dependencies:
- Signed artifacts, integrity checks, vulnerability disclosure, SBOM, and patch SLA:
- Incident-notification time and cooperation obligations:
- Availability, rollback, version pinning, and exit/export plan:
- Independent audit/certification and scope:

## Capability-specific checks

| Capability | Mandatory evidence |
|---|---|
| OCR | On-device mode verified; supported scripts/documents; confidence/provenance; no hidden upload |
| Drug knowledge | Authoritative source, India coverage, license, version/freshness SLA, correction/recall process |
| Crash reporting | Exact content-free event schema, local redaction tests, opt-in decision, deletion and sampling |
| Cloud storage/sync | Approved threat model, encrypted payload format, key boundary, replay/rollback/conflict/deletion proof |
| Sharing links | Authentication, expiry, revocation, abuse controls, access logs, deletion proof, metadata minimization |
| AI/model | Prompt/data destination, training policy, model/version, grounding, deletion, safety evaluation, offline fallback |

## Decision

| Item | Response |
|---|---|
| Risk rating | Critical / High / Medium / Low |
| Findings and mitigations | TBD |
| Residual risk accepted by | TBD |
| Decision | Approve / approve with conditions / reject |
| Expiry or reassessment trigger | Version, scope, subprocessors, incident, or policy change |
