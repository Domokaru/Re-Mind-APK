import React, { useState } from 'react';
import { GROUP_BUCKETS, GroupBucket, Phrase, BucketMetadata } from '../types/remind';
import { Plus, Edit2, Trash2, ArrowLeft, X } from 'lucide-react';

interface PhrasesViewProps {
  phrases: Phrase[];
  buckets?: BucketMetadata[];
  onAddNewPhrase: (bucket: GroupBucket) => void;
  onEditPhrase: (phrase: Phrase) => void;
  onDeletePhrase: (phraseId: number) => void;
  onOpenCreateBucket?: () => void;
  onBackToSettings?: () => void;
}

export const PhrasesView: React.FC<PhrasesViewProps> = ({
  phrases,
  buckets,
  onAddNewPhrase,
  onEditPhrase,
  onDeletePhrase,
  onOpenCreateBucket,
  onBackToSettings,
}) => {
  const activeBuckets = buckets && buckets.length > 0 ? buckets : (Object.values(GROUP_BUCKETS) as BucketMetadata[]);
  const [selectedBucket, setSelectedBucket] = useState<GroupBucket>(() => activeBuckets[0]?.id || 'SPOUSE');
  const [phraseToDelete, setPhraseToDelete] = useState<Phrase | null>(null);

  const currentMeta = activeBuckets.find((b) => b.id === selectedBucket) || activeBuckets[0] || GROUP_BUCKETS['SPOUSE'];
  const bucketPhrases = phrases.filter((p) => p.bucket === selectedBucket);

  return (
    <div className="space-y-4">
      {/* Header with Back button if navigated from Settings */}
      {onBackToSettings && (
        <div className="flex items-center justify-between pb-1">
          <button
            onClick={onBackToSettings}
            className="flex items-center gap-1.5 text-xs font-bold text-[#08dac9] hover:underline"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Back to Settings</span>
          </button>
          <span className="text-xs font-semibold text-slate-500 dark:text-slate-400">
            {phrases.length} Total Phrases
          </span>
        </div>
      )}

      {/* Group Bucket Tabs with New Group Button */}
      <div className="flex gap-1.5 overflow-x-auto pb-1 no-scrollbar items-center">
        {activeBuckets.map((meta) => {
          const b = meta.id;
          const isSelected = selectedBucket === b;
          const count = phrases.filter((p) => p.bucket === b).length;
          const color = meta.darkColorHex;

          return (
            <button
              key={b}
              onClick={() => setSelectedBucket(b)}
              className={`px-3 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition-all flex items-center gap-1.5 ${
                isSelected
                  ? 'shadow-sm ring-2 ring-[#08dac9]'
                  : 'bg-white dark:bg-[#111827] text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white border border-slate-200 dark:border-slate-800'
              }`}
              style={{
                backgroundColor: isSelected
                  ? `color-mix(in srgb, ${color} 18%, transparent)`
                  : undefined,
                color: isSelected ? color : undefined,
              }}
            >
              <span>{meta.displayName}</span>
              <span
                className={`text-[10px] px-1.5 py-0.2 rounded-full ${
                  isSelected ? 'bg-black/20 dark:bg-black/40' : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400'
                }`}
              >
                {count}
              </span>
            </button>
          );
        })}

        {onOpenCreateBucket && (
          <button
            type="button"
            onClick={onOpenCreateBucket}
            className="px-3 py-2 rounded-xl text-xs font-bold whitespace-nowrap bg-[#08dac9]/10 text-[#08dac9] hover:bg-[#08dac9]/20 border border-dashed border-[#08dac9]/40 flex items-center gap-1.5 transition-colors shrink-0"
            title="Create a new relationship group bucket"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>New Group</span>
          </button>
        )}
      </div>

      {/* Phrases Feed */}
      <div className="space-y-2.5">
        {bucketPhrases.length === 0 ? (
          <div className="p-8 text-center bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl shadow-sm">
            <p className="text-sm font-semibold text-slate-800 dark:text-white">
              No Phrases in this Pool
            </p>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
              Add at least one static message for {currentMeta.displayName} contacts.
            </p>
          </div>
        ) : (
          bucketPhrases.map((phrase) => (
            <div
              key={phrase.id}
              className="p-4 bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl shadow-sm space-y-2 group hover:border-slate-300 dark:hover:border-slate-700 transition-colors"
            >
              <p className="text-xs text-slate-800 dark:text-slate-200 italic leading-relaxed">
                "{phrase.text}"
              </p>

              <div className="flex items-center justify-between pt-1 border-t border-slate-100 dark:border-slate-800/80 text-[11px] text-slate-400 dark:text-slate-500">
                <span>{phrase.isSeed ? 'Preloaded Static Phrase' : 'Custom Phrase'}</span>
                <div className="flex items-center gap-1">
                  <button
                    onClick={() => onEditPhrase(phrase)}
                    className="p-1 rounded text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
                    title="Edit phrase (cascades to pending messages)"
                  >
                    <Edit2 className="w-3.5 h-3.5" />
                  </button>
                  <button
                    onClick={() => setPhraseToDelete(phrase)}
                    className="p-1 rounded text-rose-500 dark:text-rose-400 hover:text-rose-600 dark:hover:text-rose-300 hover:bg-rose-50 dark:hover:bg-rose-500/10 transition-colors"
                    title="Delete phrase (triggers automatic reroll)"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Add Phrase Button */}
      <button
        onClick={() => onAddNewPhrase(selectedBucket)}
        className="w-full py-3 bg-[#08dac9] hover:bg-[#07c2b3] text-[#04201d] font-bold text-sm rounded-2xl flex items-center justify-center gap-2 shadow-lg shadow-[#08dac9]/20 transition-all active:scale-[0.99]"
      >
        <Plus className="w-4 h-4" />
        <span>Add Phrase to {currentMeta.displayName} Pool</span>
      </button>

      {/* Delete Phrase Confirmation Pop-Up Modal */}
      {phraseToDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
          <div className="w-full max-w-sm bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-2xl space-y-4">
            <div className="flex items-start justify-between">
              <div className="flex items-center gap-2 text-rose-600 dark:text-rose-400 font-bold text-base">
                <Trash2 className="w-5 h-5" />
                <h3>Delete Phrase?</h3>
              </div>
              <button
                onClick={() => setPhraseToDelete(null)}
                className="p-1 rounded-full text-slate-400 hover:text-slate-600 dark:hover:text-white"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <p className="text-xs text-slate-600 dark:text-slate-300 italic p-3 bg-slate-50 dark:bg-slate-900/80 rounded-xl border border-slate-200 dark:border-slate-800">
              "{phraseToDelete.text}"
            </p>

            <p className="text-xs text-slate-500 dark:text-slate-400 leading-relaxed">
              This phrase will be removed from the <strong>{GROUP_BUCKETS[phraseToDelete.bucket]?.displayName || phraseToDelete.bucket}</strong> pool. If any upcoming pending messages currently have this phrase locked in, they will automatically re-roll to a different phrase.
            </p>

            <div className="flex gap-2 justify-end pt-2">
              <button
                type="button"
                onClick={() => setPhraseToDelete(null)}
                className="px-4 py-2 text-xs font-semibold text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={() => {
                  onDeletePhrase(phraseToDelete.id);
                  setPhraseToDelete(null);
                }}
                className="px-4 py-2 text-xs font-bold bg-rose-600 hover:bg-rose-500 text-white rounded-xl transition-colors shadow-sm"
              >
                Delete Phrase
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
