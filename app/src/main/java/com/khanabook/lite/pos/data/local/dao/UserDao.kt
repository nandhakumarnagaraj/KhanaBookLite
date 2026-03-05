package com.khanabook.lite.pos.data.local.dao

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.room.*
import com.khanabook.lite.pos.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("UPDATE users SET password_hash = :newHash WHERE id = :userId")
    suspend fun updatePasswordHash(userId: Int, newHash: String)

    @Query("UPDATE users SET email = :newPhone, whatsapp_number = :newPhone WHERE role = 'admin'")
    suspend fun updateAdminPhoneNumber(newPhone: String)

    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("UPDATE users SET is_active = :isActive WHERE id = :userId")
    suspend fun setActivationStatus(userId: Int, isActive: Boolean)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}


