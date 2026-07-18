# Official Reference Baseline

These references are inputs, not substitutes for specialist review. Re-check them when implementing because platform and policy requirements change. Last checked 2026-07-18.

## Android security and identity

- [Android cryptography guidance](https://developer.android.com/privacy-and-security/cryptography)
- [Android Keystore system](https://developer.android.com/privacy-and-security/keystore)
- [Biometric authentication](https://developer.android.com/identity/sign-in/biometric-auth)
- [Android backup controls](https://developer.android.com/identity/data/autobackup)

## Health Connect and Google Play

- [Health Connect availability](https://developer.android.com/health-and-fitness/health-connect/availability)
- [Health Connect data types and permissions](https://developer.android.com/health-and-fitness/health-connect/data-types)
- [Health Connect permission UX](https://developer.android.com/health-and-fitness/health-connect/ui/permissions)
- [Google Play health-app declaration](https://support.google.com/googleplay/android-developer/answer/14738291)
- [Google Play health content and services policy](https://support.google.com/googleplay/android-developer/answer/16679511)
- [Google Play Data safety declaration](https://support.google.com/googleplay/android-developer/answer/10787469)

## Emergency passes

- [Google Wallet generic private passes](https://developers.google.com/wallet/generic-private-pass)

Sensitive health-related passes require the private-pass route and explicit issuer approval. Treat Wallet delivery as an external integration with onboarding and service requirements, not a local UI feature.

## Health privacy and medical-function boundaries

- [FTC Health Breach Notification Rule compliance](https://www.ftc.gov/business-guidance/resources/complying-ftcs-health-breach-notification-rule-0)
- [FDA clinical decision-support guidance FAQ](https://www.fda.gov/medical-devices/software-medical-device-samd/clinical-decision-support-software-frequently-asked-questions-faqs)

Applicability depends on intended use, data flows, organization type, and launch jurisdiction. HIPAA is not automatically applicable to every consumer health app, and lack of HIPAA applicability does not remove privacy/security duties.

## India-first planning

- [Digital Personal Data Protection Act, 2023 — India Code](https://www.indiacode.nic.in/indiacode/handle/123456789/22037?view_type=browse)
- [DPDP Rules 2025 and enforcement timeline — MeitY](https://www.meity.gov.in/documents/act-and-policies/digital-personal-data-protection-rules-2025-gDOxUjMtQWa)
- [Ayushman Bharat Digital Mission overview](https://abdm.gov.in/abdm)
- [ABDM health-data consent FAQ](https://abdm.gov.in/FAQ)

The private local prototype does not need to integrate with ABHA/ABDM. If the product later accesses, shares, or interoperates with external health data, treat consent, privacy, and interoperability as a separate workstream.
