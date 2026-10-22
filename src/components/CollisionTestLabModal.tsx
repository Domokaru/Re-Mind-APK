import React from 'react';
import { ContactSchedule } from '../types/remind';
import { BucketBadge } from './BucketBadge';
import { X, MessageSquare, Zap } from 'lucide-react';

interface CollisionTestLabModalProps {
  contacts: ContactSchedule[];
  onClose: () => void;
  onToggleCollision: (contactId: number, messagedToday: boolean) => void;
}

export const CollisionTestLabModal: React.FC<CollisionTestLabModalProps> = ({
  contacts,
  onClose,
  onToggleCollision,
}) => {
  const startOfToday = new Date();
  startOfToday.setHours(0, 0, 0, 0);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
      <div className="w-full max-w-xl bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-2xl max-h-[85vh] overflow-y-auto">
        <div className="flex items-center justify-between pb-4 border-b border-slate-100 dark:border-slate-800">
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-amber-500/10 text-amber-600 dark:text-amber-400">
              <Zap className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-slate-900 dark:text-white">
                Hardware & Collision Test Lab
              </h2>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Simulate phone activity to verify Re:MIND safety guardrails
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-full text-slate-400 hover:text-slate-600 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="space-y-4 pt-4">
          <div className="p-3 bg-slate-50 dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 text-xs text-slate-600 dark:text-slate-300 space-y-1.5">
            <div className="font-semibold text-sky-600 dark:text-sky-400 flex items-center gap-1.5">
              <MessageSquare className="w-4 h-4" />
              <span>Dual-Layer Collision Avoidance Engine</span>
            </div>
            <p className="text-slate-500 dark:text-slate-400 leading-relaxed">
              Toggle "Messaged Today" on any contact below to simulate an outgoing SMS or active Google Messages RCS conversation today. When automated send triggers, Re:MIND will detect the collision, abort the text, log it as <strong>SKIPPED_COLLISION</strong>, and pre-roll their next recurrence without double-texting!
            </p>
          </div>

          <div className="space-y-2">
            <h3 className="text-xs font-bold text-slate-700 dark:text-slate-300 uppercase tracking-wider">
              Tracked Contacts ({contacts.length})
            </h3>

            {contacts.map((c) => {
              const hasMessagedToday =
                c.lastManualInteractionEpochMillis >= startOfToday.getTime();

              return (
                <div
                  key={c.id}
                  className="flex items-center justify-between p-3 bg-slate-50 dark:bg-slate-900/80 rounded-xl border border-slate-200 dark:border-slate-800 hover:border-slate-300 dark:hover:border-slate-700 transition-colors"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-full bg-slate-200 dark:bg-slate-800 flex items-center justify-center text-xs font-bold text-slate-800 dark:text-white">
                      {c.displayName[0]}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-semibold text-slate-900 dark:text-white">
                          {c.displayName}
                        </span>
                        <BucketBadge bucket={c.bucket} />
                      </div>
                      <span className="text-xs text-slate-500 dark:text-slate-400">
                        {c.phoneNumber}
                      </span>
                    </div>
                  </div>

                  <button
                    onClick={() => onToggleCollision(c.id, !hasMessagedToday)}
                    className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
                      hasMessagedToday
                        ? 'bg-amber-500 text-slate-950 shadow-md shadow-amber-500/20'
                        : 'bg-slate-200 dark:bg-slate-800 text-slate-700 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
                    }`}
                  >
                    {hasMessagedToday ? 'Active Today (Collision)' : 'No Activity'}
                  </button>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
