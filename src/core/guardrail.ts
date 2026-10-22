import { GROUP_BUCKETS, GroupBucket } from '../types/remind';

export interface GuardrailValidationResult {
  isValid: boolean;
  errorMessage?: string;
}

// Regex matching any word containing "love" or "luv", case-insensitively
const LOVE_REGEX = /(?:\b(love[a-z]*|luv[a-z]*)\b|(love|luv))/i;

export const validateLoveGuardrail = (
  bucket: GroupBucket,
  text: string
): GuardrailValidationResult => {
  if (!text || !text.trim()) {
    return {
      isValid: false,
      errorMessage: 'Message text cannot be empty.',
    };
  }

  const meta = GROUP_BUCKETS[bucket];
  if (meta && !meta.allowsLoveWord && LOVE_REGEX.test(text)) {
    return {
      isValid: false,
      errorMessage: `Strict Security Guardrail: Phrases in the ${meta.displayName} bucket can never contain the word 'love' or 'luv'.`,
    };
  }

  return { isValid: true };
};
