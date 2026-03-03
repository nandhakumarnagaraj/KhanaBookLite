package com.khanabooklite.app.data.repository

import com.khanabooklite.app.data.local.dao.UserDao
import com.khanabooklite.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserRepository(private val userDao: UserDao) {

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser

    fun setCurrentUser(user: UserEntity?) {
        _currentUser.value = user
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
