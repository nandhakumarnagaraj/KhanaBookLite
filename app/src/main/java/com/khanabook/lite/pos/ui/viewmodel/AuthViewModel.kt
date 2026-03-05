package com.khanabook.lite.pos.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.khanabook.lite.pos.BuildConfig
import com.khanabook.lite.pos.R
import com.khanabook.lite.pos.data.local.entity.RestaurantProfileEntity
import com.khanabook.lite.pos.data.local.entity.UserEntity
import com.khanabook.lite.pos.data.remote.*
import com.khanabook.lite.pos.data.repository.RestaurantRepository
import com.khanabook.lite.pos.data.repository.UserRepository
import com.khanabook.lite.pos.domain.manager.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "AuthViewModel"

// Maximum consecutive failed login attempts before lockout
private const val MAX_FAILED_ATTEMPTS = 5

@HiltViewModel
class AuthViewModel
@Inject
constructor(
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

    // OTP stored privately in ViewModel memory â€” never exposed through StateFlow
    private var generatedOtp: String? = null

    // â”€â”€ Brute-force protection
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private var failedLoginAttempts = 0
    private var lockoutUntilMs: Long = 0L

    fun login(email: String, password: String) {
        // Brute-force lockout check
        val now = System.currentTimeMillis()
        if (now < lockoutUntilMs) {
            val remainingSeconds = (lockoutUntilMs - now) / 1000
            _loginStatus.value =
                    LoginResult.Error(
                            "Too many failed attempts. Try again in $remainingSeconds seconds."
                    )
            return
        }

        viewModelScope.launch {
            val user = userRepository.getUserByEmail(email)
            if (user != null && AuthManager.verifyPassword(password, user.passwordHash)) {
                if (user.isActive) {
                    failedLoginAttempts = 0 // reset on success
                    userRepository.setCurrentUser(user)
                    _loginStatus.value = LoginResult.Success(user)
                } else {
                    _loginStatus.value = LoginResult.Error("Account is inactive")
                }
            } else {
                failedLoginAttempts++
                if (failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
                    // Lock out for 30 seconds per block of failures
                    lockoutUntilMs = System.currentTimeMillis() + 30_000L
                    failedLoginAttempts = 0
                    _loginStatus.value =
                            LoginResult.Error(
                                    "Too many failed attempts. Account temporarily locked for 30 seconds."
                            )
                } else {
                    val remaining = MAX_FAILED_ATTEMPTS - failedLoginAttempts
                    _loginStatus.value =
                            LoginResult.Error(
                                    "Invalid email or password. $remaining attempt(s) remaining."
                            )
                }
            }
        }
    }

    fun sendOtp(phoneNumber: String, purpose: String = "signup") {
        viewModelScope.launch {
            // Generate OTP â€” stored privately, never exposed in state
            val otp = (100000..999999).random().toString()
            generatedOtp = otp

            try {
                // Check if user exists for reset password
                if (purpose == "reset") {
                    val user = userRepository.getUserByEmail(phoneNumber)
                    if (user == null) {
                        _resetPasswordStatus.value =
                                ResetPasswordResult.Error("No account found with this number")
                        generatedOtp = null
                        return@launch
                    }
                }

                val formattedPhone = if (phoneNumber.length == 10) "91$phoneNumber" else phoneNumber

                val request =
                        WhatsAppRequest(
                                to = formattedPhone,
                                template =
                                        WhatsAppTemplate(
                                                name = BuildConfig.WHATSAPP_OTP_TEMPLATE_NAME,
                                                language = Language(),
                                                components =
                                                        listOf(
                                                                Component(
                                                                        type = "body",
                                                                        parameters =
                                                                                listOf(
                                                                                        Parameter(
                                                                                                text =
                                                                                                        otp
                                                                                        ) // Maps to
                                                                                        // {{1}}
                                                                                        )
                                                                ),
                                                                Component(
                                                                        type = "button",
                                                                        sub_type = "url",
                                                                        index = "0",
                                                                        parameters =
                                                                                listOf(
                                                                                        Parameter(
                                                                                                text =
                                                                                                        otp
                                                                                        ) // Value
                                                                                        // for
                                                                                        // Copy
                                                                                        // Code
                                                                                        // button
                                                                                        )
                                                                )
                                                        )
                                        )
                        )

                val response =
                        whatsAppApiService.sendOtp(
                                phoneNumberId = BuildConfig.WHATSAPP_PHONE_NUMBER_ID,
                                token = "Bearer ${BuildConfig.META_ACCESS_TOKEN}",
                                request = request
                        )

                if (response.isSuccessful) {
                    // âœ… OTP is NOT included in the state â€” only a "sent" signal
                    if (purpose == "reset") {
                        _resetPasswordStatus.value = ResetPasswordResult.OtpSent
                    } else {
                        _signUpStatus.value = SignUpResult.OtpSent
                    }
                } else {
                    val errorMsg = "Failed to send WhatsApp OTP. Please try again."
                    generatedOtp = null
                    if (purpose == "reset") {
                        _resetPasswordStatus.value = ResetPasswordResult.Error(errorMsg)
                    } else {
                        _signUpStatus.value = SignUpResult.Error(errorMsg)
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Network error. Please check your connection."
                generatedOtp = null
                if (purpose == "reset") {
                    _resetPasswordStatus.value = ResetPasswordResult.Error(errorMsg)
                } else {
                    _signUpStatus.value = SignUpResult.Error(errorMsg)
                }
            }
        }
    }

    fun verifyOtp(enteredOtp: String): Boolean {
        val valid = enteredOtp.isNotBlank() && enteredOtp == generatedOtp
        if (valid) {
            generatedOtp = null // Invalidate after successful verification
        }
        return valid
    }

    fun signUp(name: String, phoneNumber: String, password: String) {
        viewModelScope.launch {
            try {
                // 1. Create User
                val newUser =
                        UserEntity(
                                name = name,
                                email = phoneNumber,
                                passwordHash = AuthManager.hashPassword(password),
                                role = "admin",
                                whatsappNumber = phoneNumber,
                                isActive = true,
                                createdAt =
                                        java.text.SimpleDateFormat(
                                                        "yyyy-MM-dd HH:mm:ss",
                                                        java.util.Locale.getDefault()
                                                )
                                                .format(java.util.Date())
                        )
                userRepository.insertUser(newUser)

                // 2. Update Shop Profile with signup details
                val currentProfile = restaurantRepository.getProfile()
                val updatedProfile =
                        if (currentProfile != null) {
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
                                    lastResetDate =
                                            java.text.SimpleDateFormat(
                                                            "yyyy-MM-dd",
                                                            java.util.Locale.getDefault()
                                                    )
                                                    .format(java.util.Date())
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
                _resetPasswordStatus.value =
                        ResetPasswordResult.Error(e.message ?: "Failed to reset password")
            }
        }
    }

    fun logout() {
        userRepository.setCurrentUser(null)
        generatedOtp = null
        failedLoginAttempts = 0
        lockoutUntilMs = 0L
        _loginStatus.value = null
        _signUpStatus.value = null
        _resetPasswordStatus.value = null
    }

    /**
     * Real Google Sign-In using Credential Manager (modern Android API). Requires an Activity
     * context â€” pass LocalContext.current from the composable.
     */
    fun loginWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(activityContext)

                val googleIdOption =
                        GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false) // show all Google accounts
                                .setServerClientId(
                                        activityContext.getString(R.string.default_web_client_id)
                                )
                                .setAutoSelectEnabled(false)
                                .build()

                val request =
                        GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

                val result =
                        credentialManager.getCredential(
                                context = activityContext,
                                request = request
                        )

                val credential = result.credential
                if (credential is CustomCredential &&
                                credential.type ==
                                        GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
                    val googleEmail = googleCred.id
                    val googleName = googleCred.displayName ?: googleEmail

                    // Reuse existing account or create new one on first sign-in
                    val user =
                            userRepository.getUserByEmail(googleEmail)
                                    ?: run {
                                        val newUser =
                                                UserEntity(
                                                        name = googleName,
                                                        email = googleEmail,
                                                        passwordHash = "",
                                                        role = "admin",
                                                        whatsappNumber = null,
                                                        isActive = true,
                                                        createdAt =
                                                                SimpleDateFormat(
                                                                                "yyyy-MM-dd HH:mm:ss",
                                                                                Locale.getDefault()
                                                                        )
                                                                        .format(Date())
                                                )
                                        userRepository.insertUser(newUser)
                                        userRepository.getUserByEmail(googleEmail)!!
                                    }

                    failedLoginAttempts = 0
                    userRepository.setCurrentUser(user)
                    _loginStatus.value = LoginResult.Success(user)
                } else {
                    _loginStatus.value =
                            LoginResult.Error("Google Sign-In: unexpected credential type")
                }
            } catch (e: GetCredentialException) {
                Log.w(TAG, "Google Sign-In cancelled or unavailable", e)
                _loginStatus.value = LoginResult.Error("Google Sign-In cancelled. Try again.")
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                _loginStatus.value = LoginResult.Error("Google Sign-In failed. Please try again.")
            }
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
        // âœ… OTP removed from state â€” prevents OTP leakage through StateFlow
        object OtpSent : SignUpResult()
        data class Error(val message: String) : SignUpResult()
    }

    sealed class ResetPasswordResult {
        object Success : ResetPasswordResult()
        // âœ… OTP removed from state â€” prevents OTP leakage through StateFlow
        object OtpSent : ResetPasswordResult()
        data class Error(val message: String) : ResetPasswordResult()
    }
}
