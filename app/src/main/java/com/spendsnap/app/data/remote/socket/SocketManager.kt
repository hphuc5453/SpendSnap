package com.spendsnap.app.data.remote.socket

import android.util.Log
import com.spendsnap.app.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

enum class InvalidatedResource {
    TRANSACTIONS, STATISTICS, CATEGORIES, BUDGETS;

    companion object {
        fun fromString(value: String?): InvalidatedResource? = when (value) {
            "transactions" -> TRANSACTIONS
            "statistics" -> STATISTICS
            "categories" -> CATEGORIES
            "budgets" -> BUDGETS
            else -> null
        }
    }
}

@Singleton
class SocketManager @Inject constructor() {

    private var socket: Socket? = null
    private val _invalidations = MutableSharedFlow<Set<InvalidatedResource>>(extraBufferCapacity = 16)
    val invalidations: SharedFlow<Set<InvalidatedResource>> = _invalidations.asSharedFlow()

    @Synchronized
    fun connect(token: String) {
        if (token.isBlank()) return
        if (socket?.connected() == true) return
        disconnectInternal()

        val options = IO.Options().apply {
            auth = mapOf("token" to token)
            reconnection = true
            reconnectionDelay = 1_000L
            reconnectionDelayMax = 5_000L
        }

        val sock = runCatching { IO.socket(BuildConfig.BASE_URL, options) }
            .onFailure { Log.e(TAG, "Failed to init socket", it) }
            .getOrNull() ?: return

        sock.on(Socket.EVENT_CONNECT) {
            Log.d(TAG, "Socket connected: ${sock.id()}")
        }
        sock.on(Socket.EVENT_DISCONNECT) { args ->
            Log.d(TAG, "Socket disconnected: ${args.firstOrNull()}")
        }
        sock.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.w(TAG, "Socket connect error: ${args.firstOrNull()}")
        }
        sock.on("auth:error") { args ->
            Log.w(TAG, "Socket auth error: ${args.firstOrNull()}")
        }
        sock.on(EVENT_DATA_INVALIDATED) { args ->
            val payload = args.firstOrNull() as? JSONObject ?: return@on
            val affected = payload.optJSONArray("affected") ?: return@on
            val resources = buildSet {
                for (i in 0 until affected.length()) {
                    InvalidatedResource.fromString(affected.optString(i))?.let { add(it) }
                }
            }
            if (resources.isNotEmpty()) {
                Log.d(TAG, "data:invalidated → $resources")
                _invalidations.tryEmit(resources)
            }
        }

        sock.connect()
        socket = sock
    }

    @Synchronized
    fun disconnect() {
        disconnectInternal()
    }

    private fun disconnectInternal() {
        socket?.let {
            it.off()
            it.disconnect()
        }
        socket = null
    }

    companion object {
        private const val TAG = "SocketManager"
        private const val EVENT_DATA_INVALIDATED = "data:invalidated"
    }
}
