import {
  Sun,
  Moon,
  Download,
  Upload,
  RotateCcw,
  Quote,
  ChevronRight,
  Plus,
} from 'lucide-react';
import { GROUP_BUCKETS, BucketMetadata } from '../types/remind';

interface SettingsViewProps {
  themeMode: 'DARK' | 'LIGHT';
  isGlobalPauseEnabled: boolean;
  phraseCount?: number;
  buckets?: BucketMetadata[];
  onToggleTheme: (theme: 'DARK' | 'LIGHT') => void;
  onToggleGlobalPause: (paused: boolean) => void;
  onOpenPhrases: () => void;
  onOpenAndroidCode?: () => void;
  onOpenTestLab?: () => void;
  onOpenCreateBucket?: () => void;
  onResetFactory: () => void;
}

export const SettingsView: React.FC<SettingsViewProps> = ({
  themeMode,
  isGlobalPauseEnabled,
  phraseCount = 50,
  buckets,
  onToggleTheme,
  onToggleGlobalPause,
  onOpenPhrases,
  onOpenTestLab,
  onOpenCreateBucket,
  onResetFactory,
}) => {
  const handleExportJson = () => {
    const raw = localStorage.getItem('remind_app_state_v1');
    if (!raw) return;
    const blob = new Blob([raw], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `remind_backup_${new Date().toISOString().split('T')[0]}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const handleImportJson = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const text = event.target?.result as string;
        JSON.parse(text); // validate
        localStorage.setItem('remind_app_state_v1', text);
        alert('Backup successfully imported! Reloading...');
        window.location.reload();
      } catch {
        alert('Failed to parse backup JSON file.');
      }
    };
    reader.readAsText(file);
  };

  return (
    <div className="space-y-4">
      {/* Brand Header with App Icon */}
      <div className="flex items-center gap-3.5 p-4 bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl shadow-sm">
        <img
          src="/app-icon.jpeg"
          alt="Re:MIND Icon"
          className="w-12 h-12 rounded-2xl shadow-md border border-slate-200/60 dark:border-slate-700/60 object-cover shrink-0"
        />
        <div>
          <h2 className="text-base font-bold text-slate-900 dark:text-white tracking-tight">Re:MIND</h2>
          <p className="text-xs text-slate-500 dark:text-slate-400">Automated Mindfulness & Thoughtful SMS</p>
        </div>
      </div>

      {/* 1. Phrase Pools & Lexical Guardrails Launcher (Pushed from bottom nav) */}
      <div className="p-4 bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl shadow-sm space-y-3">
        <div className="flex items-center justify-between">
          <h3 className="text-xs font-bold uppercase tracking-wider text-[#08dac9] flex items-center gap-1.5">
            <Quote className="w-4 h-4" />
            <span>Phrase Pools</span>
          </h3>
        </div>

        <button
          onClick={onOpenPhrases}
          className="w-full p-3.5 bg-slate-50 hover:bg-slate-100 dark:bg-slate-900/80 dark:hover:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-800 hover:border-[#08dac9]/40 text-left flex items-center justify-between transition-colors group"
        >
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-lg bg-[#08dac9]/10 text-[#08dac9] group-hover:bg-[#08dac9]/20">
              <Quote className="w-4 h-4" />
            </div>
            <div>
              <div className="text-sm font-bold text-slate-900 dark:text-white group-hover:text-[#08dac9]">
                Manage Phrase Pools & Rules
              </div>
              <div className="text-xs text-slate-500 dark:text-slate-400">
                Add, edit, or customize rotation messages by group
              </div>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-slate-400 group-hover:text-[#08dac9] transition-colors" />
        </button>
      </div>

      {/* 2. Appearance Theme */}
      <div className="p-4 bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl space-y-3 shadow-sm">
        <h3 className="text-xs font-bold uppercase tracking-wider text-[#08dac9]">
          Appearance Theme
        </h3>
        <div className="grid grid-cols-2 gap-2">
          <button
            onClick={() => onToggleTheme('DARK')}
            className={`p-3 rounded-xl flex items-center justify-center gap-2 text-xs font-bold transition-all ${
              themeMode === 'DARK'
                ? 'bg-[#08dac9] text-[#04201d] font-bold shadow-sm'
                : 'bg-slate-100 dark:bg-slate-900 text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white border border-slate-200 dark:border-slate-800'
            }`}
          >
            <Moon className="w-4 h-4" />
            <span>Dark Mode (Default)</span>
          </button>
          <button
            onClick={() => onToggleTheme('LIGHT')}
            className={`p-3 rounded-xl flex items-center justify-center gap-2 text-xs font-bold transition-all ${
              themeMode === 'LIGHT'
                ? 'bg-[#08dac9] text-[#04201d] font-bold shadow-sm'
                : 'bg-slate-100 dark:bg-slate-900 text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white border border-slate-200 dark:border-slate-800'
            }`}
          >
            <Sun className="w-4 h-4" />
            <span>Light Mode</span>
          </button>
        </div>

        {/* Relationship Buckets Swatches */}
        <div className="pt-2.5 border-t border-slate-100 dark:border-slate-800/80 space-y-2">
          <div className="flex items-center justify-between text-[11px] font-semibold text-slate-500 dark:text-slate-400">
            <span>Relationship Buckets</span>
            {onOpenCreateBucket && (
              <button
                type="button"
                onClick={onOpenCreateBucket}
                className="text-[#08dac9] hover:underline flex items-center gap-1 font-bold text-[11px] transition-colors"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>New Group</span>
              </button>
            )}
          </div>
          <div className="flex flex-wrap gap-1.5">
            {(buckets && buckets.length > 0 ? buckets : (Object.values(GROUP_BUCKETS) as BucketMetadata[])).map((meta) => {
              const isDark = themeMode === 'DARK';
              const color = isDark ? meta.darkColorHex : meta.lightColorHex;
              const label = meta.id === 'ACQUAINTANCES' ? 'Acquain.' : meta.displayName;
              return (
                <div
                  key={meta.id}
                  className="px-2 py-1 rounded-lg border flex items-center gap-1.5 transition-colors"
                  style={{
                    backgroundColor: `color-mix(in srgb, ${color} 12%, transparent)`,
                    borderColor: `color-mix(in srgb, ${color} 30%, transparent)`,
                  }}
                >
                  <span
                    className="w-2.5 h-2.5 rounded-full shadow-sm shrink-0"
                    style={{ backgroundColor: color }}
                  />
                  <span
                    className="text-[10px] font-bold tracking-tight"
                    style={{ color }}
                  >
                    {label}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* 3. Global Automation Controls */}
      <div className="p-4 bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl space-y-3 shadow-sm">
        <h3 className="text-xs font-bold uppercase tracking-wider text-[#08dac9]">
          Automation Controls
        </h3>

        {/* Vacation Mode Toggle */}
        <div className="flex items-center justify-between p-3.5 bg-slate-50 dark:bg-slate-900/80 rounded-xl border border-slate-200 dark:border-slate-800">
          <div>
            <div className="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
              <span>Vacation Mode (Pause All)</span>
              {isGlobalPauseEnabled && (
                <span className="text-[10px] bg-amber-400/20 text-amber-600 dark:text-amber-400 px-1.5 py-0.5 rounded font-bold">
                  ACTIVE
                </span>
              )}
            </div>
            <div className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              Silently suspends all background automated SMS dispatches
            </div>
          </div>
          <button
            onClick={() => onToggleGlobalPause(!isGlobalPauseEnabled)}
            className={`w-11 h-6 rounded-full transition-colors relative p-0.5 shrink-0 ${
              isGlobalPauseEnabled
                ? 'bg-amber-500 dark:bg-amber-400'
                : 'bg-slate-300 dark:bg-slate-800 border border-slate-300 dark:border-slate-700'
            }`}
          >
            <span
              className={`w-5 h-5 rounded-full bg-white block transition-transform shadow ${
                isGlobalPauseEnabled ? 'translate-x-5' : 'translate-x-0'
              }`}
            />
          </button>
        </div>
      </div>

      {/* 4. Data Backup & Recovery */}
      <div className="p-4 bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl space-y-3 shadow-sm">
        <h3 className="text-xs font-bold uppercase tracking-wider text-[#08dac9]">
          Offline & Cloud Backup
        </h3>
        <p className="text-xs text-slate-500 dark:text-slate-400">
          Export your entire local Room database as JSON or restore your data.
        </p>
        <div className="grid grid-cols-2 gap-2">
          <button
            onClick={handleExportJson}
            className="p-2.5 bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-800 dark:text-white rounded-xl text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors"
          >
            <Download className="w-4 h-4 text-[#08dac9]" />
            <span>Export JSON</span>
          </button>
          <label className="p-2.5 bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-800 dark:text-white rounded-xl text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors cursor-pointer text-center">
            <Upload className="w-4 h-4 text-[#08dac9]" />
            <span>Import JSON</span>
            <input
              type="file"
              accept=".json"
              onChange={handleImportJson}
              className="hidden"
            />
          </label>
        </div>

        <div className="pt-2 border-t border-slate-100 dark:border-slate-800 flex items-center justify-end">
          <button
            onClick={() => {
              if (
                window.confirm(
                  'Reset all data back to original 50 seed phrases and sample contacts?'
                )
              ) {
                onResetFactory();
              }
            }}
            className="text-xs text-rose-600 dark:text-rose-400 hover:text-rose-700 dark:hover:text-rose-300 flex items-center gap-1 font-medium"
          >
            <RotateCcw className="w-3 h-3" />
            <span>Reset to Factory Seed</span>
          </button>
        </div>
      </div>
    </div>
  );
};
