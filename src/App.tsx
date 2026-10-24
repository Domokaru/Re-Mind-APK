/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect } from 'react';
import { useRemindStore } from './state/remindStore';
import { DashboardView } from './screens/DashboardView';
import { CalendarView } from './screens/CalendarView';
import { PeopleView } from './screens/PeopleView';
import { PhrasesView } from './screens/PhrasesView';
import { SettingsView } from './screens/SettingsView';
import { UpcomingActionSheet } from './components/UpcomingActionSheet';
import { ContactEditModal } from './components/ContactEditModal';
import { PhraseModal } from './components/PhraseModal';
import { CollisionTestLabModal } from './components/CollisionTestLabModal';
import { AndroidCodeModal } from './components/AndroidCodeModal';
import { CreateBucketModal } from './components/CreateBucketModal';
import {
  ContactSchedule,
  GroupBucket,
  MessageLog,
  Phrase,
} from './types/remind';
import {
  Calendar,
  Users,
  Settings,
  AlertTriangle,
} from 'lucide-react';

// Custom Dashboard Analytics Board Icon inspired by the requested board design
// Featuring screen board frame, line graph with nodes, pie chart, bar chart, and stat lines
function DashboardAnalyticsIcon({ className = 'w-5 h-5' }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.75"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
    >
      {/* Outer monitor / board display frame */}
      <rect x="2" y="3" width="20" height="15" rx="2" />
      {/* Stand base */}
      <path d="M9 21h6" />
      <path d="M12 18v3" />
      
      {/* Top line graph with 4 node points */}
      <polyline points="4.5,9.5 7.5,7 10.5,8.5 13.5,6" />
      <circle cx="4.5" cy="9.5" r="0.75" fill="currentColor" stroke="none" />
      <circle cx="7.5" cy="7" r="0.75" fill="currentColor" stroke="none" />
      <circle cx="10.5" cy="8.5" r="0.75" fill="currentColor" stroke="none" />
      <circle cx="13.5" cy="6" r="0.75" fill="currentColor" stroke="none" />
      
      {/* Top right stat list rows */}
      <line x1="16" y1="6.5" x2="19.5" y2="6.5" />
      <line x1="16" y1="9" x2="18.5" y2="9" />

      {/* Bottom left pie chart circle with radial slice */}
      <circle cx="7" cy="14" r="2.2" />
      <path d="M7 11.8v2.2l1.5 1.2" />

      {/* Bottom right bar chart columns */}
      <line x1="13" y1="15.5" x2="13" y2="13" />
      <line x1="15.5" y1="15.5" x2="15.5" y2="11.5" />
      <line x1="18" y1="15.5" x2="18" y2="14" />
    </svg>
  );
}

type BottomTab = 'DASHBOARD' | 'CALENDAR' | 'PEOPLE' | 'PHRASES' | 'SETTINGS';

