import { useState, useEffect, useCallback } from 'react';
import {
  ContactSchedule,
  MessageLog,
  Phrase,
  GroupBucket,
  ThemeMode,
  BucketMetadata,
  DEFAULT_GROUP_BUCKETS,
  GROUP_BUCKETS,
  registerCustomBucket,
} from '../types/remind';
import { SEED_PHRASES, INITIAL_SAMPLE_CONTACTS } from '../core/seedData';
import { computeNextOccurrence } from '../core/recurrence';
import { validateLoveGuardrail } from '../core/guardrail';

export interface RemindState {
  themeMode: 'DARK' | 'LIGHT';
  isGlobalPauseEnabled: boolean;
  contacts: ContactSchedule[];
  phrases: Phrase[];
  pendingLogs: MessageLog[];
  historyLogs: MessageLog[];
  buckets?: BucketMetadata[];
}

const STORAGE_KEY = 'remind_app_state_v2';

const isLegacySampleContact = (c: ContactSchedule) => {
  const key = c.contactLookupKey || '';
  return (
    key.startsWith('lookup_sarah_') ||
    key.startsWith('lookup_mom_') ||
    key.startsWith('lookup_marcus_') ||
    key.startsWith('lookup_dave_') ||
    key.startsWith('lookup_greg_') ||
    c.displayName === 'Sarah Hallauer' ||
    c.displayName === 'Mom (Linda)' ||
    c.displayName === 'Marcus Vance' ||
    c.displayName === 'Dave Miller (Architect)' ||
    c.displayName === 'Greg Kowalski'
  );
};

