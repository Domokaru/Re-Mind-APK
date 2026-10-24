import React, { useState } from 'react';
import { X, ShieldCheck, AlertTriangle, Sparkles } from 'lucide-react';
import { validateLoveGuardrail } from '../core/guardrail';

interface ColorPreset {
  label: string;
  dark: string;
  light: string;
}

const COLOR_PRESETS: ColorPreset[] = [
  { label: 'Emerald', dark: '#10B981', light: '#059669' },
  { label: 'Indigo', dark: '#6366F1', light: '#4F46E5' },
  { label: 'Violet', dark: '#8B5CF6', light: '#7C3AED' },
  { label: 'Fuchsia', dark: '#D946EF', light: '#C026D3' },
  { label: 'Rose', dark: '#F43F5E', light: '#E11D48' },
  { label: 'Amber', dark: '#F59E0B', light: '#D97706' },
  { label: 'Orange', dark: '#F97316', light: '#EA580C' },
  { label: 'Cyan', dark: '#06B6D4', light: '#0891B2' },
  { label: 'Sky', dark: '#38BDF8', light: '#0284C7' },
  { label: 'Lime', dark: '#84CC16', light: '#65A30D' },
];

interface CreateBucketModalProps {
  onClose: () => void;
  onSave: (bucket: {
    displayName: string;
    allowsLoveWord: boolean;
    darkColorHex: string;
    lightColorHex: string;
    initialPhrase?: string;
  }) => void;
}

