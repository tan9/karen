package com.example.tang.myapplication.service

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.core.Json
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ChatroomService {

    private var pool: ExecutorService = Executors.newFixedThreadPool(2)

    fun createSession(hostId: String, visitorId: String): String {
        var lock = CountDownLatch(1)

        var request: Request? = null
        var response: Response? = null
        var data: Json? = null
        var error: FuelError? = null

        pool.submit({
            val (req, res, result) =
                    Fuel.post("https://cht.services/chat/api/sessions")
                            .header("Content-Type" to "application/json")
                            .body(JSONObject(listOf("host" to hostId, "visitor" to visitorId).toMap()).toString())
                            .responseJson()

            val (d, e) = result
            data = d
            error = e

            request = req
            Log.d("chatroom", request.toString())
            response = res
            Log.d("chatroom", response.toString())

            lock.countDown()
        })

        lock.await(10, TimeUnit.SECONDS)

        if (error != null) {
            // FIXME 媽的，醜到爆...真 Kotlin 或是 Fuel 到底該怎麼寫??
            var message: String? = null
            if (data != null && data?.obj() != null) {
                message = data!!.obj()["detail"] as String
            }
            if (message == null && response != null) {
                try {
                    if (response!!.data != null) {
                        val json = Json(response!!.data.toString(Charsets.UTF_8)).obj()
                        message = json.optString("detail", json.optString("title", "Failed to create session.")) as String
                    }
                } catch (e: JSONException) {
                    // no valid response data, fall back to following message composition logic
                }
            }
            if (message == null) {
                message = error!!.message
            }
            throw RuntimeException(message, error)

        } else {
            if (data != null && data!!.obj() != null) {
                if (data!!.obj() != null) {
                    return data!!.obj()["sessionId"] as String
                } else {
                    throw RuntimeException("No response from chat room service.")
                }

            } else {
                throw RuntimeException("Failed to contact chat room service.")
            }
        }
    }
}
