# UI Design Generation Prompt

Copy the prompt below into a UI design generator or Figma AI tool. It is for design generation only; do not generate a design in this planning session.

```text
Create a high-fidelity Android mobile UI design system and seven connected screen designs for an app named “Personal Health Vault.”

Product context
- A private, local-first personal health-record organizer for one person.
- This is an early prototype, not a hospital app, diagnosis tool, or fitness social network.
- The user manually organizes medications, medical reports, vital readings, allergies, immunizations, and personal notes.
- On-device document scanning/OCR will be added later, so show a clear “Scan report” entry point but do not make AI/chat the main experience.
- The existing AI chat is archived for later: do not include a chatbot, prompt box, assistant panel, social feed, doctor messaging, payment, insurance, wearable syncing, Google Wallet, or sharing features.

Visual direction
- Strictly dark Android interface, premium, calm, private, and clinically organized.
- Use Material Design 3 principles with a polished native Android feel, not iOS styling.
- Canvas size: Android phone, 360 x 800 dp artboards.
- Background: near-black charcoal #0B0F14. Surface cards: #121820 and #18212C. Elevated surface: #202B38.
- Primary accent: muted teal #4FD1C5. Secondary accent: soft blue #7AA7FF. Warning: amber #F6C453. Critical status: restrained coral #FF7A70.
- Text: warm white #F4F7FA, secondary text #AAB6C5, disabled #667382.
- No light backgrounds, glassmorphism, neon glow, excessive shadows, or saturated red/green-only status indicators. A single low-opacity teal radial depth treatment is allowed on the access screen only; avoid bright or decorative gradients elsewhere.
- Use an 8 dp spacing grid, 16 dp side padding, 14–18 dp rounded cards, large readable type, generous touch targets, and clean outlined icons.
- Make accessibility visible: strong contrast, large labels, icons paired with text where important, chart values readable without relying only on color.

Information architecture
- Bottom navigation: Home, Records, Add, Settings.
- Home is the landing screen. Records is the content library. Add is a fast-action sheet. Settings includes local-profile and app preferences.
- Present a single self profile only. Do not show account switching, dependents, cloud sync, or biometric controls.

Design these seven screens as one coherent flow

1. Local access screen
- Personal Health Vault wordmark and subtle shield/health icon. “Health Vault” may be used only as a compact app-bar label.
   - Username and password fields, “Unlock vault” primary button, small prototype/privacy notice.
   - Keep it calm and minimal; no social-login buttons.

2. Home dashboard
   - Greeting, current date, compact profile chip, and overflow menu.
   - Prominent “Today” area with medication schedule placeholder, quick vital entry, and scan report action.
   - Recent records list and a small weight trend card.
   - Empty states feel reassuring and useful, not empty or playful.

3. Records hub
   - Search field at top, filter chips, and categorized cards for Medications, Reports, Vitals, Allergies, Immunizations, and Notes.
   - Each category shows an icon, concise description, recent count, and last-updated detail.

4. Medication list and detail
   - Medication list with active/discontinued segmented control, dosage/time chips, and clear status labels.
   - Medication detail shows name, dosage, schedule, start date, prescriber placeholder, notes, and a chronological history section.
   - Design for structured personal record keeping, not medical advice.

5. Reports timeline and scan entry
   - Chronological report cards with date, report type, provider placeholder, page count, and source badge.
   - Primary “Scan report” button and secondary “Add from files” action.
   - Show an OCR-review placeholder state labeled “Review extracted details” with confidence/provenance styling, clearly marked as draft.

6. Vitals and trends
   - Weight trend chart on dark card with readable axes, selected point value, date range control, and source label.
   - Recent measurement list for weight, height, blood pressure, heart rate, temperature, and glucose placeholders.
   - Use text and icons in addition to colors for normal, attention, and missing states.

7. Add-record sheet and settings
   - Add-record bottom sheet with large actions: Medication, Vital reading, Medical report, Allergy, Immunization, Note.
   - Settings screen includes local profile, theme set to dark, local diagnostics, archive chat entry, and a clearly disabled “Future integrations” section.

Content and interaction details
- Use fictional, non-identifying sample content only: “Morning medication,” “Annual blood panel,” “Weight 72.4 kg,” “No known allergies recorded.”
- Show realistic Android system bars, keyboard-safe layouts, loading/empty/error states, and subtle pressed/selected states.
- Make the design implementation-ready for Jetpack Compose: reusable cards, chips, list rows, bottom sheets, top bars, buttons, form fields, and chart components.
- Deliver the result as a consistent dark design system plus the seven mobile artboards, with a component reference strip showing colors, typography, buttons, inputs, chips, cards, and status styles.
```
