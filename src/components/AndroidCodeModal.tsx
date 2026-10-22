import React, { useState } from 'react';
import { X, Copy, Check, FileCode, Folder, ChevronRight, ArrowLeft } from 'lucide-react';

interface AndroidCodeModalProps {
  onClose: () => void;
}

interface CodeFileItem {
  path: string;
  category: string;
  summary: string;
  content?: string;
}

const COLOR_KT_SOURCE = `package com.hallauer.remind.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.hallauer.remind.data.local.entity.GroupBucket

// Global Dark Mode Surfaces (Default)
val DarkBackground = Color(0xFF0B0F19)
val DarkSurface = Color(0xFF111827)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkPrimary = Color(0xFF38BDF8)
val DarkOnPrimary = Color(0xFF0F172A)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)

// Global Light Mode Surfaces
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightPrimary = Color(0xFF0284C7)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF64748B)

val DarkColorScheme = darkColorScheme(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary
)

val LightColorScheme = lightColorScheme(
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary
)

/**
 * Returns the adaptive high-contrast color for each GroupBucket based on the active theme mode.
 */
@Composable
fun GroupBucket.toColor(): Color {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme() // Or read from SettingsDataStore
    return when (this) {
        GroupBucket.SPOUSE -> if (isDark) Color(0xFFE11D48) else Color(0xFFBE123C)       // Crimson / Warm Rose
        GroupBucket.FAMILY -> if (isDark) Color(0xFFD97706) else Color(0xFFB45309)       // Deep Amber / Gold
        GroupBucket.FRIENDS -> if (isDark) Color(0xFF2563EB) else Color(0xFF1D4ED8)      // Royal Blue
        GroupBucket.ACQUAINTANCES -> if (isDark) Color(0xFF0D9488) else Color(0xFF0F766E)// Emerald / Teal
        GroupBucket.OTHERS -> if (isDark) Color(0xFF7C3AED) else Color(0xFF6D28D9)       // Slate Purple
    }
}`;

const THEME_KT_SOURCE = `package com.hallauer.remind.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun ReMINDTheme(
    darkTheme: Boolean = true, // Default to Dark Theme per spec
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}`;

