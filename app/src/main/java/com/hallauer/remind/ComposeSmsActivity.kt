package com.hallauer.remind

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Standard compose SMS activity required by Android platform specifications
 * for Default SMS Handler eligibility.
 */
class ComposeSmsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val target = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data = intent.data
            intent.extras?.let { putExtras(it) }
        }
        startActivity(target)
        finish()
    }
}
