import React, { useState, useMemo } from 'react';
import {
  ContactSchedule,
  GROUP_BUCKETS,
  MessageLog,
  VirtualCalendarOccurrence,
} from '../types/remind';
import { BucketBadge } from '../components/BucketBadge';
import { projectFutureOccurrences } from '../core/recurrence';
import {
  ChevronLeft,
  ChevronRight,
  Calendar as CalendarIcon,
  Pencil,
  ChevronDown,
  ChevronUp,
} from 'lucide-react';

interface CalendarViewProps {
  contacts: ContactSchedule[];
  pendingMessages: MessageLog[];
  historyMessages: MessageLog[];
  onSelectUpcoming: (log: MessageLog) => void;
}

export const CalendarView: React.FC<CalendarViewProps> = ({
  contacts,
  pendingMessages,
  historyMessages,
  onSelectUpcoming,
}) => {
  const [currentDate, setCurrentDate] = useState(() => new Date());
  const [selectedDateIso, setSelectedDateIso] = useState<string>(() => {
    return new Date().toISOString().split('T')[0];
  });
  const [expandedPendingIds, setExpandedPendingIds] = useState<Record<number | string, boolean>>({});

  const toggleExpandPending = (id: number | string) => {
    setExpandedPendingIds((prev) => ({
      ...prev,
      [id]: !prev[id],
    }));
  };

  const year = currentDate.getFullYear();
  const month = currentDate.getMonth();

  // Dynamic Virtual Projections across the visible month
  const virtualProjections = useMemo(() => {
    const list: VirtualCalendarOccurrence[] = [];
    contacts.forEach((contact) => {
      const pending = pendingMessages.find(
        (p) => p.contactScheduleId === contact.id
      );
      const pendingDateIso =
        pending?.scheduledDateIso || new Date().toISOString().split('T')[0];
      const projected = projectFutureOccurrences(
        contact,
        pendingDateIso,
        year,
        month
      );
      list.push(...projected);
    });
    return list;
  }, [contacts, pendingMessages, year, month]);

  // Calendar math
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const firstDayOfWeek = new Date(year, month, 1).getDay(); // 0=Sun, 1=Mon...

  const monthName = currentDate.toLocaleString('default', {
    month: 'long',
    year: 'numeric',
  });

  const prevMonth = () => {
    setCurrentDate(new Date(year, month - 1, 1));
  };

  const nextMonth = () => {
    setCurrentDate(new Date(year, month + 1, 1));
  };

  const jumpToToday = () => {
    const now = new Date();
    setCurrentDate(now);
    setSelectedDateIso(now.toISOString().split('T')[0]);
  };

  // Selected Day agenda items
  const dayPending = pendingMessages.filter(
    (p) => p.scheduledDateIso === selectedDateIso
  );
  const dayHistory = historyMessages.filter(
    (h) => h.scheduledDateIso === selectedDateIso
  );
  const dayProjections = virtualProjections.filter(
    (v) => v.projectedDateIso === selectedDateIso
  );

  const selectedDateObj = new Date(selectedDateIso + 'T00:00:00');
  const selectedDateHeader = selectedDateObj.toLocaleDateString('en-US', {
    weekday: 'long',
    month: 'short',
    day: 'numeric',
  });

  return (
    <div className="space-y-4">
      {/* Month Navigation Header */}
      <div className="flex items-center justify-between p-3 bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl shadow-sm">
        <h2 className="text-base font-bold text-slate-900 dark:text-white pl-2">{monthName}</h2>
        <div className="flex items-center gap-1">
          <button
            onClick={prevMonth}
            className="p-1.5 rounded-lg text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            <ChevronLeft className="w-5 h-5" />
          </button>
          <button
            onClick={jumpToToday}
            className="px-2.5 py-1 text-xs font-semibold text-sky-600 dark:text-sky-400 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg transition-colors"
          >
            Today
          </button>
          <button
            onClick={nextMonth}
            className="p-1.5 rounded-lg text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            <ChevronRight className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* Month Grid */}
      <div className="bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl p-4 shadow-sm">
        {/* Weekday headers */}
        <div className="grid grid-cols-7 gap-1 text-center text-[11px] font-bold text-slate-400 dark:text-slate-500 mb-2">
          <span>Sun</span>
          <span>Mon</span>
          <span>Tue</span>
          <span>Wed</span>
          <span>Thu</span>
          <span>Fri</span>
          <span>Sat</span>
        </div>

        {/* Days grid */}
        <div className="grid grid-cols-7 gap-1.5">
          {Array.from({ length: firstDayOfWeek }).map((_, i) => (
            <div key={`empty-${i}`} className="h-12 rounded-xl" />
          ))}

          {Array.from({ length: daysInMonth }).map((_, i) => {
            const dayNum = i + 1;
            const dateIso = `${year}-${(month + 1).toString().padStart(2, '0')}-${dayNum.toString().padStart(2, '0')}`;
            const isSelected = dateIso === selectedDateIso;
            const isToday =
              dateIso === new Date().toISOString().split('T')[0];

            // Events on this date
            const pendingOnDate = pendingMessages.filter(
              (p) => p.scheduledDateIso === dateIso
            );
            const historyOnDate = historyMessages.filter(
              (h) => h.scheduledDateIso === dateIso
            );
            const projectedOnDate = virtualProjections.filter(
              (v) => v.projectedDateIso === dateIso
            );

            const allBucketsOnDay = [
              ...pendingOnDate.map((p) => p.bucket),
              ...historyOnDate.map((h) => h.bucket),
              ...projectedOnDate.map((v) => v.bucket),
            ];

            const distinctBuckets = Array.from(new Set(allBucketsOnDay));

            return (
              <button
                key={dayNum}
                onClick={() => setSelectedDateIso(dateIso)}
                className={`h-12 rounded-xl flex flex-col items-center justify-between p-1.5 transition-all text-xs font-semibold ${
                  isSelected
                    ? 'bg-sky-100 dark:bg-sky-500/20 border-2 border-sky-500 dark:border-sky-400 text-sky-800 dark:text-sky-300 font-bold'
                    : isToday
                      ? 'bg-sky-50 dark:bg-slate-800 border border-sky-400/80 text-sky-900 dark:text-white'
                      : 'bg-slate-50 dark:bg-slate-900/60 border border-slate-200 dark:border-slate-800/80 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800'
                }`}
              >
                <span>{dayNum}</span>

                {/* Color Dots for Group Buckets */}
                <div className="flex gap-1 h-1.5 items-center">
                  {distinctBuckets.slice(0, 3).map((b) => (
                    <span
                      key={b}
                      className="w-1.5 h-1.5 rounded-full"
                      style={{ backgroundColor: `var(--bucket-${b.toLowerCase()})` }}
                    />
                  ))}
                  {distinctBuckets.length > 3 && (
                    <span className="w-1 h-1 rounded-full bg-slate-400 dark:bg-slate-500" />
                  )}
                </div>
              </button>
            );
          })}
        </div>
      </div>

      {/* Selected Day Agenda */}
      <div className="space-y-2.5">
        <div className="flex items-center justify-between px-1">
          <h3 className="text-xs font-bold uppercase tracking-wider text-slate-600 dark:text-slate-400 flex items-center gap-1.5">
            <CalendarIcon className="w-3.5 h-3.5 text-sky-600 dark:text-sky-400" />
            <span>Agenda for {selectedDateHeader}</span>
          </h3>
          <span className="text-[11px] text-slate-400 dark:text-slate-500">
            {dayPending.length + dayHistory.length + dayProjections.length} items
          </span>
        </div>

        {dayPending.length === 0 &&
          dayHistory.length === 0 &&
          dayProjections.length === 0 && (
            <div className="p-6 text-center bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl text-xs text-slate-400 dark:text-slate-500 shadow-sm">
              No automated check-ins scheduled or executed on this day.
            </div>
          )}

        {/* 1. Next Occurrence (PENDING) - Collapsed down to person name, expandable, pen icon for manual actions */}
        {dayPending.map((log) => {
          const isExpanded = !!expandedPendingIds[log.id];
          return (
            <div
              key={`pending-${log.id}`}
              className="bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 hover:border-sky-400/60 dark:hover:border-sky-500/40 rounded-2xl shadow-sm transition-all overflow-hidden"
            >
              <div
                onClick={() => toggleExpandPending(log.id)}
                className="p-3.5 flex items-center justify-between cursor-pointer group"
              >
                <div className="flex items-center gap-2">
                  <span className="text-sm font-bold text-slate-900 dark:text-white group-hover:text-sky-600 dark:group-hover:text-sky-400 transition-colors">
                    {log.contactDisplayName}
                  </span>
                  <BucketBadge bucket={log.bucket} />
                </div>

                <div className="flex items-center gap-1">
                  {/* Pen icon replaces Next Locked and triggers manual actions */}
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      onSelectUpcoming(log);
                    }}
                    className="p-1.5 rounded-lg text-slate-500 hover:text-sky-600 dark:text-slate-400 dark:hover:text-sky-400 hover:bg-sky-50 dark:hover:bg-slate-800 transition-colors"
                    title="Manual actions"
                    aria-label="Manual actions"
                  >
                    <Pencil className="w-4 h-4" />
                  </button>

                  {/* Expand / Collapse toggle chevron */}
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      toggleExpandPending(log.id);
                    }}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 transition-colors"
                    title={isExpanded ? 'Collapse' : 'Expand'}
                    aria-label={isExpanded ? 'Collapse' : 'Expand'}
                  >
                    {isExpanded ? (
                      <ChevronUp className="w-4 h-4" />
                    ) : (
                      <ChevronDown className="w-4 h-4" />
                    )}
                  </button>
                </div>
              </div>

              {/* Expanded content */}
              {isExpanded && (
                <div className="px-3.5 pb-3.5 border-t border-slate-100 dark:border-slate-800/80">
                  <p className="text-xs text-slate-700 dark:text-slate-300 italic mt-2.5">
                    "{log.messageText}"
                  </p>
                  <div className="mt-2 text-[11px] text-slate-400 dark:text-slate-500 flex items-center justify-between">
                    <span>{log.contactPhoneNumber}</span>
                    <span>Scheduled dispatch</span>
                  </div>
                </div>
              )}
            </div>
          );
        })}

        {/* 2. Virtual Future Projections (Occurrences beyond Next-1) */}
        {dayProjections.map((proj, idx) => (
          <div
            key={`proj-${proj.contactScheduleId}-${idx}`}
            className="p-3.5 bg-slate-50/80 dark:bg-[#111827]/60 border border-dashed border-slate-300 dark:border-slate-700/80 rounded-2xl space-y-1.5 shadow-sm"
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className="text-xs font-semibold text-slate-800 dark:text-slate-300">
                  {proj.displayName}
                </span>
                <BucketBadge bucket={proj.bucket} />
              </div>
              <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-slate-200 dark:bg-slate-800 text-slate-600 dark:text-slate-400 border border-slate-300 dark:border-slate-700">
                Projected Cadence
              </span>
            </div>
            <div className="flex items-center justify-between text-xs text-slate-500 dark:text-slate-400">
              <span>Target: {proj.timeSummary}</span>
              <span className="text-[11px] italic text-slate-400 dark:text-slate-500">
                {proj.placeholderNote}
              </span>
            </div>
          </div>
        ))}

        {/* 3. Executed History on this day */}
        {dayHistory.map((hist) => (
          <div
            key={`hist-${hist.id}`}
            className="p-3 bg-slate-50 dark:bg-slate-900/80 border border-slate-200 dark:border-slate-800 rounded-2xl space-y-1 text-xs shadow-sm"
          >
            <div className="flex items-center justify-between">
              <span className="font-bold text-slate-900 dark:text-white">{hist.contactDisplayName}</span>
              <span
                className={`px-1.5 py-0.5 rounded text-[10px] font-bold ${
                  hist.status === 'SENT'
                    ? 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400'
                    : 'bg-amber-500/10 text-amber-600 dark:text-amber-400'
                }`}
              >
                {hist.status}
              </span>
            </div>
            <p className="text-slate-600 dark:text-slate-400 italic">"{hist.messageText}"</p>
          </div>
        ))}
      </div>
    </div>
  );
};
