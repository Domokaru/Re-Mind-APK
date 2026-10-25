package com.hallauer.remind.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Standard WAP_PUSH_DELIVER receiver required by Android platform specifications
 * for Default SMS Handler eligibility.
 */
class MmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // System MMS push receiver for Default SMS App compliance
    }
}
