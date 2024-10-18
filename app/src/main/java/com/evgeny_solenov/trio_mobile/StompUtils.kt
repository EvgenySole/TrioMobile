package com.example.testandro6

import android.annotation.SuppressLint
import android.util.Log
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent

object StompUtils {
    @SuppressLint("CheckResult")
    fun lifecycle(stompClient: StompClient) {
        stompClient.lifecycle().subscribe { lifecycleEvent ->
            when (lifecycleEvent.getType()) {
                LifecycleEvent.Type.OPENED -> Log.d(
                    "Stomp Utils",
                    "Stomp connection opened"
                )
                LifecycleEvent.Type.ERROR -> Log.e(
                    "Stomp Utils",
                    "Error",
                    lifecycleEvent.exception
                )
                LifecycleEvent.Type.CLOSED -> Log.d(
                    "Stomp Utils",
                    "Stomp connection closed"
                )
                LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> TODO()
            }

        }
    }
}