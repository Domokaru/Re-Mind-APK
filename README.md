# Re:MIND — Personal Mindfulness & Intelligent SMS Automation Platform

<p align="center">
  <img src="./public/app-icon.jpeg" alt="Re:MIND Icon" width="140" height="140" style="border-radius: 32px; box-shadow: 0 10px 30px rgba(0,0,0,0.35);" />
</p>

<p align="center">
  <strong>An intentional, privacy-first automation platform that nurtures meaningful human connections with your spouse, family, friends, and colleagues through guardrailed, non-repetitive SMS check-ins.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/React-19-blue?logo=react" alt="React 19" />
  <img src="https://img.shields.io/badge/TypeScript-5.x-blue?logo=typescript" alt="TypeScript" />
  <img src="https://img.shields.io/badge/Vite-8.x-646CFF?logo=vite" alt="Vite" />
  <img src="https://img.shields.io/badge/Tailwind_CSS-v4-38B2AC?logo=tailwind-css" alt="Tailwind CSS" />
  <img src="https://img.shields.io/badge/Android-Kotlin-7F52FF?logo=android" alt="Android" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Room_DB-SQLite-003B57?logo=sqlite" alt="Room DB" />
  <img src="https://img.shields.io/badge/License-Apache_2.0-green.svg" alt="License" />
</p>

---

## 1. Overview & Core Philosophy

In a fast-paced world, it is easy to let days and weeks slip by without checking in on the people who matter most. **Re:MIND** was engineered to eliminate relationship drift through quiet, thoughtful automation.

Unlike generic marketing auto-responders or intrusive chatbots, Re:MIND operates as an **invisible, intentional personal assistant**:
- **Authentic Voice**: Messages feel warm, human, and personal — never robotic or scripted.
- **Strict Privacy**: 100% client-side and device-authoritative. Your contact details, phone numbers, and communication history remain entirely in your private database.
- **Zero Accidental Overlaps**: Intelligent collision detection prevents reaching out if you have already messaged the recipient naturally that day.
- **Strict Boundaries**: Lexical guardrails prevent intimate language from ever reaching professional acquaintances or casual contacts.

---

## 2. Complete Functional Features

### 📱 Phone & Google Contacts Integration
- **One-Tap Contact Picker**: Features a dedicated **Pick Contact** action powered by the standard W3C Contact Picker API (`navigator.contacts.select`).
- **Direct System Integration**: On mobile devices (Android / Chrome) and installed PWAs, clicking **Pick Contact** immediately invokes your phone's native Google Contacts picker sheet.
- **Automatic Field Population**: Seamlessly extracts and normalizes the selected contact's display name and primary mobile number into standard international format (`+1...`).

### 🏷️ Adaptive Relationship Buckets
Contacts are organized into relationship categories, each with tailored messaging styles, color codes, and guardrails:
- **Spouse**: Deeply appreciative, warm, and affectionate connection check-ins.
- **Family**: Grounded, supportive check-ins acknowledging busy schedules and mutual care.
- **Friends**: Casual, uplifting, low-pressure touchpoints ("no need for a long reply").
- **Acquaintances**: Professional, courteous, friendly inquiries and check-ins.
- **Others**: Thoughtful, polite general reminders.
- **Custom User Buckets**: Create unlimited custom groups (e.g. *Mentors*, *Gym Crew*, *Volunteers*) with custom titles and high-contrast color palettes.

### 🛡️ Lexical "No-Love" Guardrail System
To completely prevent embarrassing social errors:
- The system enforces lexical boundary validation across all phrase pools and custom inputs.
- Words such as `love`, `beloved`, `darling`, `honey`, `sweetheart`, `kiss`, `babe`, `xoxo`, or `miss you` are strictly restricted to **Spouse** and **Family** groups.
- If a user attempts to add an affectionate phrase to **Acquaintances** or **Others**, the lexical engine rejects it with a clear explanation.

### 🔄 Anti-Repetition Cycle Engine
- Every relationship bucket maintains a rotation pool of thoughtful messages.
- The dispatch engine tracks phrase usage on a per-contact basis, guaranteeing that no message is reused until all eligible phrases in that pool have been sent.

### ⏰ Flexible Recurrence & Random Window Dispatching
- **Cadence Types**:
  - `DAILY`: Everyday check-ins (e.g. for a spouse).
  - `WEEKLY`: Specific days of the week (e.g. every Tuesday and Friday).
  - `EVERY_N_DAYS`: Interval-based recurrence (e.g. every 3 days).
  - `EVERY_N_WEEKS`: Multi-week cadences (e.g. every 2 weeks on Thursday).
  - `MONTHLY_DAY`: Specific day of each month (e.g. the 15th).
