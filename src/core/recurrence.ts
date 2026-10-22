import { ContactSchedule, VirtualCalendarOccurrence } from '../types/remind';

export interface CalculatedOccurrenceResult {
  targetDate: Date;
  epochMillis: number;
  dateIso: string; // YYYY-MM-DD
  lockedMinute: number;
  timeSummary: string;
}

/**
 * Computes the exact next recurrence timestamp for a contact.
 * Enforces the 7:00 AM double-text recovery skip rule:
 * If the contact already had a dispatch today (e.g. held by blackout and fired at 7 AM),
 * start searching strictly from tomorrow.
 */
export const computeNextOccurrence = (
  schedule: ContactSchedule,
  fromDate: Date = new Date()
): CalculatedOccurrenceResult => {
  const today = new Date(fromDate);
  today.setHours(0, 0, 0, 0);

  const todayIso = today.toISOString().split('T')[0];
  const lastDispatchedIso = schedule.lastDispatchedDateIso;

  // Clamp target hour to safe waking window: 7..22 (7:00 AM to 10:00 PM)
  const clampedHour = Math.min(22, Math.max(7, schedule.targetHour));

  // Lock in minute
  const lockedMinute =
    schedule.timeMode === 'EXACT_TIME'
      ? Math.min(59, Math.max(0, schedule.targetMinute))
      : Math.floor(Math.random() * 60);

  // If already dispatched today, start searching strictly from tomorrow
  const startSearchDate = new Date(today);
  if (lastDispatchedIso && lastDispatchedIso === todayIso) {
    startSearchDate.setDate(startSearchDate.getDate() + 1);
  }

  const nextDate = findNextRecurrenceDate(
    schedule,
    startSearchDate,
    clampedHour,
    lockedMinute,
    fromDate
  );

  const targetDateTime = new Date(nextDate);
  targetDateTime.setHours(clampedHour, lockedMinute, 0, 0);

  const dateIso = targetDateTime.toISOString().split('T')[0];

  const timeSummary = formatTimeSummary(schedule, clampedHour, lockedMinute);

  return {
    targetDate: targetDateTime,
    epochMillis: targetDateTime.getTime(),
    dateIso,
    lockedMinute,
    timeSummary,
  };
};

const findNextRecurrenceDate = (
  schedule: ContactSchedule,
  startSearchFrom: Date,
  targetHour: number,
  targetMinute: number,
  now: Date
): Date => {
  const candidate = new Date(startSearchFrom);

  switch (schedule.recurrenceType) {
    case 'DAILY': {
      const candidateTime = new Date(candidate);
      candidateTime.setHours(targetHour, targetMinute, 0, 0);
      if (candidateTime > now) return candidate;
      candidate.setDate(candidate.getDate() + 1);
      return candidate;
    }

    case 'EVERY_N_DAYS': {
      const interval = Math.max(1, schedule.intervalN || 1);
      const candidateTime = new Date(candidate);
      candidateTime.setHours(targetHour, targetMinute, 0, 0);
      if (candidateTime > now) return candidate;
      candidate.setDate(candidate.getDate() + interval);
      return candidate;
    }

    case 'WEEKLY': {
      const allowedDays = parseDaysOfWeek(schedule.daysOfWeekCsv);
      const test = new Date(candidate);
      for (let i = 0; i < 14; i++) {
        // JS getDay(): 0=Sun, 1=Mon... ISO: 1=Mon..7=Sun
        const isoDay = test.getDay() === 0 ? 7 : test.getDay();
        if (allowedDays.includes(isoDay)) {
          const testTime = new Date(test);
          testTime.setHours(targetHour, targetMinute, 0, 0);
          if (testTime > now) return test;
        }
        test.setDate(test.getDate() + 1);
      }
      candidate.setDate(candidate.getDate() + 7);
      return candidate;
    }

    case 'EVERY_N_WEEKS': {
      const allowedDays = parseDaysOfWeek(schedule.daysOfWeekCsv);
      const targetIsoDay = allowedDays[0] || 1; // Default Monday
      const intervalWeeks = Math.max(1, schedule.intervalN || 1);

      const test = new Date(candidate);
      while ((test.getDay() === 0 ? 7 : test.getDay()) !== targetIsoDay) {
        test.setDate(test.getDate() + 1);
      }

      const testTime = new Date(test);
      testTime.setHours(targetHour, targetMinute, 0, 0);
      if (testTime > now) return test;

      test.setDate(test.getDate() + 7 * intervalWeeks);
      return test;
    }

    case 'MONTHLY_DAY': {
      const test = new Date(candidate);
      for (let m = 0; m < 12; m++) {
        const year = test.getFullYear();
        const month = test.getMonth();
        const daysInMonth = new Date(year, month + 1, 0).getDate();

        const resolvedDay =
          schedule.dayOfMonth === -1
            ? daysInMonth
            : Math.min(daysInMonth, Math.max(1, schedule.dayOfMonth));

        const testDate = new Date(year, month, resolvedDay, targetHour, targetMinute);
        if (testDate > now) return testDate;

        test.setMonth(test.getMonth() + 1);
        test.setDate(1);
      }
      return candidate;
    }

    case 'YEARLY_DATE': {
      const targetMonth = Math.min(11, Math.max(0, (schedule.monthOfYear || 1) - 1));
      const targetDay = Math.min(31, Math.max(1, schedule.dayOfMonth || 1));
      let year = candidate.getFullYear();

      for (let y = 0; y < 5; y++) {
        const daysInMonth = new Date(year, targetMonth + 1, 0).getDate();
        const resolvedDay = Math.min(daysInMonth, targetDay);
        const testDate = new Date(year, targetMonth, resolvedDay, targetHour, targetMinute);
        if (testDate > now) return testDate;
        year++;
      }
      return candidate;
    }

    default:
      return candidate;
  }
};

