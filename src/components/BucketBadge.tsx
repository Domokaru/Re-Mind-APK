import React from 'react';
import { GROUP_BUCKETS, GroupBucket } from '../types/remind';

interface BucketBadgeProps {
  bucket: GroupBucket;
  className?: string;
}

const BUCKET_CSS_VARS: Record<string, string> = {
  SPOUSE: 'var(--bucket-spouse)',
  FAMILY: 'var(--bucket-family)',
  FRIENDS: 'var(--bucket-friends)',
  ACQUAINTANCES: 'var(--bucket-acquaintances)',
  OTHERS: 'var(--bucket-others)',
};

export const BucketBadge: React.FC<BucketBadgeProps> = ({ bucket, className = '' }) => {
  const meta = GROUP_BUCKETS[bucket];
  if (!meta) {
    return (
      <span
        className={`inline-flex items-center px-2 py-0.5 rounded text-[11px] font-semibold tracking-tight transition-colors border bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 border-slate-300 dark:border-slate-700 ${className}`}
      >
        {bucket}
      </span>
    );
  }

  const colorVal = BUCKET_CSS_VARS[bucket] || meta.darkColorHex;

  return (
    <span
      className={`inline-flex items-center px-2 py-0.5 rounded text-[11px] font-semibold tracking-tight transition-colors border ${className}`}
      style={{
        backgroundColor: `color-mix(in srgb, ${colorVal} 15%, transparent)`,
        color: colorVal,
        borderColor: `color-mix(in srgb, ${colorVal} 30%, transparent)`,
      }}
    >
      {meta.displayName}
    </span>
  );
};
