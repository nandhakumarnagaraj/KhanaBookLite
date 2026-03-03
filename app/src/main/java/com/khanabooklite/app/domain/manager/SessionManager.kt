package com.khanabooklite.app.domain.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {
    private var lastInteractionTime: Long = System.currentTimeMillis()
    private var timeoutMinutes: Int = 30

    private val _isSessionExpired = MutableStateFlow(false)
    val isSessionExpired: StateFlow<Boolean> = _isSessionExpired

    fun updateTimeout(minutes: Int) {
        timeoutMinutes = minutes
    }

    fun onUserInteraction() {
        lastInteractionTime = System.currentTimeMillis()
        if (_isSessionExpired.value) {
            _isSessionExpired.value = false
        }
    }

    fun checkSession() {
        val currentTime = System.currentTimeMillis()
        val elapsedMillis = currentTime - lastInteractionTime
        val elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)

        if (elapsedMinutes >= timeoutMinutes) {
            _isSessionExpired.value = true
        }
    }
    
    fun resetSession() {
        _isSessionExpired.value = false
        lastInteractionTime = System.currentTimeMillis()
    }
}
