package com.khanabook.lite.pos.data.repository

import android.content.SharedPreferences
import com.khanabook.lite.pos.data.local.dao.UserDao
import com.khanabook.lite.pos.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val KEY_USER_EMAIL = "logged_in_user_email"

class UserRepository(
    private val userDao: UserDao,
    private val prefs: SharedPreferences
) {

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser

    suspend fun loadPersistedUser() {
        val email = prefs.getString(KEY_USER_EMAIL, null)
        if (email != null) {
            val user = userDao.getUserByEmail(email)
            _currentUser.value = user
        }
    }

    fun setCurrentUser(user: UserEntity?) {
        _currentUser.value = user
        if (user != null) {
            prefs.edit().putString(KEY_USER_EMAIL, user.email).apply()
        } else {
            prefs.edit().remove(KEY_USER_EMAIL).apply()
        }
    }

    suspend fun insertUser(user: UserEntity): Long {
        return userDao.insertUser(user)
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }

    suspend fun updatePasswordHash(userId: Int, newHash: String) {
        userDao.updatePasswordHash(userId, newHash)
    }

    suspend fun updateAdminPhoneNumber(newPhone: String) {
        userDao.updateAdminPhoneNumber(newPhone)
    }

    fun getAllUsers(): Flow<List<UserEntity>> {
        return userDao.getAllUsers()
    }

    suspend fun setActivationStatus(userId: Int, isActive: Boolean) {
        userDao.setActivationStatus(userId, isActive)
    }

    suspend fun deleteUser(user: UserEntity) {
        userDao.deleteUser(user)
    }
}
