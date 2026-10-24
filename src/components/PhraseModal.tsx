import React, { useState } from 'react';
import { GROUP_BUCKETS, GroupBucket, Phrase, BucketMetadata } from '../types/remind';
import { validateLoveGuardrail } from '../core/guardrail';
import { X, AlertTriangle, ShieldCheck } from 'lucide-react';

interface PhraseModalProps {
  phrase?: Phrase | null;
  defaultBucket?: GroupBucket;
  buckets?: BucketMetadata[];
  onClose: () => void;
  onSave: (data: Omit<Phrase, 'id' | 'createdAtEpochMillis'> & { id?: number }) => boolean;
}

export const PhraseModal: React.FC<PhraseModalProps> = ({
  phrase,
  defaultBucket = 'SPOUSE',
  buckets,
  onClose,
  onSave,
}) => {
  const activeBuckets = buckets && buckets.length > 0 ? buckets : (Object.values(GROUP_BUCKETS) as BucketMetadata[]);
  const [bucket, setBucket] = useState<GroupBucket>(phrase?.bucket || defaultBucket);
  const [text, setText] = useState(phrase?.text || '');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const handleTextChange = (val: string, currentBucket: GroupBucket) => {
    setText(val);
    const result = validateLoveGuardrail(currentBucket, val);
    if (!result.isValid) {
      setErrorMsg(result.errorMessage || 'Invalid phrase');
    } else {
      setErrorMsg(null);
    }
  };

  const handleBucketChange = (newBucket: GroupBucket) => {
    setBucket(newBucket);
    handleTextChange(text, newBucket);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (errorMsg || !text.trim()) return;

    const ok = onSave({
      id: phrase?.id,
      bucket,
      text: text.trim(),
      isSeed: phrase ? phrase.isSeed : false,
    });
    if (ok) {
      onClose();
    }
  };

  const currentMeta = GROUP_BUCKETS[bucket] || activeBuckets.find((b) => b.id === bucket) || {
    id: bucket,
    displayName: bucket,
    allowsLoveWord: false,
    darkColorHex: '#08dac9',
    lightColorHex: '#069689',
  };

  const currentColor = currentMeta.darkColorHex;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
      <div className="w-full max-w-lg bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-2xl">
        <div className="flex items-center justify-between pb-4 border-b border-slate-100 dark:border-slate-800">
          <div className="flex items-center gap-2">
            <h2 className="text-lg font-bold text-slate-900 dark:text-white">
              {phrase ? 'Edit Phrase' : 'Add Phrase to Pool'}
            </h2>
            <span
              className="px-2 py-0.5 rounded text-[11px] font-bold border"
              style={{
                backgroundColor: `color-mix(in srgb, ${currentColor} 15%, transparent)`,
                color: currentColor,
                borderColor: `color-mix(in srgb, ${currentColor} 30%, transparent)`,
              }}
            >
              {currentMeta.displayName}
            </span>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-full text-slate-400 hover:text-slate-600 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4 pt-4">
          <div>
            <label className="text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5 block">
              Relationship Group Bucket
            </label>
            <div className="grid grid-cols-3 sm:grid-cols-5 gap-1.5">
              {activeBuckets.map((meta) => {
                const b = meta.id;
                const isSelected = bucket === b;
                const color = meta.darkColorHex;
                return (
                  <button
                    key={b}
                    type="button"
                    onClick={() => handleBucketChange(b)}
                    className={`py-1.5 px-1 rounded-lg text-xs font-semibold transition-all ${
                      isSelected
                        ? 'ring-2 ring-[#08dac9] shadow-sm'
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

          {!currentMeta.allowsLoveWord && (
            <div className="p-2.5 bg-blue-500/10 border border-blue-500/20 rounded-xl flex items-center gap-2 text-xs text-blue-700 dark:text-blue-300">
              <ShieldCheck className="w-4 h-4 shrink-0 text-blue-500 dark:text-blue-400" />
              <span>
                <strong>Lexical Guardrail Active:</strong> Phrases in{' '}
                {currentMeta.displayName} cannot contain 'love' or 'luv'.
              </span>
            </div>
          )}

          <div>
            <label className="text-xs font-semibold text-slate-700 dark:text-slate-300">
              Phrase Text (Static message sent silently via SMS)
            </label>
            <textarea
              required
              rows={4}
              value={text}
              onChange={(e) => handleTextChange(e.target.value, bucket)}
              placeholder="Write a grounded, sincere check-in (e.g. Hope your week is off to a solid start...)"
              className="mt-1 w-full p-3 bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-xl text-sm text-slate-900 dark:text-slate-100 focus:outline-none focus:border-sky-500"
            />
          </div>

          {errorMsg && (
            <div className="p-2.5 bg-rose-500/10 border border-rose-500/20 rounded-xl flex items-start gap-2 text-xs text-rose-700 dark:text-rose-300">
              <AlertTriangle className="w-4 h-4 shrink-0 text-rose-500 dark:text-rose-400 mt-0.5" />
              <div>
                <strong>Guardrail Error:</strong> {errorMsg}
              </div>
            </div>
          )}

          <div className="flex gap-3 justify-end pt-3 border-t border-slate-100 dark:border-slate-800">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-medium text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!!errorMsg || !text.trim()}
              className="px-5 py-2 text-xs font-bold text-[#04201d] bg-[#08dac9] hover:bg-[#07c2b3] disabled:opacity-40 rounded-xl transition-colors shadow-sm"
            >
              Save Phrase
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