const parseDaysOfWeek = (csv: string): number[] => {
  if (!csv || !csv.trim()) return [1]; // Mon default
  return csv
    .split(',')
    .map((s) => parseInt(s.trim(), 10))
    .filter((n) => !isNaN(n) && n >= 1 && n <= 7);
};

export const formatTimeSummary = (
  schedule: ContactSchedule,
  hour: number,
  minute: number
): string => {
  const amPm = hour >= 12 ? 'PM' : 'AM';
  const h12 = hour % 12 === 0 ? 12 : hour % 12;

  if (schedule.timeMode === 'EXACT_TIME') {
    return `${h12}:${minute.toString().padStart(2, '0')} ${amPm}`;
  } else {
    const endH = hour + 1;
    const endAmPm = endH >= 12 ? 'PM' : 'AM';
    const endH12 = endH % 12 === 0 ? 12 : endH % 12;
    return `${h12} ${amPm} - ${endH12} ${endAmPm} Window`;
  }
};

/**
 * Projects subsequent occurrences beyond the single locked-in PENDING occurrence
 * across the calendar month.
 */
export const projectFutureOccurrences = (
  schedule: ContactSchedule,
  lockedPendingDateIso: string,
  year: number,
  month: number // 0-indexed (0=Jan)
): VirtualCalendarOccurrence[] => {
  if (!schedule.isActive) return [];

  const monthStart = new Date(year, month, 1);
  const monthEnd = new Date(year, month + 1, 0);
  const lockedDate = new Date(lockedPendingDateIso + 'T00:00:00');

  const results: VirtualCalendarOccurrence[] = [];
  const cursor = new Date(lockedDate);
  cursor.setDate(cursor.getDate() + 1);

  const timeSummary = formatTimeSummary(schedule, schedule.targetHour, schedule.targetMinute);

  let iterations = 0;
  while (cursor <= monthEnd && iterations < 365) {
    iterations++;
    const nextDate = findNextRecurrenceDate(
      schedule,
      cursor,
      schedule.targetHour,
      0,
      new Date(cursor.getTime() - 1000)
    );

    if (nextDate > monthEnd) break;

    const dateIso = nextDate.toISOString().split('T')[0];
    if (nextDate >= monthStart && dateIso > lockedPendingDateIso) {
      results.push({
        contactScheduleId: schedule.id,
        displayName: schedule.displayName,
        bucket: schedule.bucket,
        projectedDateIso: dateIso,
        timeSummary,
        placeholderNote: 'Phrase will roll after previous message executes',
      });
    }

    cursor.setTime(nextDate.getTime() + 86400000);
  }

  return results;
};
