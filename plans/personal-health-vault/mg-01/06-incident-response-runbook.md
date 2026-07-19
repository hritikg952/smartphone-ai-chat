# Incident-Response Runbook

## Scope and principles

Use this runbook for suspected loss, disclosure, corruption, unauthorized
access, unsafe clinical behavior, secret exposure, malicious dependency,
incorrect store declaration, or failure to honor deletion. Preserve evidence
without copying health content into tickets, chat, logs, or screenshots.

## Roles

| Role | Named person | Responsibility |
|---|---|---|
| Incident commander | TBD | Severity, coordination, timeline, closure |
| Security lead | TBD | Containment, evidence, technical investigation |
| Privacy/legal lead | TBD | Applicability, notification and regulator assessment |
| Clinical safety lead | TBD | Patient/user harm assessment and safety containment |
| Product/communications lead | TBD | User impact, accurate notices, support coordination |

## Severity

| Level | Trigger | Target response |
|---|---|---|
| SEV-1 | Credible serious-harm risk, broad health-data exposure, active compromise, signing/key compromise | Immediate containment and executive/specialist escalation |
| SEV-2 | Limited sensitive-data exposure, cross-profile access, destructive data loss, high-risk unsafe output | Same-day containment and assessment |
| SEV-3 | Contained privacy/control failure without known disclosure or serious harm | Prompt fix before release; monitor scope |
| SEV-4 | Low-risk process/policy defect | Normal corrective-action workflow |

## Response flow

1. **Receive and triage:** record reporter, time, build/version, affected
   capability, and a content-free description. Do not ask for real records in a
   general issue tracker.
2. **Classify:** assign incident commander, provisional severity, possible data
   categories/profiles, safety impact, and whether the issue is ongoing.
3. **Contain:** disable distribution or affected feature, revoke credentials,
   isolate a dependency, remove network access, or provide safe user guidance.
   Do not destroy evidence.
4. **Preserve evidence:** record hashes, versions, configuration, content-free
   logs, timestamps, and access events in a restricted location with a chain of
   custody. Minimize copied personal data.
5. **Investigate:** establish first/last affected versions, root cause, affected
   population, data destinations, deletion status, clinical consequences, and
   exploitability.
6. **Assess notifications:** privacy/legal determines obligations and deadlines
   under the laws and contracts applicable on the incident date. Clinical
   safety determines whether urgent corrective communication is needed.
7. **Eradicate and recover:** patch, rotate, remove bad data/dependency, verify
   migrations/deletion, run focused regression and safety tests, and stage a
   controlled rollout.
8. **Communicate:** state known facts, uncertainty, user actions, support route,
   and correction plan. Never minimize risk or make unsupported legal claims.
9. **Close and learn:** obtain owner sign-off, create a blameless postmortem,
   track corrective actions to evidence, update threat/data/claims registers,
   and rehearse the changed control.

## Evidence record

```text
Incident ID / severity:
Opened / detected / contained / closed timestamps:
Incident commander and reviewers:
Affected versions and capabilities:
Data categories and approximate affected population:
Clinical-safety impact:
Root cause and contributing controls:
Containment and credential/key actions:
Notification assessment and rationale:
Recovery verification:
Corrective actions, owners, due dates, evidence:
Postmortem and approval links:
```

## Exercise requirement

Before any public or external beta, run at least one tabletop covering an
unexpected health-data network transmission and one covering an unsafe or
incorrect medication/OCR result. Documentation alone does not satisfy the
release evidence gate in MG-17.
