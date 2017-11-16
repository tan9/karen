package com.example.tang.myapplication

import com.android.internal.telephony.ITelephony

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class PhoneStatReceiver : BroadcastReceiver() {

    private var _TAG = "karen"

    override fun onReceive(context: Context, intent: Intent) {
        val telephonyManager = context.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
        when (telephonyManager.callState) {
            TelephonyManager.CALL_STATE_RINGING -> {
                val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                Log.v(_TAG, "Incoming number: " + number)
                if (shouldProcess(number)) {
                    val sharedPreferences = context.getSharedPreferences("handled_incoming_calls", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()

                    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)
                    val message = "\uD83D\uDC67\uD83C\uDFFB " + number + " @ " + dateFormat.format(Date())
                    editor.putString(System.currentTimeMillis().toString(), message)
                    editor.apply()

                    sendSms(number, "沒有人能幫你喔，請連到 http://www.google.com/ 自已想辦法。", context)
                    endCall(telephonyManager)
                }
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
            }
            TelephonyManager.CALL_STATE_IDLE -> {
            }
        }

    }

    private fun shouldProcess(incomingNumber: String?): Boolean =
            true

    private fun sendSms(phoneNo: String, msg: String, context: Context) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNo, null, msg, null, null)
            Toast.makeText(context, "Message Sent.", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Log.e(_TAG, "Fail to send SMS message.", e)
        }
    }

    private fun endCall(telephonyManager: TelephonyManager) {
        val c = TelephonyManager::class.java
        try {
            val iTelephonyGetter = c.getDeclaredMethod("getITelephony")
            iTelephonyGetter.isAccessible = true

            val iTelephony: ITelephony = iTelephonyGetter.invoke(telephonyManager) as ITelephony
            iTelephony.endCall()
            Log.e(_TAG, "Call ended successfully.")

        } catch (e: Exception) {
            Log.e(_TAG, "Fail to end incoming call.", e)
        }
    }
}