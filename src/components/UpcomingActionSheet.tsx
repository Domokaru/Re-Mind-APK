import React, { useState } from 'react';
import { MessageLog } from '../types/remind';
import { BucketBadge } from './BucketBadge';
import { validateLoveGuardrail } from '../core/guardrail';
import {
  Send,
  Dices,
  Edit3,
  SkipForward,
  Clock,
  PauseCircle,
  X,
  AlertTriangle,
  Check,
  Trash2,
} from 'lucide-react';

interface UpcomingActionSheetProps {
  log: MessageLog | null;
  onClose: () => void;
  onSendNow: (log: MessageLog) => void;
  onRerollPhrase: (log: MessageLog) => void;
  onEditOneOffText: (log: MessageLog, newText: string) => boolean;
  onSkipOccurrence: (log: MessageLog) => void;
  onRescheduleOccurrence: (log: MessageLog, newEpoch: number, newDateIso: string) => void;
  onPauseContact: (contactId: number) => void;
}

export const UpcomingActionSheet: React.FC<UpcomingActionSheetProps> = ({
  log,
  onClose,
  onSendNow,
  onRerollPhrase,
  onEditOneOffText,
  onSkipOccurrence,
  onRescheduleOccurrence,
  onPauseContact,
}) => {
  const [isEditing, setIsEditing] = useState(false);
  const [editText, setEditText] = useState(log?.messageText || '');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const [isRescheduling, setIsRescheduling] = useState(false);
  const [targetDate, setTargetDate] = useState(log?.scheduledDateIso || '');
  const [targetHour, setTargetHour] = useState('14');
  const [targetMin, setTargetMin] = useState('00');

  // Confirmation state before discarding/skipping message
  const [isConfirmingSkip, setIsConfirmingSkip] = useState(false);

  if (!log) return null;

  const handleTextChange = (text: string) => {
    setEditText(text);
    const result = validateLoveGuardrail(log.bucket, text);
    if (!result.isValid) {
      setErrorMsg(result.errorMessage || 'Invalid text');
    } else {
      setErrorMsg(null);
    }
  };

  const handleSaveText = () => {
    if (errorMsg || !editText.trim()) return;
    const ok = onEditOneOffText(log, editText);
    if (ok) {
      setIsEditing(false);
    }
  };

  const handleConfirmReschedule = () => {
    if (!targetDate) return;
    const h = parseInt(targetHour, 10);
    const m = parseInt(targetMin, 10);
    const dateObj = new Date(`${targetDate}T${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:00`);
    onRescheduleOccurrence(log, dateObj.getTime(), targetDate);
    setIsRescheduling(false);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
      <div className="w-full sm:max-w-lg bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-t-3xl sm:rounded-2xl p-6 shadow-2xl max-h-[90vh] overflow-y-auto">
        {/* Grab Handle for Mobile */}
        <div className="w-12 h-1 bg-slate-300 dark:bg-slate-700 rounded-full mx-auto mb-4 sm:hidden" />

        {/* Header */}
        <div className="flex items-start justify-between pb-4 border-b border-slate-100 dark:border-slate-800">
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-lg font-bold text-slate-900 dark:text-white">{log.contactDisplayName}</h2>
              <BucketBadge bucket={log.bucket} />
            </div>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">{log.contactPhoneNumber}</p>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-full text-slate-400 hover:text-slate-600 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Message Content Preview or Editor */}
        <div className="my-4">
          {isEditing ? (
            <div className="space-y-2">
              <label className="text-xs font-semibold text-slate-700 dark:text-slate-300">
                Edit One-Off Upcoming Message:
              </label>
              <textarea
                value={editText}
                onChange={(e) => handleTextChange(e.target.value)}
                rows={3}
                className="w-full p-3 bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-xl text-sm text-slate-900 dark:text-slate-100 focus:outline-none focus:border-sky-500"
              />
              {errorMsg && (
                <div className="flex items-center gap-1.5 text-xs text-rose-500 font-medium">
                  <AlertTriangle className="w-4 h-4 shrink-0" />
                  <span>{errorMsg}</span>
                </div>
              )}
              <div className="flex gap-2 justify-end pt-1">
                <button
                  onClick={() => setIsEditing(false)}
                  className="px-3 py-1.5 text-xs font-medium text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg"
                >
                  Cancel
                </button>
                <button
                  onClick={handleSaveText}
                  disabled={!!errorMsg || !editText.trim()}
                  className="px-3 py-1.5 text-xs font-bold text-white dark:text-slate-900 bg-sky-500 dark:bg-sky-400 disabled:opacity-40 rounded-lg shadow-sm"
                >
                  Save Text
                </button>
              </div>
            </div>
          ) : (
            <div className="p-3.5 bg-slate-50 dark:bg-slate-900/80 rounded-xl border border-slate-200 dark:border-slate-800/80">
              <p className="text-sm italic text-slate-800 dark:text-slate-200">"{log.messageText}"</p>
              {log.isCustomEdited && (
                <p className="text-[11px] text-purple-600 dark:text-purple-400 mt-1.5 font-medium">
                  Customized for this occurrence
                </p>
              )}
            </div>
          )}
        </div>

        {/* Reschedule Drawer */}
        {isRescheduling && (
          <div className="p-3 bg-slate-50 dark:bg-slate-900/90 rounded-xl border border-slate-200 dark:border-slate-700 mb-4 space-y-3">
            <h3 className="text-xs font-bold text-sky-600 dark:text-sky-400 uppercase tracking-wider">
              Reschedule Time (7:00 AM - 10:59 PM Window)
            </h3>
            <div className="grid grid-cols-3 gap-2">
              <input
                type="date"
                value={targetDate}
                onChange={(e) => setTargetDate(e.target.value)}
                className="col-span-1 p-2 bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-700 rounded-lg text-xs text-slate-800 dark:text-white"
              />
              <select
                value={targetHour}
                onChange={(e) => setTargetHour(e.target.value)}
                className="col-span-1 p-2 bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-700 rounded-lg text-xs text-slate-800 dark:text-white"
              >
                {Array.from({ length: 16 }).map((_, i) => {
                  const hour = i + 7; // 7 to 22
                  const display = hour === 12 ? '12 PM' : hour > 12 ? `${hour - 12} PM` : `${hour} AM`;
                  return (
                    <option key={hour} value={hour.toString()}>
                      {display}
                    </option>
                  );
                })}
              </select>
              <select
                value={targetMin}
                onChange={(e) => setTargetMin(e.target.value)}
                className="col-span-1 p-2 bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-700 rounded-lg text-xs text-slate-800 dark:text-white"
              >
                <option value="00">:00</option>
                <option value="15">:15</option>
                <option value="30">:30</option>
                <option value="45">:45</option>
              </select>
            </div>
            <div className="flex gap-2 justify-end">
              <button
                onClick={() => setIsRescheduling(false)}
                className="px-3 py-1.5 text-xs text-slate-500 dark:text-slate-400 hover:bg-slate-200 dark:hover:bg-slate-800 rounded-lg"
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmReschedule}
                className="px-3 py-1.5 text-xs font-bold text-white bg-sky-500 rounded-lg"
              >
                Apply New Time
              </button>
            </div>
          </div>
        )}

        {/* Confirmation Banner for Skipping / Deleting Occurrence */}
        {isConfirmingSkip && (
          <div className="p-4 mb-3 bg-amber-50 dark:bg-amber-500/10 border border-amber-200 dark:border-amber-500/30 rounded-2xl space-y-3 animate-fade-in">
            <div className="flex items-center gap-2 text-amber-700 dark:text-amber-400 font-bold text-sm">
              <AlertTriangle className="w-4 h-4 shrink-0" />
              <span>Skip / Discard this message?</span>
            </div>
            <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed">
              Are you sure you want to discard this scheduled occurrence for <strong>{log.contactDisplayName}</strong>? The recurrence engine will mark it skipped and pre-roll their next single occurrence immediately.
            </p>
            <div className="flex gap-2 justify-end">
              <button
                onClick={() => setIsConfirmingSkip(false)}
                className="px-3 py-1.5 text-xs font-semibold text-slate-600 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-800 rounded-lg"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  setIsConfirmingSkip(false);
                  onSkipOccurrence(log);
                  onClose();
                }}
                className="px-3 py-1.5 text-xs font-bold bg-rose-600 hover:bg-rose-500 text-white rounded-lg transition-colors shadow-sm"
              >
                Confirm Skip
              </button>
            </div>
          </div>
        )}

        {/* 6 Actions List */}
        <div className="space-y-1.5 pt-1">
          <button
            onClick={() => {
              onSendNow(log);
              onClose();
            }}
            className="w-full flex items-center gap-3 p-3 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800/80 transition-colors text-left group"
          >
            <div className="p-2 rounded-lg bg-sky-500/10 text-sky-600 dark:text-sky-400 group-hover:bg-sky-500/20">
              <Send className="w-4 h-4" />
            </div>
            <div>
              <div className="text-sm font-semibold text-slate-900 dark:text-white">Send Now</div>
              <div className="text-xs text-slate-500 dark:text-slate-400">
                Dispatch SMS now and pre-roll next occurrence
              </div>
            </div>
          </button>

          <button
            onClick={() => {
              onRerollPhrase(log);
              onClose();
            }}
            className="w-full flex items-center gap-3 p-3 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800/80 transition-colors text-left group"
          >
            <div className="p-2 rounded-lg bg-amber-500/10 text-amber-600 dark:text-amber-400 group-hover:bg-amber-500/20">
              <Dices className="w-4 h-4" />
            </div>
            <div>
              <div className="text-sm font-semibold text-slate-900 dark:text-white">Reroll Phrase</div>
              <div className="text-xs text-slate-500 dark:text-slate-400">
                Select a different phrase from {log.bucket} pool
              </div>
            </div>
          </button>

          <button
            onClick={() => setIsEditing(true)}
            className="w-full flex items-center gap-3 p-3 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800/80 transition-colors text-left group"
          >
            <div className="p-2 rounded-lg bg-purple-500/10 text-purple-600 dark:text-purple-400 group-hover:bg-purple-500/20">
              <Edit3 className="w-4 h-4" />
            </div>
            <div>
              <div className="text-sm font-semibold text-slate-900 dark:text-white">Edit Text (One-Off)</div>
              <div className="text-xs text-slate-500 dark:text-slate-400">
                Customize message for just this upcoming send
              </div>
            </div>
          </button>

          <button
            onClick={() => setIsConfirmingSkip(true)}
            className="w-full flex items-center gap-3 p-3 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800/80 transition-colors text-left group"
          >
            <div className="p-2 rounded-lg bg-slate-100 dark:bg-slate-700/20 text-slate-600 dark:text-slate-400 group-hover:bg-slate-200 dark:group-hover:bg-slate-700/30">
              <SkipForward className="w-4 h-4" />
            </div>
            <div>
              <div className="text-sm font-semibold text-slate-900 dark:text-white">Skip / Discard Occurrence</div>
              <div className="text-xs text-slate-500 dark:text-slate-400">
                Requires confirmation; pre-rolls next date
              </div>
            </div>
          </button>

          <button
            onClick={() => setIsRescheduling(!isRescheduling)}
            className="w-full flex items-center gap-3 p-3 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800/80 transition-colors text-left group"
          >
            <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 group-hover:bg-emerald-500/20">
              <Clock className="w-4 h-4" />
            </div>
            <div>
              <div className="text-sm font-semibold text-slate-900 dark:text-white">Reschedule Time / Date</div>
              <div className="text-xs text-slate-500 dark:text-slate-400">
                Shift target timestamp within 7:00 AM - 10:59 PM
              </div>
            </div>
          </button>

          <button
            onClick={() => {
              onPauseContact(log.contactScheduleId);
              onClose();
            }}
            className="w-full flex items-center gap-3 p-3 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800/80 transition-colors text-left group"
          >
            <div className="p-2 rounded-lg bg-rose-500/10 text-rose-600 dark:text-rose-400 group-hover:bg-rose-500/20">
              <PauseCircle className="w-4 h-4" />
            </div>
            <div>
              <div className="text-sm font-semibold text-slate-900 dark:text-white">Pause Contact Schedule</div>
              <div className="text-xs text-slate-500 dark:text-slate-400">
                Pause automated check-ins for this person
              </div>
            </div>
          </button>
        </div>
      </div>
    </div>
  );
};
