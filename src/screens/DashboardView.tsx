import React, { useState } from 'react';
import { MessageLog } from '../types/remind';
import { BucketBadge } from '../components/BucketBadge';
import {
  Clock,
  ChevronRight,
  Info,
} from 'lucide-react';

interface DashboardViewProps {
  upcomingMessages: MessageLog[];
  historyMessages: MessageLog[];
  onSelectUpcoming: (log: MessageLog) => void;
  onOpenTestLab?: () => void;
}

export const DashboardView: React.FC<DashboardViewProps> = ({
  upcomingMessages,
  historyMessages,
  onSelectUpcoming,
}) => {
  const [activeTab, setActiveTab] = useState<'UPCOMING' | 'HISTORY'>('UPCOMING');
  const [showAllUpcoming, setShowAllUpcoming] = useState(false);
  const [showAllHistory, setShowAllHistory] = useState(false);

  const displayedUpcoming = showAllUpcoming
    ? upcomingMessages
    : upcomingMessages.slice(0, 10);

  const displayedHistory = showAllHistory
    ? historyMessages
    : historyMessages.slice(0, 10);

  const formatScheduledTime = (epochMillis: number): string => {
    const d = new Date(epochMillis);
    return d.toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
    });
  };

  return (
    <div className="space-y-4">
      {/* Segmented Control: Upcoming vs History */}
      <div className="flex bg-slate-100 dark:bg-[#111827] p-1 rounded-xl border border-slate-200 dark:border-slate-800">
        <button
          onClick={() => setActiveTab('UPCOMING')}
          className={`flex-1 py-2 text-xs font-bold rounded-lg transition-all ${
            activeTab === 'UPCOMING'
              ? 'bg-white dark:bg-slate-800 text-sky-600 dark:text-sky-400 shadow-sm'
              : 'text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
          }`}
        >
          Upcoming Locked-In ({upcomingMessages.length})
        </button>
        <button
          onClick={() => setActiveTab('HISTORY')}
          className={`flex-1 py-2 text-xs font-bold rounded-lg transition-all ${
            activeTab === 'HISTORY'
              ? 'bg-white dark:bg-slate-800 text-sky-600 dark:text-sky-400 shadow-sm'
              : 'text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
          }`}
        >
          Recent Activity Log ({historyMessages.length})
        </button>
      </div>

      {/* Tab 1: Upcoming Messages Feed */}
      {activeTab === 'UPCOMING' && (
        <div className="space-y-3">
          {displayedUpcoming.length === 0 ? (
            <div className="p-8 text-center bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl shadow-sm">
              <Clock className="w-8 h-8 text-slate-400 dark:text-slate-600 mx-auto mb-2" />
              <p className="text-sm font-semibold text-slate-800 dark:text-white">No Upcoming Messages</p>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
                Add contacts in the People tab to automate your recurring check-ins.
              </p>
            </div>
          ) : (
            displayedUpcoming.map((log) => (
              <div
                key={log.id}
                onClick={() => onSelectUpcoming(log)}
                className="p-4 bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 hover:border-slate-300 dark:hover:border-slate-700 rounded-2xl transition-all cursor-pointer shadow-sm group hover:scale-[1.005]"
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-slate-100 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 flex items-center justify-center font-bold text-slate-800 dark:text-white text-sm">
                      {log.contactDisplayName[0]}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <h4 className="text-sm font-bold text-slate-900 dark:text-white group-hover:text-sky-600 dark:group-hover:text-sky-400 transition-colors">
                          {log.contactDisplayName}
                        </h4>
                        <BucketBadge bucket={log.bucket} />
                      </div>
                      <div className="text-xs text-sky-600 dark:text-sky-400 font-medium flex items-center gap-1 mt-0.5">
                        <Clock className="w-3.5 h-3.5" />
                        <span>{formatScheduledTime(log.scheduledEpochMillis)}</span>
                      </div>
                    </div>
                  </div>

                  <span className="text-[11px] font-medium text-slate-400 group-hover:text-slate-600 dark:group-hover:text-slate-200 flex items-center gap-1">
                    <span>Actions</span>
                    <ChevronRight className="w-3.5 h-3.5" />
                  </span>
                </div>

                {/* Pre-rolled locked phrase in quotes */}
                <div className="mt-3 p-3 bg-slate-50 dark:bg-slate-900/90 rounded-xl border border-slate-200 dark:border-slate-800/80">
                  <p className="text-xs text-slate-800 dark:text-slate-200 italic leading-relaxed">
                    "{log.messageText}"
                  </p>
                  {log.isCustomEdited && (
                    <span className="inline-block mt-1.5 text-[10px] font-semibold text-purple-600 dark:text-purple-400">
                      Edited one-off for this occurrence
                    </span>
                  )}
                </div>
              </div>
            ))
          )}

          {upcomingMessages.length > 10 && (
            <button
              onClick={() => setShowAllUpcoming(!showAllUpcoming)}
              className="w-full py-2.5 text-xs font-semibold text-sky-600 dark:text-sky-400 bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors shadow-sm"
            >
              {showAllUpcoming ? 'Show Less' : `Show All Upcoming (${upcomingMessages.length})`}
            </button>
          )}
        </div>
      )}

      {/* Tab 2: Executed History Feed */}
      {activeTab === 'HISTORY' && (
        <div className="space-y-3">
          {displayedHistory.length === 0 ? (
            <div className="p-8 text-center bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl shadow-sm">
              <Info className="w-8 h-8 text-slate-400 dark:text-slate-600 mx-auto mb-2" />
              <p className="text-sm font-semibold text-slate-800 dark:text-white">No Executed History Yet</p>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
                Dispatched texts or collision skips will be logged here.
              </p>
            </div>
          ) : (
            displayedHistory.map((log) => {
              const isSent = log.status === 'SENT';
              const isCollision = log.status === 'SKIPPED_COLLISION';
              const isManual = log.status === 'SKIPPED_MANUAL';
              const isFailed = log.status === 'FAILED';

              return (
                <div
                  key={log.id}
                  className="p-4 bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl space-y-2 shadow-sm"
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-bold text-slate-900 dark:text-white">
                        {log.contactDisplayName}
                      </span>
                      <BucketBadge bucket={log.bucket} />
                    </div>

                    {/* Status Pill */}
                    {isSent && (
                      <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20">
                        SENT
                      </span>
                    )}
                    {isCollision && (
                      <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-500/10 text-amber-600 dark:text-amber-400 border border-amber-500/20">
                        SKIPPED: RECENT TEXT
                      </span>
                    )}
                    {isManual && (
                      <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400">
                        SKIPPED: MANUAL
                      </span>
                    )}
                    {isFailed && (
                      <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-rose-500/10 text-rose-600 dark:text-rose-400 border border-rose-500/20">
                        FAILED
                      </span>
                    )}
                  </div>

                  <p className="text-xs text-slate-700 dark:text-slate-300 italic">
                    "{log.messageText}"
                  </p>

                  <div className="flex items-center justify-between text-[11px] text-slate-400 dark:text-slate-500 pt-1 border-t border-slate-100 dark:border-slate-800/60">
                    <span>{formatScheduledTime(log.executedEpochMillis || log.scheduledEpochMillis)}</span>
                    {log.statusNote && (
                      <span className="text-slate-500 dark:text-slate-400 font-medium">{log.statusNote}</span>
                    )}
                  </div>
                </div>
              );
            })
          )}

          {historyMessages.length > 10 && (
            <button
              onClick={() => setShowAllHistory(!showAllHistory)}
              className="w-full py-2.5 text-xs font-semibold text-sky-600 dark:text-sky-400 bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors shadow-sm"
            >
              {showAllHistory ? 'Show Less' : `Show All History (${historyMessages.length})`}
            </button>
          )}
        </div>
      )}
    </div>
  );
};
