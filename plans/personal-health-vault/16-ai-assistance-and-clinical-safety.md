# MG-16 — AI Assistance, Grounding, and Clinical Safety

## Outcome

Add optional on-device intelligence for document assistance, grounded Q&A, pattern exploration, interaction explanations, and visit preparation without making generative output authoritative.

## Dependencies

MG-08 through MG-12, MG-14, and MG-17’s evaluation/security harness. D-012, D-013, D-016.

## Capability boundaries

- **OCR/parser:** extracts drafts with confidence and provenance; user verifies.
- **Interaction engine:** deterministic, authoritative source-based rules; AI may explain cited results.
- **Pattern explorer:** computes transparent temporal/statistical associations; AI may summarize with limitations.
- **Visit prep:** deterministic source packet first; AI may organize/rephrase.
- **Grounded assistant:** answers about selected vault records using cited snippets and declares when evidence is absent.
- **Prohibited initial behaviors:** diagnosis, dosage/treatment changes, emergency triage, autonomous alerts from model output, web browsing with vault context, or silent record mutation.

## AI architecture

1. `AssistantPolicy` authorizes capability, profile, data categories, purpose, and maximum context.
2. `RetrievalService` returns selected, verified, versioned source fragments from MG-14.
3. `InferenceEngine` accepts structured request/context and cancellation; it never queries repositories directly.
4. `OutputValidator` enforces schema/citations/length/prohibited claims and can reject.
5. UI shows source links, model/version, limitations, and clear draft status.
6. No prompt/output persistence by default; approved history is encrypted, profile-scoped, and separately deletable.

## Model lifecycle

- Publish a signed first-party model manifest with hash, size, capabilities, license, minimum device resources, and compatibility.
- Never ship a Hugging Face bearer token in `BuildConfig`; distribute only legally permitted artifacts through an approved authenticated mechanism.
- Verify hash/signature before load, use atomic download, quotas, cancellation, cleanup, and device thermal/memory checks.
- AI remains optional; allow delete/unload and ensure the vault works without it.

## Work packages

1. Approve a capability-by-capability clinical/regulatory risk assessment and claims copy.
2. Refactor current LiteRT code behind the structured AI boundary; remove conversation aggregate coupling.
3. Build consented context selection with visible included sources and strict profile isolation.
4. Implement constrained output schemas, citation verification, refusal/fallback, and deterministic post-processing.
5. Build interaction rules/data update pipeline with source licensing, freshness, severity governance, and failure semantics.
6. Build pattern computations with minimum sample sizes, multiple-comparison/false-positive caution, confounder language, and reproducible source windows.
7. Build visit prep and grounded Q&A with source-only answers and an “insufficient evidence” path.
8. Create red-team and clinical evaluation sets using synthetic/de-identified data across hallucination, wrong profile, prompt injection in documents, unsafe advice, missing citation, OCR error, multilingual input, and crisis/emergency prompts.
9. Add feedback/report mechanism that never uploads health context without separate consent.

## Quality gates

Set per-capability thresholds before evaluation: schema validity, citation precision/coverage, grounded factuality, unsafe-advice rate, refusal appropriateness, extraction accuracy, latency, memory, thermal behavior, and cancellation. Averages cannot hide critical safety failures.

## Acceptance criteria

- [ ] Every AI invocation is purpose- and profile-scoped with visible sources.
- [ ] Unverified OCR and unrelated records are excluded by default.
- [ ] Model output cannot mutate authoritative records without explicit review/confirmation.
- [ ] Interaction alerts come from an approved deterministic source, not LLM recall.
- [ ] Unsupported/unsafe questions fail safely and point to appropriate professional/emergency resources without pretending to triage.
- [ ] Model artifacts are licensed, integrity-checked, optional, deletable, and credential-safe.
- [ ] Clinical/red-team thresholds pass with no unresolved critical failures.

## Exit gate

Each capability ships independently only after product, clinical, privacy, security, and performance owners sign its evaluation report.