export function useRemindStore() {
  const [themeMode, setThemeMode] = useState<'DARK' | 'LIGHT'>('DARK');
  const [isGlobalPauseEnabled, setIsGlobalPauseEnabled] = useState<boolean>(false);
  const [buckets, setBuckets] = useState<BucketMetadata[]>(() => Object.values(DEFAULT_GROUP_BUCKETS));
  const [contacts, setContacts] = useState<ContactSchedule[]>([]);
  const [phrases, setPhrases] = useState<Phrase[]>([]);
  const [pendingLogs, setPendingLogs] = useState<MessageLog[]>([]);
  const [historyLogs, setHistoryLogs] = useState<MessageLog[]>([]);
  const [initialized, setInitialized] = useState(false);

  // Load from local storage or initialize seed
  useEffect(() => {
    try {
      // Check v2 first, then fallback to v1 for user phrases/custom contacts
      const stored = localStorage.getItem(STORAGE_KEY) || localStorage.getItem('remind_app_state_v1');
      if (stored) {
        const parsed: RemindState = JSON.parse(stored);
        setThemeMode(parsed.themeMode || 'DARK');
        setIsGlobalPauseEnabled(parsed.isGlobalPauseEnabled || false);
        if (parsed.buckets && parsed.buckets.length > 0) {
          parsed.buckets.forEach((b) => registerCustomBucket(b));
          setBuckets(parsed.buckets);
        } else {
          setBuckets(Object.values(DEFAULT_GROUP_BUCKETS));
        }

        // Scrub all sample/example contacts and their logs
        const cleanContacts = (parsed.contacts || []).filter((c) => !isLegacySampleContact(c));
        const cleanContactIds = new Set(cleanContacts.map((c) => c.id));
        const cleanPending = (parsed.pendingLogs || []).filter((p) =>
          cleanContactIds.has(p.contactScheduleId)
        );
        const cleanHistory = (parsed.historyLogs || []).filter(
          (h) => cleanContactIds.has(h.contactScheduleId) && h.contactDisplayName !== 'Marcus Vance'
        );

        setContacts(cleanContacts);
        setPhrases(parsed.phrases || []);
        setPendingLogs(cleanPending);
        setHistoryLogs(cleanHistory);
        setInitialized(true);
        return;
      }
    } catch {
      // Fallback
    }

    // Initialize with 50 seed phrase rotation pool and ZERO sample contacts
    const initialPhrases: Phrase[] = SEED_PHRASES.map((p, index) => ({
      ...p,
      id: index + 1,
      createdAtEpochMillis: Date.now() - (50 - index) * 60000,
    }));

    setPhrases(initialPhrases);
    setContacts([]);
    setPendingLogs([]);
    setHistoryLogs([]);
    setInitialized(true);
  }, []);

  // Save changes to localStorage
  useEffect(() => {
    if (!initialized) return;
    const state: RemindState = {
      themeMode,
      isGlobalPauseEnabled,
      contacts,
      phrases,
      pendingLogs,
      historyLogs,
      buckets,
    };
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
    } catch {
      // Ignore
    }
  }, [
    initialized,
    themeMode,
    isGlobalPauseEnabled,
    contacts,
    phrases,
    pendingLogs,
    historyLogs,
    buckets,
  ]);

  // Sync theme with HTML class
  useEffect(() => {
    const root = document.documentElement;
    if (themeMode === 'DARK') {
      root.classList.add('dark');
      root.classList.remove('light');
    } else {
      root.classList.remove('dark');
      root.classList.add('light');
    }
  }, [themeMode]);

  // ==================== CORE PRE-ROLL ENGINE (N != N-1) ====================

  const preRollNextForContact = useCallback(
    (
      contact: ContactSchedule,
      availablePhrases: Phrase[] = phrases,
      currentPending: MessageLog[] = pendingLogs
    ): { nextLog: MessageLog; updatedPending: MessageLog[] } => {
      const occurrence = computeNextOccurrence(contact);

      const pool = availablePhrases.filter((p) => p.bucket === contact.bucket);
      // N != N-1 rule: exclude last sent phrase id and text
      const eligible = pool.filter(
        (p) =>
          p.id !== contact.lastSentPhraseId &&
          p.text !== contact.lastSentPhraseText
      );
      const chosenPool = eligible.length > 0 ? eligible : pool;
      const chosen =
        chosenPool.length > 0
          ? chosenPool[Math.floor(Math.random() * chosenPool.length)]
          : null;

      const phraseText =
        chosen?.text || 'Thinking of you today and hoping all is well!';

      const newLog: MessageLog = {
        id: Date.now() + Math.floor(Math.random() * 1000),
        contactScheduleId: contact.id,
        contactDisplayName: contact.displayName,
        contactPhoneNumber: contact.phoneNumber,
        bucket: contact.bucket,
        phraseId: chosen?.id,
        messageText: phraseText,
        isCustomEdited: false,
        scheduledDateIso: occurrence.dateIso,
        scheduledEpochMillis: occurrence.epochMillis,
        status: 'PENDING',
      };

      // Remove existing pending row for this contact
      const filtered = currentPending.filter(
        (p) => p.contactScheduleId !== contact.id
      );
      return {
        nextLog: newLog,
        updatedPending: [...filtered, newLog],
      };
    },
    [phrases, pendingLogs]
  );

  // ==================== ACTION SHEET DISPATCH & HANDLERS ====================

  const sendNow = useCallback(
    (log: MessageLog) => {
      const contact = contacts.find((c) => c.id === log.contactScheduleId);
      if (!contact) return;

      const now = new Date();
      const currentHour = now.getHours();

      // Blackout check: 23:00 to 06:59
      if (currentHour >= 23 || currentHour < 7) {
        alert(
          'Blackout Window Active: Automated messages are held between 11:00 PM and 7:00 AM. Queued for 7:00 AM.'
        );
        return;
      }

      // Check collision today
      const startOfToday = new Date();
      startOfToday.setHours(0, 0, 0, 0);
      const hasMessagedToday =
        contact.lastManualInteractionEpochMillis >= startOfToday.getTime();

      let executedLog: MessageLog;

      if (hasMessagedToday) {
        executedLog = {
          ...log,
          executedEpochMillis: Date.now(),
          status: 'SKIPPED_COLLISION',
          statusNote: `Skipped: You already messaged ${contact.displayName} earlier today`,
        };
      } else {
        executedLog = {
          ...log,
          executedEpochMillis: Date.now(),
          status: 'SENT',
          statusNote: 'Delivered via default cellular SMS subscription',
        };
      }

      // Update history
      setHistoryLogs((prev) => [executedLog, ...prev]);

      // Update contact's last sent phrase & last dispatched date
      const updatedContact: ContactSchedule = {
        ...contact,
        lastSentPhraseId: log.phraseId,
        lastSentPhraseText: log.messageText,
        lastDispatchedDateIso: now.toISOString().split('T')[0],
      };

      setContacts((prev) =>
        prev.map((c) => (c.id === contact.id ? updatedContact : c))
      );

      // Immediately pre-roll next occurrence
      const { updatedPending } = preRollNextForContact(
        updatedContact,
        phrases,
        pendingLogs
      );
      setPendingLogs(updatedPending);
    },
    [contacts, phrases, pendingLogs, preRollNextForContact]
  );

  const rerollPhrase = useCallback(
    (log: MessageLog) => {
      const contact = contacts.find((c) => c.id === log.contactScheduleId);
      const pool = phrases.filter((p) => p.bucket === log.bucket);
      if (pool.length === 0) return;

      // Exclude current phrase and last sent phrase
      const eligible = pool.filter(
        (p) => p.id !== log.phraseId && p.id !== contact?.lastSentPhraseId
      );
      const chosenPool = eligible.length > 0 ? eligible : pool;
      const chosen = chosenPool[Math.floor(Math.random() * chosenPool.length)];

      setPendingLogs((prev) =>
        prev.map((p) =>
          p.id === log.id
            ? {
                ...p,
                phraseId: chosen.id,
                messageText: chosen.text,
                isCustomEdited: false,
              }
            : p
        )
      );
    },
    [contacts, phrases]
  );

  const editOneOffText = useCallback(
    (log: MessageLog, newText: string): boolean => {
      const validation = validateLoveGuardrail(log.bucket, newText);
      if (!validation.isValid) {
        return false;
      }

      setPendingLogs((prev) =>
        prev.map((p) =>
          p.id === log.id
            ? {
                ...p,
                messageText: newText.trim(),
                isCustomEdited: true,
              }
            : p
        )
      );
      return true;
    },
    []
  );

  const skipOccurrence = useCallback(
    (log: MessageLog) => {
      const contact = contacts.find((c) => c.id === log.contactScheduleId);
      if (!contact) return;

      const skippedLog: MessageLog = {
        ...log,
        executedEpochMillis: Date.now(),
        status: 'SKIPPED_MANUAL',
        statusNote: 'Skipped: Manually skipped by user',
      };

      setHistoryLogs((prev) => [skippedLog, ...prev]);

      // Immediately pre-roll next occurrence
      const { updatedPending } = preRollNextForContact(
        contact,
        phrases,
        pendingLogs
      );
      setPendingLogs(updatedPending);
    },
    [contacts, phrases, pendingLogs, preRollNextForContact]
  );

  const rescheduleOccurrence = useCallback(
    (log: MessageLog, newEpochMillis: number, newDateIso: string) => {
      setPendingLogs((prev) =>
        prev.map((p) =>
          p.id === log.id
            ? {
                ...p,
                scheduledEpochMillis: newEpochMillis,
                scheduledDateIso: newDateIso,
                statusNote: 'Rescheduled manually',
              }
            : p
        )
      );
    },
    []
  );

  const toggleContactActive = useCallback(
    (contactId: number, isActive: boolean) => {
      const contact = contacts.find((c) => c.id === contactId);
      if (!contact) return;

      const updated = { ...contact, isActive };
      setContacts((prev) => prev.map((c) => (c.id === contactId ? updated : c)));

      if (isActive) {
        const { updatedPending } = preRollNextForContact(
          updated,
          phrases,
          pendingLogs
        );
        setPendingLogs(updatedPending);
      } else {
        setPendingLogs((prev) =>
          prev.filter((p) => p.contactScheduleId !== contactId)
        );
      }
    },
    [contacts, phrases, pendingLogs, preRollNextForContact]
  );

  const saveContact = useCallback(
    (scheduleData: ContactSchedule) => {
      let savedContact: ContactSchedule;
      if (scheduleData.id === 0) {
        savedContact = {
          ...scheduleData,
          id: Date.now(),
          createdAtEpochMillis: Date.now(),
          updatedAtEpochMillis: Date.now(),
        };
        setContacts((prev) => [...prev, savedContact]);
      } else {
        savedContact = {
          ...scheduleData,
          updatedAtEpochMillis: Date.now(),
        };
        setContacts((prev) =>
          prev.map((c) => (c.id === savedContact.id ? savedContact : c))
        );
      }

      // Pre-roll next occurrence for this saved contact
      if (savedContact.isActive) {
        const { updatedPending } = preRollNextForContact(
          savedContact,
          phrases,
          pendingLogs
        );
        setPendingLogs(updatedPending);
      }
    },
    [phrases, pendingLogs, preRollNextForContact]
  );

  const deleteContact = useCallback((contactId: number) => {
    setContacts((prev) => prev.filter((c) => c.id !== contactId));
    setPendingLogs((prev) =>
      prev.filter((p) => p.contactScheduleId !== contactId)
    );
  }, []);

  // ==================== PHRASE CRUD & CASCADE UPDATES ====================

  const savePhrase = useCallback(
    (phraseData: Omit<Phrase, 'id' | 'createdAtEpochMillis'> & { id?: number }): boolean => {
      const validation = validateLoveGuardrail(phraseData.bucket, phraseData.text);
      if (!validation.isValid) {
        return false;
      }

      if (phraseData.id && phraseData.id > 0) {
        // Edit existing phrase
        const updatedPhrase: Phrase = {
          id: phraseData.id,
          bucket: phraseData.bucket,
          text: phraseData.text.trim(),
          isSeed: phraseData.isSeed,
          createdAtEpochMillis: Date.now(),
        };

        setPhrases((prev) =>
          prev.map((p) => (p.id === updatedPhrase.id ? updatedPhrase : p))
        );

        // Section 7.3: Cascade update to unedited PENDING rows referencing this phrase
        setPendingLogs((prev) =>
          prev.map((p) =>
            p.phraseId === updatedPhrase.id && !p.isCustomEdited
              ? { ...p, messageText: updatedPhrase.text }
              : p
          )
        );
      } else {
        // Create new phrase
        const newPhrase: Phrase = {
          id: Date.now(),
          bucket: phraseData.bucket,
          text: phraseData.text.trim(),
          isSeed: false,
          createdAtEpochMillis: Date.now(),
        };
        setPhrases((prev) => [...prev, newPhrase]);
      }
      return true;
    },
    []
  );

  const deletePhrase = useCallback(
    (phraseId: number) => {
      // Find affected pending messages
      const affectedLogs = pendingLogs.filter((p) => p.phraseId === phraseId);

      setPhrases((prev) => prev.filter((p) => p.id !== phraseId));

      // Section 7.3: Cascade reroll affected pending messages
      affectedLogs.forEach((log) => {
        rerollPhrase(log);
      });
    },
    [pendingLogs, rerollPhrase]
  );

  // ==================== HARDWARE / COLLISION SIMULATOR ====================

  const simulateCollisionForContact = useCallback(
    (contactId: number, messagedToday: boolean) => {
      setContacts((prev) =>
        prev.map((c) =>
          c.id === contactId
            ? {
                ...c,
                lastManualInteractionEpochMillis: messagedToday ? Date.now() : 0,
              }
            : c
        )
      );
    },
    []
  );

  // ==================== BUCKET / RELATIONSHIP GROUP MANAGEMENT ====================

  const addBucket = useCallback(
    (newBucket: {
      displayName: string;
      allowsLoveWord: boolean;
      darkColorHex: string;
      lightColorHex: string;
      initialPhrase?: string;
    }): BucketMetadata => {
      const slug = newBucket.displayName.trim().toUpperCase().replace(/[^A-Z0-9]/g, '_');
      const uniqueId = `CUSTOM_${slug}_${Date.now()}`;
      const bucketMeta: BucketMetadata = {
        id: uniqueId,
        displayName: newBucket.displayName.trim(),
        allowsLoveWord: newBucket.allowsLoveWord,
        darkColorHex: newBucket.darkColorHex,
        lightColorHex: newBucket.lightColorHex,
        isCustom: true,
      };
      registerCustomBucket(bucketMeta);
      setBuckets((prev) => [...prev, bucketMeta]);

      // If initial phrase was provided, add it to phrases pool
      if (newBucket.initialPhrase && newBucket.initialPhrase.trim()) {
        const newPhrase: Phrase = {
          id: Date.now(),
          bucket: uniqueId,
          text: newBucket.initialPhrase.trim(),
          isSeed: false,
          createdAtEpochMillis: Date.now(),
        };
        setPhrases((prev) => [...prev, newPhrase]);
      }

      return bucketMeta;
    },
    []
  );

  const deleteBucket = useCallback((bucketId: string) => {
    if (DEFAULT_GROUP_BUCKETS[bucketId]) return false;
    setBuckets((prev) => prev.filter((b) => b.id !== bucketId));
    delete GROUP_BUCKETS[bucketId];
    return true;
  }, []);

  const resetToFactorySeed = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY);
    window.location.reload();
  }, []);

  return {
    themeMode,
    setThemeMode,
    isGlobalPauseEnabled,
    setIsGlobalPauseEnabled,
    buckets,
    addBucket,
    deleteBucket,
    contacts,
    phrases,
    pendingLogs,
    historyLogs,
    sendNow,
    rerollPhrase,
    editOneOffText,
    skipOccurrence,
    rescheduleOccurrence,
    toggleContactActive,
    saveContact,
    deleteContact,
    savePhrase,
    deletePhrase,
    simulateCollisionForContact,
    resetToFactorySeed,
  };
}
