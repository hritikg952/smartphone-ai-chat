# MG-14 — Private Global Search

## Outcome

Return fast, profile-scoped results across structured records and verified document content without creating a plaintext shadow copy of the vault.

## Dependencies

MG-08 through MG-11 and MG-13; security design from MG-03/04.

## Search scope

Medication/regimen names and ingredients, providers/facilities, report titles/tags/dates, verified extracted observations, vitals/allergies/immunizations, journal tags/text according to user setting, insurance metadata with masked snippets, and optionally document OCR text after explicit indexing policy.

## Architecture decisions

Choose after a security spike among an encrypted database-native full-text index, application-layer token index, or constrained in-memory search. Document leakage characteristics (term frequency, equality, index size), locale/tokenization, ranking, migration, and deletion. Do not use an unencrypted FTS sidecar.

## Work packages

1. Define searchable fields, sensitivity classes, excluded fields, snippet masking, and source verification filters.
2. Implement one profile-scoped `SearchRepository`; caller cannot request all profiles implicitly.
3. Build versioned indexer driven by committed change events/outbox; support rebuild and progress while unlocked.
4. Normalize case/diacritics/units/drug synonyms cautiously while preserving exact-match behavior.
5. Implement filters for type/date/provider/source/status and stable relevance ordering.
6. Build search UI with debouncing/cancellation, recent searches disabled by default or encrypted, keyboard/accessibility, and result provenance.
7. Deep link through the lock/profile authorization gate and verify the result still exists.
8. Remove/update terms atomically on edit/delete/profile deletion/consent changes; add integrity reconciliation.
9. Prevent query text/results from logs, analytics, saved state, app recents, and assistant context unless explicitly submitted.

## Performance and tests

- Define representative dataset and p50/p95 targets for cold/warm search, index update, rebuild, memory, and storage.
- Cross-profile leakage, deleted/stale result, malformed Unicode, locale, large-note, concurrent update, locked state, and interrupted rebuild tests.
- Confirm index files/caches are encrypted and wrong-key access fails.

## Acceptance criteria

- [ ] Results are correct, fast at target scale, and limited to selected profile.
- [ ] Searchable/excluded fields and leakage tradeoffs are approved in an ADR.
- [ ] Draft/unverified OCR is excluded by default.
- [ ] Delete/edit updates all index artifacts deterministically.
- [ ] Query/history/snippets never enter plaintext logs or state snapshots.
- [ ] Rebuild recovers from index corruption without changing source records.

## Exit gate

Security review approves the index leakage model and the representative performance/correctness suite passes.

