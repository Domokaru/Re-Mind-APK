package com.hallauer.remind.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Standard SMS_DELIVER receiver required by Android platform specifications
 * for Default SMS Handler eligibility.
 */
class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // System SMS delivery receiver for Default SMS App compliance
    }
}
