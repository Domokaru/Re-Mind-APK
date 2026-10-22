package com.hallauer.remind.core.util

import android.telephony.PhoneNumberUtils

object PhoneNumberNormalizer {
    /**
     * Strips whitespace, dashes, parens, and standardizes to E.164 where possible.
     */
    fun normalize(rawNumber: String, defaultCountryIso: String = "US"): String {
        val clean = rawNumber.trim().replace(Regex("[^0-9+]"), "")
        val formatted = PhoneNumberUtils.formatNumberToE164(clean, defaultCountryIso)
        return formatted ?: clean
    }

    /**
     * Compares two phone numbers loosely using PhoneNumberUtils.compare
     * to safely match local 10-digit dials against stored +1 E.164 strings.
     */
    fun areEqual(number1: String?, number2: String?): Boolean {
        if (number1.isNullOrBlank() || number2.isNullOrBlank()) return false
        return PhoneNumberUtils.compare(number1, number2)
    }
}
