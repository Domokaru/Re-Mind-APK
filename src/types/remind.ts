export type GroupBucket =
  | 'SPOUSE'
  | 'FAMILY'
  | 'FRIENDS'
  | 'ACQUAINTANCES'
  | 'OTHERS'
  | (string & {});

export type ThemeMode = 'DARK' | 'LIGHT';

// Global Dark Mode Surfaces (Default)
export const DarkBackground = '#0B0F19';
export const DarkSurface = '#111827';
export const DarkSurfaceVariant = '#1E293B';
export const DarkPrimary = '#38BDF8';
export const DarkOnPrimary = '#0F172A';
export const DarkTextPrimary = '#F8FAFC';
export const DarkTextSecondary = '#94A3B8';

// Global Light Mode Surfaces
export const LightBackground = '#F8FAFC';
export const LightSurface = '#FFFFFF';
export const LightSurfaceVariant = '#F1F5F9';
export const LightPrimary = '#0284C7';
export const LightOnPrimary = '#FFFFFF';
export const LightTextPrimary = '#0F172A';
export const LightTextSecondary = '#64748B';

export interface BucketMetadata {
  id: string;
  displayName: string;
  allowsLoveWord: boolean;
  darkColorHex: string;
  lightColorHex: string;
  isCustom?: boolean;
}

export const DEFAULT_GROUP_BUCKETS: Record<string, BucketMetadata> = {
  SPOUSE: {
    id: 'SPOUSE',
    displayName: 'Spouse',
    allowsLoveWord: true,
    darkColorHex: '#E11D48',
    lightColorHex: '#BE123C',
  },
  FAMILY: {
    id: 'FAMILY',
    displayName: 'Family',
    allowsLoveWord: true,
    darkColorHex: '#D97706',
    lightColorHex: '#B45309',
  },
  FRIENDS: {
    id: 'FRIENDS',
    displayName: 'Friends',
    allowsLoveWord: false,
    darkColorHex: '#2563EB',
    lightColorHex: '#1D4ED8',
  },
  ACQUAINTANCES: {
    id: 'ACQUAINTANCES',
    displayName: 'Acquaintances',
    allowsLoveWord: false,
    darkColorHex: '#0D9488',
    lightColorHex: '#0F766E',
  },
  OTHERS: {
    id: 'OTHERS',
    displayName: 'Others',
    allowsLoveWord: false,
    darkColorHex: '#7C3AED',
    lightColorHex: '#6D28D9',
  },
};

export const GROUP_BUCKETS: Record<string, BucketMetadata> = {
  ...DEFAULT_GROUP_BUCKETS,
};

export function registerCustomBucket(bucket: BucketMetadata) {
  GROUP_BUCKETS[bucket.id] = bucket;
}

/**
 * Returns the adaptive color for each GroupBucket based on the active theme mode.
 */
export function getBucketColor(bucket: string, isDark: boolean = true): string {
  const meta = GROUP_BUCKETS[bucket];
  if (!meta) return isDark ? '#38BDF8' : '#0284C7';
  return isDark ? meta.darkColorHex : meta.lightColorHex;
}

export type RecurrenceType =
  | 'DAILY'
  | 'EVERY_N_DAYS'
  | 'WEEKLY'
  | 'EVERY_N_WEEKS'
  | 'MONTHLY_DAY'
  | 'YEARLY_DATE';

export type TimeMode = 'EXACT_TIME' | 'RANDOM_WINDOW_1HR';

export type MessageStatus =
  | 'PENDING'
  | 'SENT'
  | 'SKIPPED_COLLISION'
  | 'SKIPPED_MANUAL'
  | 'FAILED';

export interface ContactSchedule {
  id: number;
  contactLookupKey: string;
  contactId: number;
  displayName: string;
  phoneNumber: string;
  normalizedNumber: string;
  photoUri?: string;
  bucket: GroupBucket;
  recurrenceType: RecurrenceType;
  intervalN: number;
  daysOfWeekCsv: string; // e.g. "1,4" (Mon, Thu)
  dayOfMonth: number;
  monthOfYear: number;
  timeMode: TimeMode;
  targetHour: number; // 7..22
  targetMinute: number; // 0..59
  isActive: boolean;
  pausedReason?: string;
  lastSentPhraseId?: number;
  lastSentPhraseText?: string;
  lastDispatchedDateIso?: string; // "YYYY-MM-DD"
  lastManualInteractionEpochMillis: number; // For simulated RCS/SMS collision
  createdAtEpochMillis: number;
  updatedAtEpochMillis: number;
}

export interface Phrase {
  id: number;
  bucket: GroupBucket;
  text: string;
  isSeed: boolean;
  createdAtEpochMillis: number;
}

export interface MessageLog {
  id: number;
  contactScheduleId: number;
  contactDisplayName: string;
  contactPhoneNumber: string;
  bucket: GroupBucket;
  phraseId?: number;
  messageText: string;
  isCustomEdited: boolean;
  scheduledDateIso: string; // "YYYY-MM-DD"
  scheduledEpochMillis: number;
  executedEpochMillis?: number;
  status: MessageStatus;
  statusNote?: string;
}

export interface VirtualCalendarOccurrence {
  contactScheduleId: number;
  displayName: string;
  bucket: GroupBucket;
  projectedDateIso: string;
  timeSummary: string;
  placeholderNote: string;
}
