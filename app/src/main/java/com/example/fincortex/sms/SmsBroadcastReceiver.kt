package com.example.fincortex.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.fincortex.repository.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Listens for incoming SMS messages and auto-saves bank transactions to Firestore.
 * Registered in AndroidManifest.xml with RECEIVE_SMS permission.
 */
class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

        for (smsMessage in messages) {
            val body = smsMessage.messageBody ?: continue
            val timestamp = smsMessage.timestampMillis

            val transaction = SmsReader.parseSms(body, timestamp) ?: continue

            // Save to Firestore in background coroutine
            CoroutineScope(Dispatchers.IO).launch {
                TransactionRepository().addTransaction(transaction.copy(userId = userId))
            }
        }
    }
}
