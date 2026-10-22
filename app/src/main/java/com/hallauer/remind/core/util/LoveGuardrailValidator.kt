package com.hallauer.remind.core.util

import com.hallauer.remind.data.local.entity.GroupBucket

object LoveGuardrailValidator {
    // Matches "love", "luv", "loving", "loved", "loves", "lover" case-insensitively
    private val LOVE_REGEX = Regex("""(?i)\b(love[a-z]*|luv[a-z]*)\b|(?i)(love|luv)""")

    fun validate(bucket: GroupBucket, text: String): ValidationResult {
        if (text.isBlank()) {
            return ValidationResult.Invalid("Message text cannot be empty.")
        }
        if (!bucket.allowsLoveWord && LOVE_REGEX.containsMatchIn(text)) {
            return ValidationResult.Invalid(
                "Security Guardrail: Phrases in the ${bucket.displayName} bucket can never contain the word 'love' or 'luv'."
            )
        }
        return ValidationResult.Valid
    }

    sealed class ValidationResult {
        data object Valid : ValidationResult()
        data class Invalid(val errorMessage: String) : ValidationResult()
    }
}
