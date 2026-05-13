package com.spendsnap.app

import android.app.Application
import com.spendsnap.app.data.local.AuthManager
import com.spendsnap.app.data.remote.socket.SocketManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject lateinit var authManager: AuthManager
    @Inject lateinit var socketManager: SocketManager

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        authManager.accessToken
            .distinctUntilChanged()
            .onEach { token ->
                if (!token.isNullOrEmpty()) socketManager.connect(token)
                else socketManager.disconnect()
            }
            .launchIn(appScope)
    }
}
