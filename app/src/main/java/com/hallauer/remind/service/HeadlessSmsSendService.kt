package com.hallauer.remind.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Headless SMS service responding to RESPOND_VIA_MESSAGE, required by Android
 * platform specifications for Default SMS Handler eligibility.
 */
class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
