package com.khanabook.lite.pos.data.local

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
    private val userDao: UserDao,
    private val rawMaterialDao: RawMaterialDao,
    private val recipeDao: RecipeDao,
    private val batchDao: BatchDao
) {
    suspend fun initialize() {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val fullTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        
        // Calculate expiry date (6 months from now)
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, 6)
        val sixMonthsLater = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

        withContext(Dispatchers.IO) {
            // 0. Default Admin User (Owner)
            val defaultAdminPhone = "9150677849"
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
                    shopName = "",
                    shopAddress = "",
                    whatsappNumber = defaultAdminPhone,
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

            // Check if categories are empty
            if (categoryDao.getAllCategoriesFlow().first().isEmpty()) {
                // 2. Categories
                val starterVegId = categoryDao.insertCategory(CategoryEntity(name = "Veg Starters", isVeg = true, createdAt = fullTimestamp)).toInt()
                val starterNonVegId = categoryDao.insertCategory(CategoryEntity(name = "Non-Veg Starters", isVeg = false, createdAt = fullTimestamp)).toInt()
                val mainVegId = categoryDao.insertCategory(CategoryEntity(name = "Veg Main Course", isVeg = true, createdAt = fullTimestamp)).toInt()
                val mainNonVegId = categoryDao.insertCategory(CategoryEntity(name = "Non-Veg Main Course", isVeg = false, createdAt = fullTimestamp)).toInt()
                val beverageId = categoryDao.insertCategory(CategoryEntity(name = "Beverages", isVeg = true, createdAt = fullTimestamp)).toInt()
                val dessertId = categoryDao.insertCategory(CategoryEntity(name = "Desserts", isVeg = true, createdAt = fullTimestamp)).toInt()

                // 3. Menu Items
                val paneerTikkaId = menuDao.insertItem(MenuItemEntity(categoryId = starterVegId, name = "Paneer Tikka", basePrice = 180.0, foodType = "veg", stockQuantity = 50, createdAt = fullTimestamp)).toInt()
                
                val pizzaId = menuDao.insertItem(MenuItemEntity(categoryId = mainVegId, name = "Margherita Pizza", basePrice = 0.0, foodType = "veg", stockQuantity = 0, createdAt = fullTimestamp)).toInt()
                menuDao.insertVariant(ItemVariantEntity(menuItemId = pizzaId, variantName = "Small", price = 199.0, sortOrder = 1))
                menuDao.insertVariant(ItemVariantEntity(menuItemId = pizzaId, variantName = "Medium", price = 349.0, sortOrder = 2))
                menuDao.insertVariant(ItemVariantEntity(menuItemId = pizzaId, variantName = "Large", price = 599.0, sortOrder = 3))

                val biryaniId = menuDao.insertItem(MenuItemEntity(categoryId = mainNonVegId, name = "Hyderabadi Biryani", basePrice = 0.0, foodType = "non-veg", stockQuantity = 0, createdAt = fullTimestamp)).toInt()
                menuDao.insertVariant(ItemVariantEntity(menuItemId = biryaniId, variantName = "Half", price = 160.0, sortOrder = 1))
                menuDao.insertVariant(ItemVariantEntity(menuItemId = biryaniId, variantName = "Full", price = 280.0, sortOrder = 2))

                val springRollId = menuDao.insertItem(MenuItemEntity(categoryId = starterVegId, name = "Veg Spring Rolls", basePrice = 120.0, foodType = "veg", stockQuantity = 30, createdAt = fullTimestamp)).toInt()
                val lollipopId = menuDao.insertItem(MenuItemEntity(categoryId = starterNonVegId, name = "Chicken Lollipop", basePrice = 220.0, foodType = "non-veg", stockQuantity = 40, createdAt = fullTimestamp)).toInt()

                val coffeeId = menuDao.insertItem(MenuItemEntity(categoryId = beverageId, name = "Cold Coffee", basePrice = 0.0, foodType = "veg", stockQuantity = 0, createdAt = fullTimestamp)).toInt()
                menuDao.insertVariant(ItemVariantEntity(menuItemId = coffeeId, variantName = "With Ice Cream", price = 120.0, sortOrder = 1))
                menuDao.insertVariant(ItemVariantEntity(menuItemId = coffeeId, variantName = "Regular", price = 90.0, sortOrder = 2))

                val sodaId = menuDao.insertItem(MenuItemEntity(categoryId = beverageId, name = "Fresh Lime Soda", basePrice = 60.0, foodType = "veg", stockQuantity = 100, createdAt = fullTimestamp)).toInt()
                val jamunId = menuDao.insertItem(MenuItemEntity(categoryId = dessertId, name = "Gulab Jamun (2pcs)", basePrice = 50.0, foodType = "veg", stockQuantity = 80, createdAt = fullTimestamp)).toInt()

                // 4. Raw Materials
                val maidaId = rawMaterialDao.insertRawMaterial(RawMaterialEntity(name = "Flour (Maida)", unit = "kg", currentStock = 25.0, lowStockThreshold = 5.0, lastUpdated = fullTimestamp)).toInt()
                val chickenId = rawMaterialDao.insertRawMaterial(RawMaterialEntity(name = "Chicken (Boneless)", unit = "kg", currentStock = 12.5, lowStockThreshold = 3.0, lastUpdated = fullTimestamp)).toInt()
                val milkId = rawMaterialDao.insertRawMaterial(RawMaterialEntity(name = "Milk", unit = "ltr", currentStock = 15.0, lowStockThreshold = 5.0, lastUpdated = fullTimestamp)).toInt()
                val paneerId = rawMaterialDao.insertRawMaterial(RawMaterialEntity(name = "Paneer", unit = "kg", currentStock = 8.0, lowStockThreshold = 2.0, lastUpdated = fullTimestamp)).toInt()
                val oilId = rawMaterialDao.insertRawMaterial(RawMaterialEntity(name = "Cooking Oil", unit = "ltr", currentStock = 15.0, lowStockThreshold = 4.0, lastUpdated = fullTimestamp)).toInt()
                val sugarId = rawMaterialDao.insertRawMaterial(RawMaterialEntity(name = "Sugar", unit = "kg", currentStock = 10.0, lowStockThreshold = 2.0, lastUpdated = fullTimestamp)).toInt()
                val onionId = rawMaterialDao.insertRawMaterial(RawMaterialEntity(name = "Onions", unit = "kg", currentStock = 45.0, lowStockThreshold = 10.0, lastUpdated = fullTimestamp)).toInt()
                val tomatoId = rawMaterialDao.insertRawMaterial(RawMaterialEntity(name = "Tomatoes", unit = "kg", currentStock = 14.5, lowStockThreshold = 5.0, lastUpdated = fullTimestamp)).toInt()
                val cheeseId = rawMaterialDao.insertRawMaterial(RawMaterialEntity(name = "Mozzarella Cheese", unit = "kg", currentStock = 5.0, lowStockThreshold = 1.0, lastUpdated = fullTimestamp)).toInt()
                val lemonId = rawMaterialDao.insertRawMaterial(RawMaterialEntity(name = "Lemons", unit = "pcs", currentStock = 50.0, lowStockThreshold = 10.0, lastUpdated = fullTimestamp)).toInt()

                // Stock Logs
                val initialMaterials = listOf(
                    maidaId to 25.0, chickenId to 12.5, milkId to 15.0, paneerId to 8.0, 
                    oilId to 15.0, sugarId to 10.0, onionId to 45.0, tomatoId to 14.5,
                    cheeseId to 5.0, lemonId to 50.0
                )
                initialMaterials.forEach { (id, stock) ->
                    rawMaterialDao.insertStockLog(RawMaterialStockLogEntity(rawMaterialId = id, delta = stock, reason = "initial", createdAt = fullTimestamp))
                }

                // 5. Recipe & BOM (Initialization for ALL items)
                
                // Chicken Lollipop
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = lollipopId, rawMaterialId = chickenId, quantityNeeded = 0.25))
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = lollipopId, rawMaterialId = maidaId, quantityNeeded = 0.05))
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = lollipopId, rawMaterialId = oilId, quantityNeeded = 0.02))

                // Paneer Tikka
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = paneerTikkaId, rawMaterialId = paneerId, quantityNeeded = 0.2))
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = paneerTikkaId, rawMaterialId = onionId, quantityNeeded = 0.05))
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = paneerTikkaId, rawMaterialId = tomatoId, quantityNeeded = 0.05))

                // Margherita Pizza
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = pizzaId, rawMaterialId = maidaId, quantityNeeded = 0.15))
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = pizzaId, rawMaterialId = cheeseId, quantityNeeded = 0.1))
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = pizzaId, rawMaterialId = tomatoId, quantityNeeded = 0.04))

                // Hyderabadi Biryani
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = biryaniId, rawMaterialId = chickenId, quantityNeeded = 0.3))
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = biryaniId, rawMaterialId = onionId, quantityNeeded = 0.1))

                // Veg Spring Rolls
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = springRollId, rawMaterialId = maidaId, quantityNeeded = 0.1))
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = springRollId, rawMaterialId = onionId, quantityNeeded = 0.05))

                // Cold Coffee
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = coffeeId, rawMaterialId = milkId, quantityNeeded = 0.25))
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = coffeeId, rawMaterialId = sugarId, quantityNeeded = 0.02))

                // Fresh Lime Soda
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = sodaId, rawMaterialId = sugarId, quantityNeeded = 0.03))
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = sodaId, rawMaterialId = lemonId, quantityNeeded = 1.0))

                // Gulab Jamun
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = jamunId, rawMaterialId = maidaId, quantityNeeded = 0.05))
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = jamunId, rawMaterialId = sugarId, quantityNeeded = 0.1))
                recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = jamunId, rawMaterialId = milkId, quantityNeeded = 0.05))

                // 6. Batch & Expiry (Sample Data)
                batchDao.insertBatch(MaterialBatchEntity(
                    rawMaterialId = chickenId,
                    quantity = 12.5,
                    initialQuantity = 12.5,
                    expiryDate = sixMonthsLater,
                    receivedDate = fullTimestamp,
                    batchNumber = "BATCH-CH-001"
                ))
                
                batchDao.insertBatch(MaterialBatchEntity(
                    rawMaterialId = cheeseId,
                    quantity = 5.0,
                    initialQuantity = 5.0,
                    expiryDate = sixMonthsLater,
                    receivedDate = fullTimestamp,
                    batchNumber = "BATCH-CHZ-001"
                ))
            }
        }
    }
}
