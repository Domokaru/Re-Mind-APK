import React, { useState } from 'react';
import { ContactSchedule, MessageLog, BucketMetadata, GROUP_BUCKETS } from '../types/remind';
import { BucketBadge } from '../components/BucketBadge';
import { formatTimeSummary } from '../core/recurrence';
import {
  UserPlus,
  Edit2,
  Trash2,
  AlertTriangle,
  Clock,
  ChevronDown,
  ChevronUp,
  Filter,
  X,
} from 'lucide-react';

interface PeopleViewProps {
  contacts: ContactSchedule[];
  pendingMessages: MessageLog[];
  buckets?: BucketMetadata[];
  onToggleActive: (contactId: number, isActive: boolean) => void;
  onEditContact: (contact: ContactSchedule) => void;
  onDeleteContact: (contactId: number) => void;
  onAddNewContact: () => void;
}

export const PeopleView: React.FC<PeopleViewProps> = ({
  contacts,
  pendingMessages,
  buckets,
  onToggleActive,
  onEditContact,
  onDeleteContact,
  onAddNewContact,
}) => {
  const [filter, setFilter] = useState<string>('ALL');
  const [expandedContactIds, setExpandedContactIds] = useState<Set<number>>(new Set());
  const [contactToDelete, setContactToDelete] = useState<ContactSchedule | null>(null);

  const toggleExpand = (id: number) => {
    setExpandedContactIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  const activeBuckets = buckets && buckets.length > 0 ? buckets : (Object.values(GROUP_BUCKETS) as BucketMetadata[]);

  const filterOptions = [
    { id: 'ALL', label: 'All Relationship Groups' },
    ...activeBuckets.map((b) => ({ id: b.id, label: b.displayName })),
    { id: 'PAUSED', label: 'Paused Schedules' },
  ];

  const filteredContacts = contacts.filter((c) => {
    if (filter === 'ALL') return true;
    if (filter === 'PAUSED') return !c.isActive;
    return c.bucket === filter;
  });

  const getRecurrenceLabel = (c: ContactSchedule): string => {
    let cadence = '';
    switch (c.recurrenceType) {
      case 'DAILY':
        cadence = 'Every Day';
        break;
      case 'EVERY_N_DAYS':
        cadence = `Every ${c.intervalN} Days`;
        break;
      case 'WEEKLY':
        cadence = 'Weekly';
        break;
      case 'EVERY_N_WEEKS':
        cadence = `Every ${c.intervalN} Weeks`;
        break;
      case 'MONTHLY_DAY':
        cadence = `Monthly on day ${c.dayOfMonth}`;
        break;
      case 'YEARLY_DATE':
        cadence = 'Yearly';
        break;
    }
    const timeStr = formatTimeSummary(c, c.targetHour, c.targetMinute);
    return `${cadence} · ${timeStr}`;
  };

  return (
    <div className="space-y-4">
      {/* Top Controls: Full-Width Filter Pill */}
      <div className="w-full">
        <div className="relative w-full flex items-center bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 hover:border-slate-300 dark:hover:border-slate-700 rounded-full px-4 py-2.5 shadow-sm transition-colors">
          <Filter className="w-4 h-4 text-[#08dac9] shrink-0 mr-2.5" />
          <select
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            className="w-full bg-transparent text-xs font-bold text-slate-800 dark:text-white focus:outline-none cursor-pointer pr-6 appearance-none"
          >
            {filterOptions.map((opt) => {
              const count =
                opt.id === 'ALL'
                  ? contacts.length
                  : opt.id === 'PAUSED'
                    ? contacts.filter((c) => !c.isActive).length
                    : contacts.filter((c) => c.bucket === opt.id).length;
              return (
                <option
                  key={opt.id}
                  value={opt.id}
                  className="bg-white dark:bg-[#111827] text-slate-900 dark:text-white py-1"
                >
                  {opt.label} ({count})
                </option>
              );
            })}
          </select>
          <ChevronDown className="w-4 h-4 text-slate-400 absolute right-4 pointer-events-none" />
        </div>
      </div>

      {/* List of People Cards (Collapsed by default to name, expands on click) */}
      <div className="space-y-2.5">
        {filteredContacts.length === 0 ? (
          <div className="p-8 text-center bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl shadow-sm">
            <p className="text-sm font-semibold text-slate-800 dark:text-white">
              No Contacts in This Filter
            </p>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
              Tap the button below to add someone new to your mindfulness rotation.
            </p>
          </div>
        ) : (
          filteredContacts.map((contact) => {
            const isExpanded = expandedContactIds.has(contact.id);
            const pending = pendingMessages.find(
              (p) => p.contactScheduleId === contact.id
            );

            return (
              <div
                key={contact.id}
                className="bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl shadow-sm transition-all overflow-hidden hover:border-slate-300 dark:hover:border-slate-700"
              >
                {/* Collapsed Header Row (Always visible, click toggles expand) */}
                <button
                  type="button"
                  onClick={() => toggleExpand(contact.id)}
                  className="w-full p-4 flex items-center justify-between text-left transition-colors hover:bg-slate-50 dark:hover:bg-slate-800/40"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-full bg-slate-100 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 flex items-center justify-center font-bold text-slate-800 dark:text-white text-xs shrink-0">
                      {contact.displayName[0]}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-bold text-slate-900 dark:text-white">
                          {contact.displayName}
                        </span>
                        <BucketBadge bucket={contact.bucket} />
                      </div>
                      {!isExpanded && (
                        <div className="text-[11px] text-slate-500 dark:text-slate-400 mt-0.5">
                          {getRecurrenceLabel(contact)}
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="flex items-center gap-2 text-slate-400">
                    <span
                      className={`text-xs font-semibold ${
                        contact.isActive
                          ? 'text-emerald-600 dark:text-emerald-400'
                          : 'text-slate-400 dark:text-slate-500'
                      }`}
                    >
                      {contact.isActive ? 'Active' : 'Paused'}
                    </span>
                    {isExpanded ? (
                      <ChevronUp className="w-4 h-4 text-[#08dac9]" />
                    ) : (
                      <ChevronDown className="w-4 h-4 text-slate-400" />
                    )}
                  </div>
                </button>

                {/* Expanded Details Body */}
                {isExpanded && (
                  <div className="px-4 pb-4 pt-1 space-y-3 border-t border-slate-100 dark:border-slate-800/80 animate-fade-in">
                    <div className="flex items-center justify-between text-xs text-slate-600 dark:text-slate-300">
                      <span>
                        Phone: <strong className="text-slate-900 dark:text-white">{contact.phoneNumber}</strong>
                      </span>
                      <div className="flex items-center gap-2">
                        <span className="text-xs text-slate-500 dark:text-slate-400">Schedule</span>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            onToggleActive(contact.id, !contact.isActive);
                          }}
                          className={`w-10 h-5 rounded-full transition-colors relative p-0.5 ${
                            contact.isActive
                              ? 'bg-[#08dac9]'
                              : 'bg-slate-200 dark:bg-slate-800 border border-slate-300 dark:border-slate-700'
                          }`}
                        >
                          <span
                            className={`w-4 h-4 rounded-full bg-white block transition-transform shadow ${
                              contact.isActive ? 'translate-x-5' : 'translate-x-0'
                            }`}
                          />
                        </button>
                      </div>
                    </div>

                    <div className="text-xs font-medium text-[#08dac9] flex items-center gap-1.5">
                      <Clock className="w-3.5 h-3.5 shrink-0" />
                      <span>{getRecurrenceLabel(contact)}</span>
                    </div>

                    {/* Next scheduled pre-rolled phrase preview */}
                    {pending && contact.isActive && (
                      <div className="p-3 bg-slate-50 dark:bg-slate-900/90 rounded-xl border border-slate-200 dark:border-slate-800 text-xs space-y-1">
                        <span className="text-[10px] uppercase font-bold text-slate-500 dark:text-slate-400 block">
                          Next Locked-in Message ({pending.scheduledDateIso}):
                        </span>
                        <p className="text-slate-800 dark:text-slate-200 italic leading-relaxed">
                          "{pending.messageText}"
                        </p>
                      </div>
                    )}

                    {/* Verification alert banner if paused */}
                    {contact.pausedReason && (
                      <div className="p-2.5 bg-amber-500/10 border border-amber-500/20 rounded-xl flex items-center gap-2 text-xs text-amber-700 dark:text-amber-300">
                        <AlertTriangle className="w-4 h-4 shrink-0 text-amber-500 dark:text-amber-400" />
                        <span>{contact.pausedReason}</span>
                      </div>
                    )}

                    {/* Actions Footer */}
                    <div className="flex items-center justify-end gap-2 pt-2 border-t border-slate-100 dark:border-slate-800/60">
                      <button
                        onClick={() => onEditContact(contact)}
                        className="px-3 py-1.5 text-xs text-slate-700 dark:text-slate-300 hover:text-slate-900 dark:hover:text-white bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 rounded-lg flex items-center gap-1.5 transition-colors font-semibold"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                        <span>Edit Schedule</span>
                      </button>
                      <button
                        onClick={() => setContactToDelete(contact)}
                        className="px-3 py-1.5 text-xs text-rose-600 dark:text-rose-400 hover:text-rose-700 dark:hover:text-rose-300 bg-rose-50 dark:bg-rose-500/10 hover:bg-rose-100 dark:hover:bg-rose-500/20 rounded-lg flex items-center gap-1.5 transition-colors font-semibold"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                        <span>Delete</span>
                      </button>
                    </div>
                  </div>
                )}
              </div>
            );
          })
        )}
      </div>

      {/* Add Contact Button */}
      <button
        onClick={onAddNewContact}
        className="w-full py-3 bg-[#08dac9] hover:bg-[#07c2b3] text-[#04201d] font-bold text-sm rounded-2xl flex items-center justify-center gap-2 shadow-lg shadow-[#08dac9]/20 transition-all active:scale-[0.99]"
      >
        <UserPlus className="w-4 h-4" />
        <span>Add Connection Schedule</span>
      </button>

      {/* Delete Contact Confirmation Pop-Up Modal */}
      {contactToDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
          <div className="w-full max-w-sm bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-2xl space-y-4">
            <div className="flex items-start justify-between">
              <div className="flex items-center gap-2 text-rose-600 dark:text-rose-400 font-bold text-base">
                <Trash2 className="w-5 h-5" />
                <h3>Delete Connection?</h3>
              </div>
              <button
                onClick={() => setContactToDelete(null)}
                className="p-1 rounded-full text-slate-400 hover:text-slate-600 dark:hover:text-white"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed">
              Are you sure you want to remove <strong>{contactToDelete.displayName}</strong>? All recurring schedules, locked-in upcoming messages, and activity history for this contact will be permanently deleted.
            </p>

            <div className="flex gap-2 justify-end pt-2">
              <button
                type="button"
                onClick={() => setContactToDelete(null)}
                className="px-4 py-2 text-xs font-semibold text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={() => {
                  onDeleteContact(contactToDelete.id);
                  setContactToDelete(null);
                }}
                className="px-4 py-2 text-xs font-bold bg-rose-600 hover:bg-rose-500 text-white rounded-xl transition-colors shadow-sm"
              >
                Delete Connection
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
