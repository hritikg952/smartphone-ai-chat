# ADR 0001 — Health Vault application shell

**Status:** Accepted for the private prototype
**Date:** 2026-07-19

## Context

The Android application is pivoting from a local AI chat prototype to a
local-first Personal Health Vault. The pivot must keep the app buildable while
preventing the retained chat, model, and camera runtime from controlling startup
or becoming a dependency of new health features.

## Decision

- Keep one Gradle application module during the foundation wave. New Health
  Vault code uses feature-oriented packages and domain repository contracts;
  module extraction will occur only when ownership or build measurements justify
  it.
- `App` owns `HealthVaultAppContainer` for the application lifetime. Activities
  consume its dependencies and never become dependencies of repositories.
- `AppSessionStore` is the public session boundary. Only onboarding completion
  is durable; unlock state is process-local and resets to locked after process
  death.
- Compose Navigation uses the allowlisted `AppRoute` set. `AppRoutePolicy`
  redirects fresh sessions to onboarding and every protected route to unlock
  while the vault is locked. Unknown paths are not routable.
- Feature state stays with feature owners. Global session state contains only
  onboarding and lock status; selected-profile and migration state will be added
  when their owning mini-goals introduce real behavior.
- The default debug/release artifacts omit legacy LiteRT and CameraX runtimes,
  permissions, and native libraries. The explicit `legacy` build retains those
  dependencies for migration work. `verifyHealthVaultArtifact` enforces the
  default artifact boundary.
- Background work is not created in MG-02. Later features must introduce it
  behind domain contracts with explicit lifetime and retry semantics.
- Feature errors stay in feature state; transient global events remain limited
  to safe, content-free notifications.

## Consequences

- The active app launches and is testable without an AI model, network, or
  camera runtime.
- Onboarding persistence is intentionally not credential security. Username and
  password fields are prototype UI only until MG-03 supplies authentication and
  key protection.
- Placeholder protected routes reserve ownership without introducing health
  data models or persistence ahead of their mini-goals.
- Legacy source remains available but cannot be reached through the default
  application container or packaged runtime.
