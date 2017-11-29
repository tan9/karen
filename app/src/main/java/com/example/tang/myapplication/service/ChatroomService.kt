package com.example.tang.myapplication.service

import com.github.kittinunf.fuel.android.core.Json
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpPost
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

        pool.submit(
                {
                    val (req, res, result) = "https://cht.services/chat/api/sessions".httpPost(
                            listOf("host" to hostId, "visitor" to visitorId)
                    ).responseJson()

                    val (d, e) = result
                    data = d
                    error = e

                    request = req
                    response = res

                    lock.countDown()
                })

        lock.await(10, TimeUnit.SECONDS)

        return data!!.obj()["sessionId"] as String
    }
}
