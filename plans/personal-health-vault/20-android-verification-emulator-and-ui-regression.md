# MG-20 — Android Verification, Emulator, and UI-Regression Workflow

## Outcome

Give development a repeatable feedback loop: build, unit-test, launch on an Android emulator, exercise core flows, collect logs, and compare screenshots before calling a UI feature done.

## Dependencies

MG-02, MG-06; decision D-020.

## Why this exists

Android has more execution layers than a typical web app: Kotlin compilation, Android resource packaging, device installation, runtime permissions, device configuration, and rendering on a real Android system. Unit tests catch logic errors, but they cannot prove a Compose screen renders or navigation works on a device.

The equivalent of browser-based development is a managed Android Virtual Device (AVD): a locally installed emulator image that can receive a debug APK, run tests, produce screenshots, and expose logs through Android Debug Bridge (ADB).

## Required verification ladder

1. **Static/build check:** `./gradlew assembleDebug` catches compilation, resources, manifest, and packaging errors.
2. **Unit tests:** `./gradlew testDebugUnitTest` exercises domain, repository, and ViewModel behavior quickly on the JVM.
3. **Device/UI tests:** `./gradlew connectedDebugAndroidTest` runs Compose/Espresso tests on an emulator or attached device.
4. **Visual smoke test:** install/launch the debug app, navigate defined flows, capture screenshots, and inspect Logcat for crashes or warnings.
5. **Regression evidence:** retain named screenshots/test reports for critical unlock, home, records, medication, and document flows.

## Test environment requirements

- Install Android SDK command-line tools, platform tools (`adb`), emulator, a system image, and one named AVD.
- Use a fixed API level and phone profile initially; add a smaller screen, a larger screen, and at least one physical-device check before release.
- Keep the emulator configuration documented in the repository or CI setup: API level, ABI, screen dimensions/density, locale, font scale, dark mode, animation policy, and test seed.
- Debug builds must use deterministic fake repositories/data and avoid downloading the legacy model or requiring network access.
- Add accessibility semantics/content descriptions to interactive Compose UI so device tests can locate controls reliably.

## Autonomous development workflow

For each implementation slice:

1. Run focused JVM tests and `assembleDebug`.
2. Boot or reuse the named emulator; install the debug APK.
3. Seed synthetic data only, then exercise the changed route through automated Compose tests or ADB-driven smoke steps.
4. Capture an emulator screenshot and compare it against the approved Figma direction and expected state.
5. Pull filtered crash/ANR logs; fix failures before continuing.
6. Run the full connected suite for cross-cutting changes and record the command/result in the implementation ticket or PR.

An agent can perform these steps when the SDK/AVD and ADB are available in its execution environment. It can build, install, launch, run connected tests, collect screenshots and UI hierarchy, and inspect logs. Visual judgement still benefits from screenshot review; device-only system flows, biometrics, camera behavior, and final performance must also be checked on a real device.

## Work packages

1. Provision and document the shared development AVD; add a health check command that verifies `adb`, emulator availability, and a booted device.
2. Add Android test dependencies and a `src/androidTest` suite for navigation and critical Compose interactions.
3. Add deterministic fixture/seed repositories for all prototype visual states; never seed real health data.
4. Define screenshot capture naming and approval process, including Figma comparison points.
5. Add a small smoke-test script or Gradle task that installs, launches, waits for idle, captures a screenshot, and collects filtered logs.
6. Configure CI emulator tests only after the local lane is stable; cache SDK/Gradle dependencies and publish reports/screenshots on failure.
7. Establish a release device matrix: API range, screen size, dark mode, font scaling, rotation, process recreation, offline mode, and low-storage behavior.

## Tests

- Cold start, process recreation, rotation, dark theme, and back-stack tests.
- Locked-vault routing: no protected content renders before a valid unlock.
- Navigation, form validation, profile switching, deletion confirmation, and error-state Compose tests.
- Screenshot tests or captured visual checks for Figma-aligned critical screens.
- Offline/airplane-mode tests prove the core vault needs no network.
- Manual physical-device checks for keyboard, biometric prompt, camera/document picker, accessibility services, and performance.

## Acceptance criteria

- [ ] A documented AVD can launch the app from a clean checkout.
- [x] `assembleDebug`, JVM tests, and connected UI tests have clear standard commands.
- [ ] A changed UI route has automated interaction coverage and screenshot evidence.
- [ ] Build/test scripts surface failures and do not rely on manually opening Android Studio.
- [x] Real-device checks are listed as a release gate for platform-dependent features.

## Implementation evidence — 2026-07-19

Status: **In progress**.

- AndroidX test runner, Espresso, Compose UI test, and debug test-manifest dependencies are configured.
- `PersonalHealthVaultAppTest` covers the fresh-start welcome state and the Get started → Credentials interaction through the real `OnboardingViewModel` seam.
- The focused suite passed two tests on `Pixel_10(AVD) - 17`; a clean onboarding screenshot was captured and inspected during the session.
- The full JVM test task and debug assembly pass. `git diff --check` also passes.
- The first device attempt encountered an offline ADB target and ran zero tests; reconnecting the emulator resolved the infrastructure failure and the same suite passed.
- The exact AVD configuration, repository-owned smoke script, retained screenshot convention, broader device matrix, and physical-device checks remain open, so the exit gate is not complete.

## Exit gate

Every feature team can validate changes independently before handoff, while Android Studio remains an optional visual/debugging tool rather than the only way to discover broken UI.