export const CreateBucketModal: React.FC<CreateBucketModalProps> = ({
  onClose,
  onSave,
}) => {
  const [displayName, setDisplayName] = useState('');
  const [allowsLoveWord, setAllowsLoveWord] = useState(false);
  const [selectedColor, setSelectedColor] = useState<ColorPreset>(COLOR_PRESETS[0]);
  const [initialPhrase, setInitialPhrase] = useState('');
  const [phraseError, setPhraseError] = useState<string | null>(null);

  const handlePhraseChange = (val: string, allowsLove: boolean) => {
    setInitialPhrase(val);
    if (!val.trim()) {
      setPhraseError(null);
      return;
    }
    if (!allowsLove && /(?:\b(love[a-z]*|luv[a-z]*)\b|(love|luv))/i.test(val)) {
      setPhraseError("Phrases in this group cannot contain the word 'love' or 'luv' because the guardrail is enabled.");
    } else {
      setPhraseError(null);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!displayName.trim() || phraseError) return;

    onSave({
      displayName: displayName.trim(),
      allowsLoveWord,
      darkColorHex: selectedColor.dark,
      lightColorHex: selectedColor.light,
      initialPhrase: initialPhrase.trim() || undefined,
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
      <div className="w-full max-w-md bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-2xl max-h-[90vh] overflow-y-auto space-y-5">
        <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
          <div className="flex items-center gap-2">
            <div
              className="w-3.5 h-3.5 rounded-full shadow-sm"
              style={{ backgroundColor: selectedColor.dark }}
            />
            <h2 className="text-base font-bold text-slate-900 dark:text-white">
              Create Relationship Group
            </h2>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-full text-slate-400 hover:text-slate-600 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Group Name */}
          <div>
            <label className="text-xs font-semibold text-slate-700 dark:text-slate-300 block mb-1">
              Group Display Name
            </label>
            <input
              type="text"
              required
              value={displayName}
              onChange={(e) => setDisplayName(e.target.value)}
              placeholder="e.g. Colleagues, Mentors, Extended Family"
              className="w-full p-2.5 bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-xl text-sm text-slate-900 dark:text-white focus:outline-none focus:border-sky-500"
            />
          </div>

          {/* Color Preset Selector */}
          <div>
            <label className="text-xs font-semibold text-slate-700 dark:text-slate-300 block mb-2">
              Group Accent Color
            </label>
            <div className="grid grid-cols-5 gap-2">
              {COLOR_PRESETS.map((preset) => {
                const isSelected = selectedColor.label === preset.label;
                return (
                  <button
                    key={preset.label}
                    type="button"
                    onClick={() => setSelectedColor(preset)}
                    className={`p-2 rounded-xl border flex flex-col items-center gap-1 transition-all ${
                      isSelected
                        ? 'ring-2 ring-sky-500 border-sky-500 dark:border-sky-400 shadow-sm'
                        : 'border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-900'
                    }`}
                  >
                    <span
                      className="w-4 h-4 rounded-full shadow-sm"
                      style={{ backgroundColor: preset.dark }}
                    />
                    <span className="text-[10px] font-bold text-slate-700 dark:text-slate-300">
                      {preset.label}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Lexical Guardrail Policy */}
          <div>
            <label className="text-xs font-semibold text-slate-700 dark:text-slate-300 block mb-2">
              Lexical "No-Love" Guardrail Policy
            </label>
            <div className="space-y-2">
              <label
                className={`p-3 rounded-xl border flex items-start gap-3 cursor-pointer transition-colors ${
                  !allowsLoveWord
                    ? 'bg-sky-50/50 dark:bg-sky-500/10 border-sky-400 dark:border-sky-500/40 text-slate-900 dark:text-white'
                    : 'border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-900 text-slate-700 dark:text-slate-300'
                }`}
              >
                <input
                  type="radio"
                  name="guardrail"
                  checked={!allowsLoveWord}
                  onChange={() => {
                    setAllowsLoveWord(false);
                    handlePhraseChange(initialPhrase, false);
                  }}
                  className="mt-0.5 text-sky-500"
                />
                <div className="text-xs">
                  <div className="font-bold flex items-center gap-1.5">
                    <ShieldCheck className="w-3.5 h-3.5 text-emerald-500" />
                    <span>Enforce Guardrail (Recommended)</span>
                  </div>
                  <p className="text-[11px] text-slate-500 dark:text-slate-400 mt-0.5">
                    Blocks words containing 'love' or 'luv'. Best for colleagues, acquaintances, and casual contacts.
                  </p>
                </div>
              </label>

              <label
                className={`p-3 rounded-xl border flex items-start gap-3 cursor-pointer transition-colors ${
                  allowsLoveWord
                    ? 'bg-sky-50/50 dark:bg-sky-500/10 border-sky-400 dark:border-sky-500/40 text-slate-900 dark:text-white'
                    : 'border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-900 text-slate-700 dark:text-slate-300'
                }`}
              >
                <input
                  type="radio"
                  name="guardrail"
                  checked={allowsLoveWord}
                  onChange={() => {
                    setAllowsLoveWord(true);
                    handlePhraseChange(initialPhrase, true);
                  }}
                  className="mt-0.5 text-sky-500"
                />
                <div className="text-xs">
                  <div className="font-bold">Allow Affectionate Terms</div>
                  <p className="text-[11px] text-slate-500 dark:text-slate-400 mt-0.5">
                    Allows 'love' and 'luv'. Appropriate for romantic partners and immediate family members.
                  </p>
                </div>
              </label>
            </div>
          </div>

          {/* Optional Initial Phrase */}
          <div>
            <div className="flex items-center justify-between mb-1">
              <label className="text-xs font-semibold text-slate-700 dark:text-slate-300">
                Initial Starting Phrase (Optional)
              </label>
              <span className="text-[10px] text-slate-400">Can add more anytime</span>
            </div>
            <textarea
              rows={2}
              value={initialPhrase}
              onChange={(e) => handlePhraseChange(e.target.value, allowsLoveWord)}
              placeholder="e.g. Hope your week is going great! Thinking of you."
              className="w-full p-2.5 bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-xl text-xs text-slate-900 dark:text-white focus:outline-none focus:border-[#08dac9] resize-none"
            />
            {phraseError && (
              <div className="flex items-center gap-1.5 text-xs text-rose-500 mt-1">
                <AlertTriangle className="w-3.5 h-3.5 shrink-0" />
                <span>{phraseError}</span>
              </div>
            )}
          </div>

          {/* Actions */}
          <div className="pt-3 border-t border-slate-100 dark:border-slate-800 flex items-center justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-semibold text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!displayName.trim() || !!phraseError}
              className="px-5 py-2 text-xs font-bold text-[#04201d] bg-[#08dac9] hover:bg-[#07c2b3] rounded-xl transition-colors disabled:opacity-50 disabled:cursor-not-allowed shadow-sm"
            >
              Create Group
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