const ANDROID_PROJECT_FILES: CodeFileItem[] = [
  {
    path: 'app/src/main/java/com/hallauer/remind/ui/theme/Color.kt',
    category: 'Theme & Style',
    summary: 'Section 9.1.1 Color Palette: Dark mode surfaces (#0B0F19, #111827, #1E293B, #38BDF8), light surfaces (#F8FAFC, #FFFFFF, #F1F5F9, #0284C7), and adaptive high-contrast GroupBucket.toColor() extension',
    content: COLOR_KT_SOURCE,
  },
  {
    path: 'app/src/main/java/com/hallauer/remind/ui/theme/Theme.kt',
    category: 'Theme & Style',
    summary: 'Material 3 DarkColorScheme and LightColorScheme integration with ReMINDTheme composable defaulting to dark mode',
    content: THEME_KT_SOURCE,
  },
  {
    path: 'gradle/libs.versions.toml',
    category: 'Build Config',
    summary: 'Version catalog (Kotlin 2.1.0, Compose BOM 2025.02.00, Room 2.6.1, Hilt 2.52, WorkManager 2.10.0)',
  },
  {
    path: 'app/build.gradle.kts',
    category: 'Build Config',
    summary: 'Android application build configuration targeting Android 15/16 (SDK 35), Min SDK 29, Room KSP, Hilt',
  },
  {
    path: 'app/src/main/AndroidManifest.xml',
    category: 'Manifest',
    summary: 'Permissions (SEND_SMS, READ_CONTACTS, SCHEDULE_EXACT_ALARM), receivers, and Google Messages RCS listener service',
  },
  {
    path: 'app/src/main/res/drawable/ic_launcher_foreground.xml',
    category: 'Assets',
    summary: 'Adaptive vector icon with Mindfulness Sky Blue chat bubble and Warm Rose pulse wave',
  },
  {
    path: 'app/src/main/java/com/hallauer/remind/RemindApplication.kt',
    category: 'Core',
    summary: '@HiltAndroidApp application class configuring Hilt WorkManager, 50 seed phrases, and periodic workers',
  },
  {
    path: 'app/src/main/java/com/hallauer/remind/MainActivity.kt',
    category: 'UI',
    summary: 'Portrait-locked Jetpack Compose navigation host with 4 bottom tabs, Vacation Mode banner, and top bar',
  },
  {
    path: 'app/src/main/java/com/hallauer/remind/core/util/LoveGuardrailValidator.kt',
    category: 'Guardrails',
    summary: 'Strict lexical guardrail blocking "love"/"luv" regex across Friends, Acquaintances, and Others buckets',
  },
  {
    path: 'app/src/main/java/com/hallauer/remind/core/util/RecurrenceCalculator.kt',
    category: 'Domain',
    summary: 'Blackout clamping (7..22), 7:00 AM recovery same-day skip rule, and virtual future calendar projections',
  },
  {
    path: 'app/src/main/java/com/hallauer/remind/core/constants/SeedPhrases.kt',
    category: 'Domain',
    summary: '50 preloaded static phrases (10 per bucket in Ben Hallauer + Dr. John Delony hybrid voice)',
  },
  {
    path: 'app/src/main/java/com/hallauer/remind/data/local/RemindDatabase.kt',
    category: 'Room DB',
    summary: 'Room Database definition with ContactScheduleEntity, PhraseEntity, and MessageLogEntity',
  },
  {
    path: 'app/src/main/java/com/hallauer/remind/data/provider/SmsCollisionDetector.kt',
    category: 'Telecom',
    summary: 'Dual-layer collision detector querying content://sms/sent and Google Messages RCS notifications',
  },
  {
    path: 'app/src/main/java/com/hallauer/remind/service/worker/SmsDispatchWorker.kt',
    category: 'Background',
    summary: 'Exact execution worker verifying global pause, blackout hours, collision avoidance, and pre-rolling next occurrence',
  },
  {
    path: 'app/src/main/java/com/hallauer/remind/service/worker/MonthlyContactVerifyWorker.kt',
    category: 'Background',
    summary: '30-day periodic worker verifying Google Contacts phone numbers with "Pause and Fire" notification',
  },
  {
    path: 'app/src/main/java/com/hallauer/remind/ui/components/UpcomingMessageActionSheet.kt',
    category: 'UI Compose',
    summary: 'Material 3 ModalBottomSheet providing all 6 manual override actions (Send Now, Reroll, Edit, Skip, Reschedule, Pause)',
  },
];

