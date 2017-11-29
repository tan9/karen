package com.example.tang.myapplication

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import com.android.internal.telephony.ITelephony
import com.example.tang.myapplication.service.ChatroomService
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
                    var processLogMessage = try {
                        val chatroomService = ChatroomService()
                        val sessionId = chatroomService.createSession(getHostId(context), number)

                        // 網址會超過簡訊長度，目前只好先分兩則送了
                        sendSms(number, "抱歉，現在無法接聽您的電話，我是虛擬助理小米，有任何問題都可以找我喔。請到:", context)
                        sendSms(number, "https://room.cht.services/$sessionId", context)

                        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)
                        "\uD83D\uDC67\uD83C\uDFFB " + number + " @ " + dateFormat.format(Date())

                    } catch (e: Exception) {
                        Log.e(_TAG, "Failed to handle call.", e)
                        "Error: ${e.message}"
                    }

                    val sharedPreferences = context.getSharedPreferences("handled_incoming_calls", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString(System.currentTimeMillis().toString(), processLogMessage)
                    editor.apply()

                    endCall(telephonyManager)
                }
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
            }
            TelephonyManager.CALL_STATE_IDLE -> {
            }
        }
    }

    private fun getHostId(context: Context): String {
        val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var hostId = manager.line1Number
        if (hostId == null) {
            hostId = manager.subscriberId
        }
        return hostId
    }

    private fun shouldProcess(incomingNumber: String?): Boolean =
            true

    private fun sendSms(phoneNo: String, message: String, context: Context) {
        try {
            val smsManager = SmsManager.getDefault()

            val pendingIntent = PendingIntent.getBroadcast(context, 0, Intent("SMS_SENT"), 0)

            smsManager.sendTextMessage(phoneNo, null, message, pendingIntent, null)
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
