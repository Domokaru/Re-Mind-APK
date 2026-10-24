import React, { useState } from 'react';
import {
  ContactSchedule,
  GROUP_BUCKETS,
  GroupBucket,
  RecurrenceType,
  TimeMode,
  BucketMetadata,
} from '../types/remind';
import { X, Plus, Save, XCircle, BookUser } from 'lucide-react';

interface ContactEditModalProps {
  schedule?: ContactSchedule | null;
  buckets?: BucketMetadata[];
  onClose: () => void;
  onSave: (schedule: ContactSchedule) => void;
  onOpenCreateBucket?: () => void;
}

export const ContactEditModal: React.FC<ContactEditModalProps> = ({
  schedule,
  buckets,
  onClose,
  onSave,
  onOpenCreateBucket,
}) => {
  const [displayName, setDisplayName] = useState(schedule?.displayName || '');
  const [phoneNumber, setPhoneNumber] = useState(schedule?.phoneNumber || '');
  const [bucket, setBucket] = useState<GroupBucket>(schedule?.bucket || 'FRIENDS');
  const [recurrenceType, setRecurrenceType] = useState<RecurrenceType>(
    schedule?.recurrenceType || 'WEEKLY'
  );
  const [intervalN, setIntervalN] = useState(schedule?.intervalN || 2);
  const [daysCsv, setDaysCsv] = useState(schedule?.daysOfWeekCsv || '1,4');
  const [dayOfMonth, setDayOfMonth] = useState(schedule?.dayOfMonth || 1);
  const [timeMode, setTimeMode] = useState<TimeMode>(
    schedule?.timeMode || 'RANDOM_WINDOW_1HR'
  );
  const [targetHour, setTargetHour] = useState(schedule?.targetHour || 14);
  const [targetMinute, setTargetMinute] = useState(schedule?.targetMinute || 15);
  const [pickerStatus, setPickerStatus] = useState<string | null>(null);

  const handlePickFromPhoneContacts = async () => {
    setPickerStatus(null);
    if ('contacts' in navigator && (navigator as any).contacts?.select) {
      try {
        const props = ['name', 'tel'];
        const opts = { multiple: false };
        const contacts = await (navigator as any).contacts.select(props, opts);
        if (contacts && contacts.length > 0) {
          const selected = contacts[0];
          let name = '';
          if (Array.isArray(selected.name) && selected.name.length > 0) {
            name = selected.name[0];
          } else if (typeof selected.name === 'string') {
            name = selected.name;
          }

          let tel = '';
          if (Array.isArray(selected.tel) && selected.tel.length > 0) {
            tel = selected.tel[0];
          } else if (typeof selected.tel === 'string') {
            tel = selected.tel;
          }

          if (name) setDisplayName(name.trim());
          if (tel) setPhoneNumber(tel.trim());
        }
      } catch (err: any) {
        if (err?.name !== 'AbortError') {
          console.warn('Contact picker error:', err);
        }
      }
    } else {
      setPickerStatus(
        'Contact picker is active when used on your phone (Android / Chrome). On desktop browsers, enter the name & phone number manually below.'
      );
    }
  };

  const toggleDayOfWeek = (isoDay: number) => {
    const current = daysCsv
      .split(',')
      .map((s) => parseInt(s.trim(), 10))
      .filter((n) => !isNaN(n));
    const next = current.includes(isoDay)
      ? current.filter((d) => d !== isoDay)
      : [...current, isoDay].sort();
    setDaysCsv(next.join(','));
  };

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    if (!displayName.trim() || !phoneNumber.trim()) return;

    const normalized = phoneNumber.trim().replace(/[^0-9+]/g, '');

    const entity: ContactSchedule = {
      id: schedule?.id || 0,
      contactLookupKey:
        schedule?.contactLookupKey || `lookup_${Date.now()}`,
      contactId: schedule?.contactId || Date.now(),
      displayName: displayName.trim(),
      phoneNumber: phoneNumber.trim(),
      normalizedNumber: normalized.startsWith('+') ? normalized : `+1${normalized}`,
      bucket,
      recurrenceType,
      intervalN: Math.max(1, intervalN),
      daysOfWeekCsv: daysCsv,
      dayOfMonth,
      monthOfYear: 1,
      timeMode,
      targetHour: Math.min(22, Math.max(7, targetHour)),
      targetMinute: Math.min(59, Math.max(0, targetMinute)),
      isActive: schedule ? schedule.isActive : true,
      lastSentPhraseId: schedule?.lastSentPhraseId,
      lastSentPhraseText: schedule?.lastSentPhraseText,
      lastDispatchedDateIso: schedule?.lastDispatchedDateIso,
      lastManualInteractionEpochMillis:
        schedule?.lastManualInteractionEpochMillis || 0,
      createdAtEpochMillis: schedule?.createdAtEpochMillis || Date.now(),
      updatedAtEpochMillis: Date.now(),
    };

    onSave(entity);
    onClose();
  };

  const dayLabels = [
    { label: 'Mon', iso: 1 },
    { label: 'Tue', iso: 2 },
    { label: 'Wed', iso: 3 },
    { label: 'Thu', iso: 4 },
    { label: 'Fri', iso: 5 },
    { label: 'Sat', iso: 6 },
    { label: 'Sun', iso: 7 },
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
      <div className="w-full max-w-lg bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-2xl max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between pb-4 border-b border-slate-100 dark:border-slate-800">
          <h2 className="text-lg font-bold text-slate-900 dark:text-white">
            {schedule ? 'Edit Schedule' : 'Add Connection Schedule'}
          </h2>
          <button
            onClick={onClose}
            className="p-1 rounded-full text-slate-400 hover:text-slate-600 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSave} className="space-y-4 pt-4">
          {/* Pick Contact Button */}
          <button
            type="button"
            onClick={handlePickFromPhoneContacts}
            className="w-full py-2.5 px-4 bg-[#08dac9]/10 hover:bg-[#08dac9]/20 text-[#08dac9] border border-[#08dac9]/30 rounded-xl text-xs font-bold flex items-center justify-center gap-2 transition-all shadow-sm active:scale-[0.99]"
            title="Pick a contact from your phone"
          >
            <BookUser className="w-4 h-4 text-[#08dac9]" />
            <span>Pick Contact</span>
          </button>

          {pickerStatus && (
            <div className="text-[11px] text-amber-700 dark:text-amber-300 bg-amber-50 dark:bg-amber-500/10 p-2.5 rounded-xl border border-amber-200 dark:border-amber-500/20 text-center">
              {pickerStatus}
            </div>
          )}

          <div>
            <label className="text-xs font-semibold text-slate-700 dark:text-slate-300">
              Contact Display Name
            </label>
            <input
              type="text"
              required
              value={displayName}
              onChange={(e) => setDisplayName(e.target.value)}
              placeholder="e.g. Sarah Hallauer, Mom, Marcus"
              className="mt-1 w-full p-2.5 bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-xl text-sm text-slate-900 dark:text-white focus:outline-none focus:border-sky-500"
            />
          </div>

          <div>
            <label className="text-xs font-semibold text-slate-700 dark:text-slate-300">
              Phone Number (Mobile Default)
            </label>
            <input
              type="tel"
              required
              value={phoneNumber}
              onChange={(e) => setPhoneNumber(e.target.value)}
              placeholder="+1 (555) 234-5678"
              className="mt-1 w-full p-2.5 bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-xl text-sm text-slate-900 dark:text-white focus:outline-none focus:border-sky-500"
            />
          </div>

          {/* Group Bucket Selector */}
          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="text-xs font-semibold text-slate-700 dark:text-slate-300">
                Group Bucket (Strictly 1)
              </label>
              {onOpenCreateBucket && (
                <button
                  type="button"
                  onClick={onOpenCreateBucket}
                  className="text-[11px] font-bold text-[#08dac9] hover:underline flex items-center gap-0.5"
                >
                  <Plus className="w-3 h-3" />
                  <span>New Group</span>
                </button>
              )}
            </div>
            <div className="grid grid-cols-3 sm:grid-cols-5 gap-2">
              {(buckets && buckets.length > 0 ? buckets : (Object.values(GROUP_BUCKETS) as BucketMetadata[])).map((meta) => {
                const b = meta.id;
                const isSelected = bucket === b;
                const color = meta.darkColorHex;
                return (
                  <button
                    key={b}
                    type="button"
                    onClick={() => setBucket(b)}
                    className={`py-2 px-1 rounded-lg text-xs font-semibold text-center transition-all ${
                      isSelected
                        ? 'ring-2 ring-[#08dac9] shadow-md'
                        : 'bg-slate-100 dark:bg-slate-900 text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
                    }`}
                    style={{
                      backgroundColor: isSelected
                        ? `color-mix(in srgb, ${color} 20%, transparent)`
                        : undefined,
                      color: isSelected ? color : undefined,
                    }}
                  >
                    {meta.displayName}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Recurrence Cadence */}
          <div>
            <label className="text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5 block">
              Recurrence Cadence
            </label>
            <select
              value={recurrenceType}
              onChange={(e) => setRecurrenceType(e.target.value as RecurrenceType)}
              className="w-full p-2.5 bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-xl text-sm text-slate-900 dark:text-white focus:outline-none focus:border-sky-500"
            >
              <option value="DAILY">Daily (Every day)</option>
              <option value="EVERY_N_DAYS">Every N Days</option>
              <option value="WEEKLY">Weekly (Specific days)</option>
              <option value="EVERY_N_WEEKS">Every N Weeks</option>
              <option value="MONTHLY_DAY">Monthly on Day X</option>
              <option value="YEARLY_DATE">Yearly on Date</option>
            </select>
          </div>

          {/* Specific Recurrence Sub-Options */}
          {recurrenceType === 'EVERY_N_DAYS' && (
            <div>
              <label className="text-xs text-slate-500 dark:text-slate-400">Interval (Days)</label>
              <input
                type="number"
                min={2}
                max={365}
                value={intervalN}
                onChange={(e) => setIntervalN(parseInt(e.target.value, 10))}
                className="mt-1 w-full p-2 bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-lg text-sm text-slate-900 dark:text-white"
              />
            </div>
          )}

          {recurrenceType === 'WEEKLY' && (
            <div>
              <label className="text-xs text-slate-500 dark:text-slate-400 block mb-1.5">
                Select Active Days of Week
              </label>
              <div className="flex gap-1.5">
                {dayLabels.map(({ label, iso }) => {
                  const currentDays = daysCsv
                    .split(',')
                    .map((s) => parseInt(s.trim(), 10));
                  const isChecked = currentDays.includes(iso);
                  return (
                    <button
                      key={iso}
                      type="button"
                      onClick={() => toggleDayOfWeek(iso)}
                      className={`flex-1 py-1.5 rounded-lg text-xs font-bold transition-colors ${
                        isChecked
                          ? 'bg-[#08dac9] text-[#04201d]'
                          : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400'
                      }`}
                    >
                      {label}
                    </button>
                  );
                })}
              </div>
            </div>
          )}

          {recurrenceType === 'MONTHLY_DAY' && (
            <div>
              <label className="text-xs text-slate-500 dark:text-slate-400">Day of Month (1..31 or -1 for Last Day)</label>
              <input
                type="number"
                min={-1}
                max={31}
                value={dayOfMonth}
                onChange={(e) => setDayOfMonth(parseInt(e.target.value, 10))}
                className="mt-1 w-full p-2 bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-lg text-sm text-slate-900 dark:text-white"
              />
            </div>
          )}

          {/* Timing Mode */}
          <div className="pt-2 border-t border-slate-100 dark:border-slate-800">
            <label className="text-xs font-semibold text-slate-700 dark:text-slate-300 block mb-2">
              Dispatch Timing Mode (7:00 AM to 10:59 PM Safe Window)
            </label>
            <div className="grid grid-cols-2 gap-3">
              <label className="flex items-center gap-2 p-2.5 bg-slate-50 dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 cursor-pointer">
                <input
                  type="radio"
                  name="timeMode"
                  checked={timeMode === 'EXACT_TIME'}
                  onChange={() => setTimeMode('EXACT_TIME')}
                  className="text-sky-500"
                />
                <span className="text-xs font-medium text-slate-800 dark:text-slate-200">
                  Exact Time
                </span>
              </label>
              <label className="flex items-center gap-2 p-2.5 bg-slate-50 dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 cursor-pointer">
                <input
                  type="radio"
                  name="timeMode"
                  checked={timeMode === 'RANDOM_WINDOW_1HR'}
                  onChange={() => setTimeMode('RANDOM_WINDOW_1HR')}
                  className="text-sky-500"
                />
                <span className="text-xs font-medium text-slate-800 dark:text-slate-200">
                  1-Hour Random Window
                </span>
              </label>
            </div>

            <div className="grid grid-cols-2 gap-3 mt-3">
              <div>
                <label className="text-xs text-slate-500 dark:text-slate-400">Start Hour</label>
                <select
                  value={targetHour}
                  onChange={(e) => setTargetHour(parseInt(e.target.value, 10))}
                  className="mt-1 w-full p-2 bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-lg text-xs text-slate-900 dark:text-white"
                >
                  {Array.from({ length: 16 }, (_, i) => i + 7).map((h) => (
                    <option key={h} value={h}>
                      {h % 12 === 0 ? 12 : h % 12}:00 {h >= 12 ? 'PM' : 'AM'}
                    </option>
                  ))}
                </select>
              </div>

              {timeMode === 'EXACT_TIME' && (
                <div>
                  <label className="text-xs text-slate-500 dark:text-slate-400">Minute</label>
                  <select
                    value={targetMinute}
                    onChange={(e) => setTargetMinute(parseInt(e.target.value, 10))}
                    className="mt-1 w-full p-2 bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-lg text-xs text-slate-900 dark:text-white"
                  >
                    {[0, 15, 30, 45].map((m) => (
                      <option key={m} value={m}>
                        :{m.toString().padStart(2, '0')}
                      </option>
                    ))}
                  </select>
                </div>
              )}
            </div>
          </div>

          <div className="flex items-center gap-3 justify-end pt-4 border-t border-slate-100 dark:border-slate-800">
            <button
              type="button"
              onClick={onClose}
              className="p-2.5 text-slate-400 hover:text-rose-500 dark:hover:text-rose-400 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors"
              title="Cancel"
              aria-label="Cancel"
            >
              <XCircle className="w-6 h-6" />
            </button>
            <button
              type="submit"
              className="p-2.5 text-[#04201d] font-bold bg-[#08dac9] hover:bg-[#07c2b3] rounded-xl transition-all shadow-md shadow-[#08dac9]/20 flex items-center justify-center"
              title="Save Schedule"
              aria-label="Save Schedule"
            >
              <Save className="w-6 h-6" />
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
