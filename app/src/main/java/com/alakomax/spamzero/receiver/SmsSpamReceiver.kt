package com.alakomax.spamzero.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class SmsSpamReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("SmsSpamReceiver", "Evento SMS recibido por el sistema.")
    }
}
