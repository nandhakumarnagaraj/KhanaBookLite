package com.khanabooklite.app.data.repository

import com.khanabooklite.app.data.local.dao.RestaurantDao
import com.khanabooklite.app.data.local.entity.RestaurantProfileEntity
import kotlinx.coroutines.flow.Flow

class RestaurantRepository(private val restaurantDao: RestaurantDao) {
    suspend fun saveProfile(profile: RestaurantProfileEntity) {
        restaurantDao.saveProfile(profile)
    }

    suspend fun getProfile(): RestaurantProfileEntity? {
        return restaurantDao.getProfile()
    }

    fun getProfileFlow(): Flow<RestaurantProfileEntity?> {
        return restaurantDao.getProfileFlow()
    }

    suspend fun resetDailyCounter(counter: Int, date: String) {
        restaurantDao.resetDailyCounter(counter, date)
    }

    suspend fun incrementOrderCounters() {
        restaurantDao.incrementOrderCounters()
    }

    suspend fun updateUpiQrPath(path: String?) {
        restaurantDao.updateUpiQrPath(path)
    }

    suspend fun updateLogoPath(path: String?) {
        restaurantDao.updateLogoPath(path)
    }
}