export default function App() {
  const store = useRemindStore();
  const [activeTab, setActiveTab] = useState<BottomTab>('DASHBOARD');

  // Modal states
  const [selectedUpcomingLog, setSelectedUpcomingLog] = useState<MessageLog | null>(null);
  const [editingContact, setEditingContact] = useState<ContactSchedule | null>(null);
  const [isAddingContact, setIsAddingContact] = useState(false);
  const [editingPhrase, setEditingPhrase] = useState<Phrase | null>(null);
  const [isAddingPhrase, setIsAddingPhrase] = useState(false);
  const [targetPhraseBucket, setTargetPhraseBucket] = useState<GroupBucket>('SPOUSE');
  const [isTestLabOpen, setIsTestLabOpen] = useState(false);
  const [isCodeModalOpen, setIsCodeModalOpen] = useState(false);
  const [isCreateBucketModalOpen, setIsCreateBucketModalOpen] = useState(false);

  // Sync dark / light class to root document element for Tailwind dark: variants
  useEffect(() => {
    if (store.themeMode === 'DARK') {
      document.documentElement.classList.add('dark');
      document.documentElement.classList.remove('light');
    } else {
      document.documentElement.classList.remove('dark');
      document.documentElement.classList.add('light');
    }
  }, [store.themeMode]);

  return (
    <div
      className={`min-h-screen ${
        store.themeMode === 'DARK'
          ? 'bg-[#0B0F19] text-slate-100 dark'
          : 'bg-[#F8FAFC] text-slate-900'
      } flex flex-col antialiased selection:bg-[#08dac9]/30 font-['Plus_Jakarta_Sans',sans-serif]`}
    >
      {/* Global Vacation Mode Banner */}
      {store.isGlobalPauseEnabled && (
        <div className="bg-amber-500/15 border-b border-amber-500/30 px-4 py-2 flex items-center justify-between text-xs text-amber-600 dark:text-amber-300">
          <div className="flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 text-amber-500 dark:text-amber-400" />
            <span>
              <strong>Vacation Mode Active:</strong> All automated text messages are paused.
            </span>
          </div>
          <button
            onClick={() => store.setIsGlobalPauseEnabled(false)}
            className="px-2.5 py-1 text-[11px] font-bold bg-amber-400 text-slate-950 rounded-lg hover:bg-amber-300 transition-colors shadow-sm"
          >
            Resume
          </button>
        </div>
      )}

      {/* Main Content Area */}
      <main className="flex-1 max-w-xl w-full mx-auto p-4 pt-5 pb-20">
        {activeTab === 'DASHBOARD' && (
          <DashboardView
            upcomingMessages={store.pendingLogs}
            historyMessages={store.historyLogs}
            onSelectUpcoming={(log) => setSelectedUpcomingLog(log)}
            onOpenTestLab={() => setIsTestLabOpen(true)}
          />
        )}

        {activeTab === 'CALENDAR' && (
          <CalendarView
            contacts={store.contacts}
            pendingMessages={store.pendingLogs}
            historyMessages={store.historyLogs}
            onSelectUpcoming={(log) => setSelectedUpcomingLog(log)}
          />
        )}

        {activeTab === 'PEOPLE' && (
          <PeopleView
            contacts={store.contacts}
            pendingMessages={store.pendingLogs}
            buckets={store.buckets}
            onToggleActive={store.toggleContactActive}
            onEditContact={(c) => setEditingContact(c)}
            onDeleteContact={store.deleteContact}
            onAddNewContact={() => setIsAddingContact(true)}
          />
        )}

        {activeTab === 'PHRASES' && (
          <PhrasesView
            phrases={store.phrases}
            buckets={store.buckets}
            onAddNewPhrase={(bucket) => {
              setTargetPhraseBucket(bucket);
              setIsAddingPhrase(true);
            }}
            onEditPhrase={(phrase) => setEditingPhrase(phrase)}
            onDeletePhrase={store.deletePhrase}
            onOpenCreateBucket={() => setIsCreateBucketModalOpen(true)}
            onBackToSettings={() => setActiveTab('SETTINGS')}
          />
        )}

        {activeTab === 'SETTINGS' && (
          <SettingsView
            themeMode={store.themeMode}
            isGlobalPauseEnabled={store.isGlobalPauseEnabled}
            phraseCount={store.phrases.length}
            buckets={store.buckets}
            onToggleTheme={store.setThemeMode}
            onToggleGlobalPause={store.setIsGlobalPauseEnabled}
            onOpenPhrases={() => setActiveTab('PHRASES')}
            onOpenAndroidCode={() => setIsCodeModalOpen(true)}
            onOpenTestLab={() => setIsTestLabOpen(true)}
            onOpenCreateBucket={() => setIsCreateBucketModalOpen(true)}
            onResetFactory={store.resetToFactorySeed}
          />
        )}
      </main>

      {/* Bottom 4 Tabs Navigation Bar (Dashboard, Calendar, People, Settings) */}
      <nav
        className={`fixed bottom-0 left-0 right-0 z-40 border-t backdrop-blur-md px-4 py-1.5 transition-colors ${
          store.themeMode === 'DARK'
            ? 'bg-[#111827]/95 border-slate-800'
            : 'bg-white/95 border-slate-200 shadow-lg'
        }`}
      >
        <div className="max-w-md mx-auto grid grid-cols-4 gap-2">
          <button
            onClick={() => setActiveTab('DASHBOARD')}
            className={`flex items-center justify-center py-2 rounded-xl transition-all ${
              activeTab === 'DASHBOARD'
                ? 'text-[#08dac9] bg-[#08dac9]/15 font-bold shadow-sm'
                : 'text-slate-400 hover:text-slate-700 dark:hover:text-slate-200'
            }`}
            title="Dashboard"
            aria-label="Dashboard"
          >
            <DashboardAnalyticsIcon className="w-5 h-5" />
          </button>

          <button
            onClick={() => setActiveTab('CALENDAR')}
            className={`flex items-center justify-center py-2 rounded-xl transition-all ${
              activeTab === 'CALENDAR'
                ? 'text-[#08dac9] bg-[#08dac9]/15 font-bold shadow-sm'
                : 'text-slate-400 hover:text-slate-700 dark:hover:text-slate-200'
            }`}
            title="Calendar"
            aria-label="Calendar"
          >
            <Calendar className="w-5 h-5" />
          </button>

          <button
            onClick={() => setActiveTab('PEOPLE')}
            className={`flex items-center justify-center py-2 rounded-xl transition-all ${
              activeTab === 'PEOPLE'
                ? 'text-[#08dac9] bg-[#08dac9]/15 font-bold shadow-sm'
                : 'text-slate-400 hover:text-slate-700 dark:hover:text-slate-200'
            }`}
            title="People"
            aria-label="People"
          >
            <Users className="w-5 h-5" />
          </button>

          <button
            onClick={() => setActiveTab('SETTINGS')}
            className={`flex items-center justify-center py-2 rounded-xl transition-all ${
              activeTab === 'SETTINGS' || activeTab === 'PHRASES'
                ? 'text-[#08dac9] bg-[#08dac9]/15 font-bold shadow-sm'
                : 'text-slate-400 hover:text-slate-700 dark:hover:text-slate-200'
            }`}
            title="Settings"
            aria-label="Settings"
          >
            <Settings className="w-5 h-5" />
          </button>
        </div>
      </nav>

      {/* Modal 1: Upcoming Message Action Sheet (6 Actions) */}
      {selectedUpcomingLog && (
        <UpcomingActionSheet
          log={selectedUpcomingLog}
          onClose={() => setSelectedUpcomingLog(null)}
          onSendNow={store.sendNow}
          onRerollPhrase={store.rerollPhrase}
          onEditOneOffText={store.editOneOffText}
          onSkipOccurrence={store.skipOccurrence}
          onRescheduleOccurrence={store.rescheduleOccurrence}
          onPauseContact={(contactId) => store.toggleContactActive(contactId, false)}
        />
      )}

      {/* Modal 2: Contact Edit / Add Sheet */}
      {(isAddingContact || editingContact) && (
        <ContactEditModal
          schedule={editingContact}
          buckets={store.buckets}
          onClose={() => {
            setIsAddingContact(false);
            setEditingContact(null);
          }}
          onSave={store.saveContact}
          onOpenCreateBucket={() => setIsCreateBucketModalOpen(true)}
        />
      )}

      {/* Modal 3: Phrase Edit / Add Modal */}
      {(isAddingPhrase || editingPhrase) && (
        <PhraseModal
          phrase={editingPhrase}
          defaultBucket={targetPhraseBucket}
          buckets={store.buckets}
          onClose={() => {
            setIsAddingPhrase(false);
            setEditingPhrase(null);
          }}
          onSave={store.savePhrase}
        />
      )}

      {/* Modal 4: Collision & Hardware Test Lab */}
      {isTestLabOpen && (
        <CollisionTestLabModal
          contacts={store.contacts}
          onClose={() => setIsTestLabOpen(false)}
          onToggleCollision={store.simulateCollisionForContact}
        />
      )}

      {/* Modal 5: Native Android Code Inspector */}
      {isCodeModalOpen && (
        <AndroidCodeModal onClose={() => setIsCodeModalOpen(false)} />
      )}

      {/* Modal 6: Create Relationship Group Bucket Modal */}
      {isCreateBucketModalOpen && (
        <CreateBucketModal
          onClose={() => setIsCreateBucketModalOpen(false)}
          onSave={(bucketData) => {
            const created = store.addBucket(bucketData);
            setTargetPhraseBucket(created.id);
          }}
        />
      )}
    </div>
  );
}
