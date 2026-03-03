package com.khanabooklite.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khanabooklite.app.data.local.entity.UserEntity
import com.khanabooklite.app.data.repository.UserRepository
import com.khanabooklite.app.domain.manager.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val allUsers: Flow<List<UserEntity>> = userRepository.getAllUsers()

    fun addUser(name: String, phone: String, role: String, password: String) {
        viewModelScope.launch {
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val user = UserEntity(
                name = name,
                email = phone, // using phone as identifier
                passwordHash = AuthManager.hashPassword(password),
                role = role,
                whatsappNumber = phone,
                isActive = true,
                createdAt = now
            )
            userRepository.insertUser(user)
        }
    }

    fun deleteUser(user: UserEntity) {
        viewModelScope.launch {
            userRepository.deleteUser(user)
        }
    }

    fun toggleUserStatus(userId: Int, isActive: Boolean) {
        viewModelScope.launch {
            userRepository.setActivationStatus(userId, isActive)
        }
    }
}