- **Humanized Timing**:
  - `RANDOM_WINDOW_1HR`: Rather than sending messages at an exact, robotic second (e.g. precisely 2:00:00 PM), the engine chooses a random minute within a designated 1-hour window (e.g. 2:24 PM, 2:47 PM) so check-ins feel natural.
  - `EXACT_TIME`: Optional specific time scheduling when precision is preferred.

### 🔒 Next-1 Locked-In Pipeline & 6 Manual Overrides
For every active connection, Re:MIND pre-computes and locks in the exact upcoming message, scheduled date, and target time. Tapping the card opens a bottom action sheet with **6 granular manual overrides**:
1. 🚀 **Send Now**: Dispatches the pre-rolled message immediately and rolls forward to the next recurrence.
2. 🎲 **Reroll Phrase**: Immediately swaps out the current phrase with another fresh candidate from the bucket pool.
3. ✏️ **Edit One-Off Text**: Edit the text specifically for this single occurrence (e.g. adding a reference to yesterday's dinner) without modifying the reusable phrase pool.
4. ⏭️ **Skip Occurrence**: Skips this scheduled cycle without dispatching and rolls forward to the next cycle.
5. 📅 **Reschedule**: Postpone or move the occurrence to a different date or time.
6. ⏸️ **Pause / Resume**: Toggle an individual contact schedule or use global Vacation Mode.

### 📅 Calendar & Collapsible Agenda
- **Monthly Overview Grid**: Displays your whole month with colored category indicator dots corresponding to each scheduled occurrence.
- **Collapsible Agenda Cards**: Upcoming items are neatly collapsed down to the recipient's name and relationship badge, keeping the view clean.
- **Direct Pen Action Launcher**: A dedicated pen icon button allows instant access to manual actions without having to read through large text blocks.
- **Expandable Details**: Tap any agenda card to expand and view the exact pre-rolled message, phone number, and scheduled time.
- **Virtual Cadence Projections**: Projects future occurrences months into the future based on recurrence rules.

### 🛑 Dual-Layer Collision Avoidance & Quiet Hours
- **Same-Day Dual-Layer Detector**: Queries device telephony / messaging providers. If you already had an active SMS or RCS exchange with a contact earlier that day, the automated dispatch is silently skipped to prevent awkward double-texting.
- **Blackout Window**: Enforces a strict 11:00 PM – 7:00 AM quiet window. Any dispatches landing in this window are automatically adjusted to respectable daytime hours.

### ✈️ Vacation Mode & Global Pause
- One-tap global pause button in Settings immediately suspends all automated dispatches.
- A prominent status banner indicates vacation mode is active, with an instant **Resume** toggle.

### 💾 Offline Backup & Full JSON Portability
- Export your entire database (contacts, custom phrase groups, logs, settings) as a standard JSON file.
- Restore your complete configuration on any device with one click.
- Built-in "Reset to Factory Seed" action safely restores the clean starting state.

---

## 3. System Architecture & Codebase Structure

Re:MIND is structured with a **clean, decoupled architecture** supporting both a modern web/PWA interface and a native Android background service.

```
├── public/                       # Static web assets & PWA manifest
│   ├── app-icon.jpeg             # Master application brand icon
│   ├── favicon.jpeg              # Web browser tab icon
│   └── manifest.json             # Web App Manifest for mobile installation
├── src/                          # Web / PWA Application Layer (React + TS)
│   ├── components/               # Reusable UI Components
│   │   ├── BucketBadge.tsx       # Adaptive color bucket badges
│   │   ├── ContactEditModal.tsx  # Add/Edit Connection modal with Contact Picker
│   │   ├── CreateBucketModal.tsx # Dynamic custom bucket creation modal
│   │   ├── PhraseModal.tsx       # Custom phrase authoring modal with guardrails
│   │   └── UpcomingActionSheet.tsx # 6-action bottom modal for scheduled messages
│   ├── core/                     # Platform-Agnostic Business Logic
│   │   ├── guardrail.ts          # Lexical validation & forbidden term matching
│   │   ├── recurrence.ts         # Occurrence calculation & virtual projections
│   │   └── seedData.ts           # 50 default phrase rotation templates
│   ├── screens/                  # Top-Level Views
│   │   ├── DashboardView.tsx     # Upcoming Next-1 feed & recent activity logs
│   │   ├── CalendarView.tsx      # Month grid, day agenda, & projections
│   │   ├── PeopleView.tsx        # Contact schedule management & bucket filters
│   │   ├── PhrasesView.tsx       # Phrase pool CRUD & bucket management
│   │   └── SettingsView.tsx      # Theme, vacation mode, JSON backup/restore
│   ├── state/
│   │   └── remindStore.ts        # Reactive centralized state hook & localStorage sync
│   ├── types/
│   │   └── remind.ts             # Strict TypeScript domain interfaces
│   ├── App.tsx                   # Main layout container & bottom icon dock
│   ├── index.css                 # Global Tailwind CSS imports & theme variables
│   └── main.tsx                  # React DOM entry point
├── app/                          # Native Android Companion Architecture (Kotlin)
│   ├── src/main/java/com/hallauer/remind/
│   │   ├── core/                 # Android recurrence, guardrails, & constants
│   │   ├── data/
│   │   │   ├── local/            # Room Database (DAOs, Entities, Converters)
│   │   │   ├── provider/         # SmsDispatcher & SmsCollisionDetector
│   │   │   └── repository/       # RemindRepository
│   │   ├── service/              # BroadcastReceivers & AlarmManager receivers
│   │   │   └── worker/           # WorkManager background dispatch workers
│   │   └── ui/                   # Jetpack Compose Material 3 screens & theme
│   └── build.gradle.kts          # Android build configuration
├── index.html                    # HTML5 entry point with OpenGraph & PWA meta
├── package.json                  # Dependencies & npm scripts
├── tsconfig.json                 # TypeScript compiler configuration
└── vite.config.ts                # Vite build configuration
```

---

## 4. Domain Data Models

```typescript
// Core Connection Schedule
interface ContactSchedule {
  id: number;
  contactLookupKey: string;
  contactId: number;
  displayName: string;
  phoneNumber: string;
  normalizedNumber: string;       // E.164 normalized
  bucket: GroupBucket;            // SPOUSE | FAMILY | FRIENDS | ACQUAINTANCES | OTHERS | Custom
  recurrenceType: RecurrenceType; // DAILY | WEEKLY | EVERY_N_DAYS | EVERY_N_WEEKS | MONTHLY_DAY
  intervalN: number;
  daysOfWeekCsv: string;          // e.g. "1,4" for Mon, Thu
  dayOfMonth: number;
  timeMode: TimeMode;             // RANDOM_WINDOW_1HR | EXACT_TIME
  targetHour: number;             // 7 to 22 (7:00 AM - 10:00 PM)
  targetMinute: number;
  isActive: boolean;
  lastSentPhraseId?: number;
  lastSentPhraseText?: string;
  lastDispatchedDateIso?: string;
}

// Next-1 Locked-In Occurrence & History Log
interface MessageLog {
  id: number;
  contactScheduleId: number;
  contactDisplayName: string;
  contactPhoneNumber: string;
  bucket: GroupBucket;
  phraseId?: number;
  messageText: string;
  isCustomEdited: boolean;
  scheduledDateIso: string;
  scheduledEpochMillis: number;
  executedEpochMillis?: number;
  status: 'PENDING' | 'SENT' | 'SKIPPED_MANUAL' | 'SKIPPED_COLLISION' | 'CANCELLED';
  statusNote?: string;
}
```

---

## 5. Getting Started & Development

### Web / PWA Environment

```bash
# 1. Clone the repository
git clone https://github.com/<your-username>/remind-mindfulness-sms.git
cd remind-mindfulness-sms

# 2. Install dependencies
npm install

# 3. Start development server (Port 3000)
npm run dev

# 4. Type check and lint
npm run lint

# 5. Build production bundle
npm run build
```

### Automated APK Build via GitHub Actions (Recommended)

This repository includes a preconfigured GitHub Actions workflow (`.github/workflows/build-apk.yml`) that compiles the Android APK in the cloud:

1. **Automatic Build**: Whenever you push code to `main`, GitHub Actions automatically builds both the **Debug APK** and **Sideload-Ready Release APK**.
2. **Manual Trigger**:
   - Go to your repository on GitHub.
   - Click the **Actions** tab at the top.
   - In the left sidebar, click **Build Android APK**.
   - Click **Run workflow** > select `main` > click the green **Run workflow** button.
3. **Download the APK**:
   - Click on the completed workflow run (with the green checkmark).
   - Scroll down to the **Artifacts** section at the bottom of the page.
   - Click **`remind-release-apk`** or **`remind-debug-apk`** to download the ZIP containing your compiled APK file.
   - Transfer or open the `.apk` directly on your Android phone to install and run!

### Local Android Studio Build

1. Open the project in **Android Studio**.
2. Select the `app` module.
3. Sync Gradle dependencies.
4. Run or assemble the APK:
   ```bash
   gradle :app:assembleDebug
   # Output: app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 6. Privacy & Security Statement

Re:MIND was created with personal privacy as an absolute priority:
- **No Third-Party Analytics**: No telemetry, tracking pixels, or user behavior tracking.
- **Client-Side Storage**: Contacts and schedules are kept on your physical device.
- **Direct Dispatch**: Messages are routed directly through your carrier's local telephony interface.
- **Data Ownership**: You can export, inspect, or delete your entire database at any time.

---

## 7. License

Distributed under the **Apache-2.0 License**. See `LICENSE` for details.
