package com.khanabooklite.app.data.local.dao

import androidx.room.*
import com.khanabooklite.app.data.local.entity.RestaurantProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: RestaurantProfileEntity)

    @Query("SELECT * FROM restaurant_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): RestaurantProfileEntity?

    @Query("SELECT * FROM restaurant_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<RestaurantProfileEntity?>

    @Query("UPDATE restaurant_profile SET daily_order_counter = :counter, last_reset_date = :date WHERE id = 1")
    suspend fun resetDailyCounter(counter: Int, date: String)

    @Query("UPDATE restaurant_profile SET daily_order_counter = daily_order_counter + 1, lifetime_order_counter = lifetime_order_counter + 1 WHERE id = 1")
    suspend fun incrementOrderCounters()

    @Query("UPDATE restaurant_profile SET upi_qr_path = :path WHERE id = 1")
    suspend fun updateUpiQrPath(path: String?)

    @Query("UPDATE restaurant_profile SET logo_path = :path WHERE id = 1")
    suspend fun updateLogoPath(path: String?)
}