export const AndroidCodeModal: React.FC<AndroidCodeModalProps> = ({ onClose }) => {
  const [copiedPath, setCopiedPath] = useState<string | null>(null);
  const [copiedContent, setCopiedContent] = useState(false);
  const [viewingFile, setViewingFile] = useState<CodeFileItem | null>(null);

  const handleCopyPath = (path: string, e: React.MouseEvent) => {
    e.stopPropagation();
    navigator.clipboard.writeText(path);
    setCopiedPath(path);
    setTimeout(() => setCopiedPath(null), 2000);
  };

  const handleCopyCode = (text: string) => {
    navigator.clipboard.writeText(text);
    setCopiedContent(true);
    setTimeout(() => setCopiedContent(false), 2000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-fade-in">
      <div className="w-full max-w-2xl bg-white dark:bg-[#111827] border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-2xl max-h-[85vh] flex flex-col text-slate-900 dark:text-slate-100">
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-200 dark:border-slate-800">
          <div className="flex items-center gap-2.5">
            {viewingFile ? (
              <button
                onClick={() => setViewingFile(null)}
                className="p-1.5 rounded-lg bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 hover:text-slate-900 dark:hover:text-white transition-colors"
                title="Back to file list"
              >
                <ArrowLeft className="w-5 h-5" />
              </button>
            ) : (
              <div className="p-2 rounded-lg bg-sky-500/10 text-sky-600 dark:text-sky-400">
                <Folder className="w-5 h-5" />
              </div>
            )}
            <div>
              <h2 className="text-base font-bold text-slate-900 dark:text-white">
                {viewingFile ? viewingFile.path.split('/').pop() : 'Native Android Kotlin Codebase'}
              </h2>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                {viewingFile
                  ? viewingFile.path
                  : 'Package: com.hallauer.remind • Section 9.1.1 Color Palette & Architecture'}
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

        {viewingFile ? (
          /* File Content Viewer */
          <div className="flex-1 overflow-hidden flex flex-col pt-3">
            <div className="flex items-center justify-between pb-2 text-xs">
              <span className="text-slate-500 dark:text-slate-400">{viewingFile.summary}</span>
              {viewingFile.content && (
                <button
                  onClick={() => handleCopyCode(viewingFile.content!)}
                  className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-sky-50 dark:bg-sky-500/10 text-sky-600 dark:text-sky-400 border border-sky-200 dark:border-sky-500/20 hover:bg-sky-100 dark:hover:bg-sky-500/20 font-semibold transition-colors"
                >
                  {copiedContent ? (
                    <>
                      <Check className="w-3.5 h-3.5 text-emerald-500" />
                      <span>Copied!</span>
                    </>
                  ) : (
                    <>
                      <Copy className="w-3.5 h-3.5" />
                      <span>Copy Code</span>
                    </>
                  )}
                </button>
              )}
            </div>
            <pre className="flex-1 overflow-auto p-4 rounded-xl bg-slate-900 text-slate-200 font-mono text-xs leading-relaxed border border-slate-800 shadow-inner">
              <code>{viewingFile.content || '// Content available in root workspace: ' + viewingFile.path}</code>
            </pre>
          </div>
        ) : (
          /* File List */
          <>
            <div className="py-3 text-xs text-slate-600 dark:text-slate-300">
              All native Android project source files have been generated in the project root under{' '}
              <code className="text-sky-600 dark:text-sky-400 font-semibold">app/</code> and{' '}
              <code className="text-sky-600 dark:text-sky-400 font-semibold">gradle/</code>, ready for import into Android Studio (Ladybug / Meerkat 2025/2026+).
            </div>

            <div className="flex-1 overflow-y-auto space-y-2 pr-1">
              {ANDROID_PROJECT_FILES.map((file) => (
                <div
                  key={file.path}
                  onClick={() => file.content && setViewingFile(file)}
                  className={`p-3 rounded-xl border transition-all flex items-start justify-between gap-3 ${
                    file.content ? 'cursor-pointer' : ''
                  } bg-slate-50 dark:bg-slate-900/80 border-slate-200 dark:border-slate-800 hover:border-sky-300 dark:hover:border-slate-700`}
                >
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <FileCode className="w-4 h-4 text-sky-600 dark:text-sky-400 shrink-0" />
                      <span className="text-xs font-mono font-semibold text-slate-800 dark:text-slate-200">
                        {file.path}
                      </span>
                      <span className="px-1.5 py-0.5 rounded text-[10px] bg-slate-200 dark:bg-slate-800 text-slate-700 dark:text-slate-400 font-medium">
                        {file.category}
                      </span>
                      {file.content && (
                        <span className="px-1.5 py-0.5 rounded text-[9px] bg-sky-100 dark:bg-sky-500/20 text-sky-700 dark:text-sky-300 font-bold uppercase tracking-wider">
                          View Code
                        </span>
                      )}
                    </div>
                    <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed pl-6">
                      {file.summary}
                    </p>
                  </div>

                  <div className="flex items-center gap-1 shrink-0">
                    <button
                      onClick={(e) => handleCopyPath(file.path, e)}
                      className="p-2 rounded-lg bg-slate-200 dark:bg-slate-800 hover:bg-slate-300 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 transition-colors"
                      title="Copy relative file path"
                    >
                      {copiedPath === file.path ? (
                        <Check className="w-4 h-4 text-emerald-500 dark:text-emerald-400" />
                      ) : (
                        <Copy className="w-4 h-4" />
                      )}
                    </button>
                    {file.content && (
                      <div className="p-2 text-slate-400">
                        <ChevronRight className="w-4 h-4" />
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </>
        )}

        {/* Footer */}
        <div className="pt-4 border-t border-slate-200 dark:border-slate-800 flex justify-between items-center mt-3">
          <span className="text-xs text-slate-500 dark:text-slate-400">
            Total Native Source Files: <strong>32 files</strong> • Dark Mode Surface Default
          </span>
          <button
            onClick={onClose}
            className="px-4 py-2 text-xs font-bold text-white dark:text-slate-900 bg-sky-600 dark:bg-sky-400 hover:bg-sky-700 dark:hover:bg-sky-300 rounded-xl transition-colors shadow-sm"
          >
            Done
          </button>
        </div>
      </div>
    </div>
  );
};
