package com.khanabook.lite.pos.data.local

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import com.khanabook.lite.pos.data.local.dao.*
import com.khanabook.lite.pos.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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
        val fullTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        withContext(Dispatchers.IO) {
            // 0. Default Admin User (Owner)
            val defaultAdminPhone = "7902688308"
            if (userDao.getUserByEmail(defaultAdminPhone) == null) {
                val admin = UserEntity(
                    name = "Owner",
                    email = defaultAdminPhone,
                    passwordHash = com.khanabook.lite.pos.domain.manager.AuthManager.hashPassword("owner123"),
                    role = "admin",
                    whatsappNumber = defaultAdminPhone,
                    isActive = true,
                    createdAt = fullTimestamp
                )
                userDao.insertUser(admin)
            }

            // 1. Restaurant Profile Configuration
            if (restaurantDao.getProfile() == null) {
                val profile = RestaurantProfileEntity(
                    id = 1,
                    shopName = "Khana Book Kitchen",
                    shopAddress = "123, Foodie Street, Gourmet City",
                    whatsappNumber = "7902688308",
                    email = "contact@khanabook.com",
                    fssaiNumber = "12345678901234",
                    country = "India",
                    gstEnabled = true,
                    gstPercentage = 5.0, // 5% GST
                    isTaxInclusive = false,
                    currency = "INR",
                    cashEnabled = true,
                    upiEnabled = true,
                    posEnabled = true,
                    printerEnabled = false,
                    paperSize = "58mm",
                    lastResetDate = todayDate,
                    sessionTimeoutMinutes = 60
                )
                restaurantDao.saveProfile(profile)
            }

            // 2. Categories and Menu Items (Sample Data)
            if (categoryDao.getAllCategoriesFlow().first().isEmpty()) {
                // Categories
                val starterVegId = categoryDao.insertCategory(CategoryEntity(name = "Veg Starters", isVeg = true, createdAt = fullTimestamp)).toInt()
                val starterNonVegId = categoryDao.insertCategory(CategoryEntity(name = "Non-Veg Starters", isVeg = false, createdAt = fullTimestamp)).toInt()
                val mainVegId = categoryDao.insertCategory(CategoryEntity(name = "Veg Main Course", isVeg = true, createdAt = fullTimestamp)).toInt()
                val mainNonVegId = categoryDao.insertCategory(CategoryEntity(name = "Non-Veg Main Course", isVeg = false, createdAt = fullTimestamp)).toInt()
                val beverageId = categoryDao.insertCategory(CategoryEntity(name = "Beverages", isVeg = true, createdAt = fullTimestamp)).toInt()
                val dessertId = categoryDao.insertCategory(CategoryEntity(name = "Desserts", isVeg = true, createdAt = fullTimestamp)).toInt()

                // Starters
                menuDao.insertItem(MenuItemEntity(categoryId = starterVegId, name = "Paneer Tikka", basePrice = 180.0, foodType = "veg", stockQuantity = 50, createdAt = fullTimestamp))
                menuDao.insertItem(MenuItemEntity(categoryId = starterVegId, name = "Veg Spring Rolls", basePrice = 120.0, foodType = "veg", stockQuantity = 30, createdAt = fullTimestamp))
                menuDao.insertItem(MenuItemEntity(categoryId = starterNonVegId, name = "Chicken Lollipop", basePrice = 220.0, foodType = "non-veg", stockQuantity = 40, createdAt = fullTimestamp))
                menuDao.insertItem(MenuItemEntity(categoryId = starterNonVegId, name = "Fish Fry", basePrice = 250.0, foodType = "non-veg", stockQuantity = 20, createdAt = fullTimestamp))

                // Main Course
                menuDao.insertItem(MenuItemEntity(categoryId = mainVegId, name = "Paneer Butter Masala", basePrice = 240.0, foodType = "veg", stockQuantity = 60, createdAt = fullTimestamp))
                menuDao.insertItem(MenuItemEntity(categoryId = mainVegId, name = "Dal Makhani", basePrice = 180.0, foodType = "veg", stockQuantity = 45, createdAt = fullTimestamp))
                menuDao.insertItem(MenuItemEntity(categoryId = mainNonVegId, name = "Butter Chicken", basePrice = 280.0, foodType = "non-veg", stockQuantity = 55, createdAt = fullTimestamp))
                menuDao.insertItem(MenuItemEntity(categoryId = mainNonVegId, name = "Mutton Rogan Josh", basePrice = 350.0, foodType = "non-veg", stockQuantity = 25, createdAt = fullTimestamp))

                // Beverages
                menuDao.insertItem(MenuItemEntity(categoryId = beverageId, name = "Fresh Lime Soda", basePrice = 60.0, foodType = "veg", stockQuantity = 100, createdAt = fullTimestamp))
                menuDao.insertItem(MenuItemEntity(categoryId = beverageId, name = "Cold Coffee", basePrice = 90.0, foodType = "veg", stockQuantity = 40, createdAt = fullTimestamp))
                menuDao.insertItem(MenuItemEntity(categoryId = beverageId, name = "Soft Drink (Can)", basePrice = 40.0, foodType = "veg", stockQuantity = 200, createdAt = fullTimestamp))

                // Desserts
                menuDao.insertItem(MenuItemEntity(categoryId = dessertId, name = "Gulab Jamun (2pcs)", basePrice = 50.0, foodType = "veg", stockQuantity = 80, createdAt = fullTimestamp))
                menuDao.insertItem(MenuItemEntity(categoryId = dessertId, name = "Vanilla Ice Cream", basePrice = 70.0, foodType = "veg", stockQuantity = 30, createdAt = fullTimestamp))
                menuDao.insertItem(MenuItemEntity(categoryId = dessertId, name = "Chocolate Brownie", basePrice = 110.0, foodType = "veg", stockQuantity = 25, createdAt = fullTimestamp))
            }
        }
    }
}


