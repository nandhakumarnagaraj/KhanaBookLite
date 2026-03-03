package com.khanabooklite.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khanabooklite.app.data.local.entity.UserEntity
import com.khanabooklite.app.data.local.entity.RestaurantProfileEntity
import com.khanabooklite.app.data.repository.UserRepository
import com.khanabooklite.app.data.repository.RestaurantRepository
import com.khanabooklite.app.domain.manager.AuthManager
import com.khanabooklite.app.data.remote.*
import com.khanabooklite.app.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val whatsAppApiService: WhatsAppApiService,
    private val restaurantRepository: RestaurantRepository
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = userRepository.currentUser

    private val _loginStatus = MutableStateFlow<LoginResult?>(null)
    val loginStatus: StateFlow<LoginResult?> = _loginStatus

    private val _signUpStatus = MutableStateFlow<SignUpResult?>(null)
    val signUpStatus: StateFlow<SignUpResult?> = _signUpStatus

    private val _resetPasswordStatus = MutableStateFlow<ResetPasswordResult?>(null)
    val resetPasswordStatus: StateFlow<ResetPasswordResult?> = _resetPasswordStatus

    private var generatedOtp: String? = null

    // Meta API Configuration
    private val META_ACCESS_TOKEN = BuildConfig.META_ACCESS_TOKEN
    private val WHATSAPP_PHONE_NUMBER_ID = BuildConfig.WHATSAPP_PHONE_NUMBER_ID
    private val OTP_TEMPLATE_NAME = BuildConfig.WHATSAPP_OTP_TEMPLATE_NAME 

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val user = userRepository.getUserByEmail(email)
            if (user != null && AuthManager.verifyPassword(password, user.passwordHash)) {
                if (user.isActive) {
                    userRepository.setCurrentUser(user)
                    _loginStatus.value = LoginResult.Success(user)
                } else {
                    _loginStatus.value = LoginResult.Error("Account is inactive")
                }
            } else {
                _loginStatus.value = LoginResult.Error("Invalid email or password")
            }
        }
    }

    fun sendOtp(phoneNumber: String, purpose: String = "signup") {
        viewModelScope.launch {
            val otp = (100000..999999).random().toString()
            generatedOtp = otp
            
            try {
                // Check if user exists for reset password
                if (purpose == "reset") {
                    val user = userRepository.getUserByEmail(phoneNumber)
                    if (user == null) {
                        _resetPasswordStatus.value = ResetPasswordResult.Error("No account found with this number")
                        return@launch
                    }
                }

                val formattedPhone = if (phoneNumber.length == 10) "91$phoneNumber" else phoneNumber
                
                val request = WhatsAppRequest(
                    to = formattedPhone,
                    template = WhatsAppTemplate(
                        name = OTP_TEMPLATE_NAME,
                        language = Language(),
                        components = listOf(
                            Component(
                                type = "body",
                                parameters = listOf(
                                    Parameter(text = otp) // Maps to {{1}}
                                )
                            ),
                            Component(
                                type = "button",
                                sub_type = "url",
                                index = "0",
                                parameters = listOf(
                                    Parameter(text = otp) // Value for Copy Code button
                                )
                            )
                        )
                    )
                )

                val response = whatsAppApiService.sendOtp(
                    phoneNumberId = WHATSAPP_PHONE_NUMBER_ID,
                    token = "Bearer $META_ACCESS_TOKEN",
                    request = request
                )

                if (response.isSuccessful) {
                    if (purpose == "reset") {
                        _resetPasswordStatus.value = ResetPasswordResult.OtpSent(otp)
                    } else {
                        _signUpStatus.value = SignUpResult.OtpSent(otp)
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    val errorMsg = "Failed to send WhatsApp: $errorBody"
                    if (purpose == "reset") {
                        _resetPasswordStatus.value = ResetPasswordResult.Error(errorMsg)
                    } else {
                        _signUpStatus.value = SignUpResult.Error(errorMsg)
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                if (purpose == "reset") {
                    _resetPasswordStatus.value = ResetPasswordResult.Error(errorMsg)
                } else {
                    _signUpStatus.value = SignUpResult.Error(errorMsg)
                }
            }
        }
    }

    fun verifyOtp(enteredOtp: String): Boolean {
        return enteredOtp == generatedOtp
    }

    fun signUp(name: String, phoneNumber: String, password: String) {
        viewModelScope.launch {
            try {
                // 1. Create User
                val newUser = UserEntity(
                    name = name,
                    email = phoneNumber, 
                    passwordHash = AuthManager.hashPassword(password),
                    role = "admin", 
                    whatsappNumber = phoneNumber,
                    isActive = true,
                    createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                )
                userRepository.insertUser(newUser)

                // 2. Update Shop Profile with signup details
                val currentProfile = restaurantRepository.getProfile()
                val updatedProfile = if (currentProfile != null) {
                    currentProfile.copy(
                        shopName = name,
                        whatsappNumber = phoneNumber,
                        upiMobile = phoneNumber
                    )
                } else {
                    RestaurantProfileEntity(
                        id = 1,
                        shopName = name,
                        shopAddress = "",
                        whatsappNumber = phoneNumber,
                        upiMobile = phoneNumber,
                        lastResetDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    )
                }
                restaurantRepository.saveProfile(updatedProfile)

                _signUpStatus.value = SignUpResult.Success
            } catch (e: Exception) {
                _signUpStatus.value = SignUpResult.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun resetPassword(phoneNumber: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val user = userRepository.getUserByEmail(phoneNumber)
                if (user != null) {
                    val newHash = AuthManager.hashPassword(newPassword)
                    userRepository.updatePasswordHash(user.id, newHash)
                    _resetPasswordStatus.value = ResetPasswordResult.Success
                } else {
                    _resetPasswordStatus.value = ResetPasswordResult.Error("User not found")
                }
            } catch (e: Exception) {
                _resetPasswordStatus.value = ResetPasswordResult.Error(e.message ?: "Failed to reset password")
            }
        }
    }

    fun logout() {
        userRepository.setCurrentUser(null)
        _loginStatus.value = null
        _signUpStatus.value = null
        _resetPasswordStatus.value = null
    }

    fun loginWithGoogle() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            val googleUser = UserEntity(
                name = "Google User",
                email = "google@example.com",
                passwordHash = "",
                role = "admin",
                whatsappNumber = null,
                isActive = true,
                createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            )
            userRepository.setCurrentUser(googleUser)
            _loginStatus.value = LoginResult.Success(googleUser)
        }
    }

    fun loginWithFacebook() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            val fbUser = UserEntity(
                name = "Facebook User",
                email = "fb@example.com",
                passwordHash = "",
                role = "admin",
                whatsappNumber = null,
                isActive = true,
                createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            )
            userRepository.setCurrentUser(fbUser)
            _loginStatus.value = LoginResult.Success(fbUser)
        }
    }

    fun resetSignUpStatus() {
        _signUpStatus.value = null
    }
    
    fun clearResetStatus() {
        _resetPasswordStatus.value = null
    }

    sealed class LoginResult {
        data class Success(val user: UserEntity) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }

    sealed class SignUpResult {
        object Success : SignUpResult()
        data class OtpSent(val otp: String) : SignUpResult()
        data class Error(val message: String) : SignUpResult()
    }

    sealed class ResetPasswordResult {
        object Success : ResetPasswordResult()
        data class OtpSent(val otp: String) : ResetPasswordResult()
        data class Error(val message: String) : ResetPasswordResult()
    }
}
