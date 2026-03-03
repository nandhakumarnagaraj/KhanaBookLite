package com.khanabooklite.app.data.local

import com.khanabooklite.app.data.local.dao.*
import com.khanabooklite.app.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class DatabaseInitializer @Inject constructor(
    private val restaurantDao: RestaurantDao,
    private val categoryDao: CategoryDao,
    private val menuDao: MenuDao,
    private val userDao: UserDao
) {
    suspend fun initialize() {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        withContext(Dispatchers.IO) {
            // 0. Default Admin User (Owner)
            val defaultAdminPhone = "7902688308"
            if (userDao.getUserByEmail(defaultAdminPhone) == null) {
                val admin = UserEntity(
                    name = "Owner",
                    email = defaultAdminPhone,
                    passwordHash = com.khanabooklite.app.domain.manager.AuthManager.hashPassword("owner123"),
                    role = "admin",
                    whatsappNumber = defaultAdminPhone,
                    isActive = true,
                    createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                )
                userDao.insertUser(admin)
            }

            // Only initialize the minimal restaurant profile if it doesn't exist.
            // No hardcoded users or sample menu items to keep it clean.
            
            if (restaurantDao.getProfile() == null) {
                val profile = RestaurantProfileEntity(
                    id = 1,
                    shopName = "Default Shop",
                    shopAddress = "",
                    whatsappNumber = "",
                    lastResetDate = todayDate
                )
                restaurantDao.saveProfile(profile)
            }
        }
    }
}
