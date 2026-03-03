package com.khanabooklite.app.di

import android.content.Context
import androidx.room.Room
import com.khanabooklite.app.data.local.AppDatabase
import com.khanabooklite.app.data.local.dao.*
import com.khanabooklite.app.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .addMigrations(
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5,
            AppDatabase.MIGRATION_5_6,
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8,
            AppDatabase.MIGRATION_8_9,
            AppDatabase.MIGRATION_9_10,
            AppDatabase.MIGRATION_10_11
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase) = database.userDao()

    @Provides
    fun provideRestaurantDao(database: AppDatabase) = database.restaurantDao()

    @Provides
    fun provideCategoryDao(database: AppDatabase) = database.categoryDao()

    @Provides
    fun provideMenuDao(database: AppDatabase) = database.menuDao()

    @Provides
    fun provideBillDao(database: AppDatabase) = database.billDao()

    @Provides
    fun provideInventoryDao(database: AppDatabase) = database.inventoryDao()

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao) = UserRepository(userDao)

    @Provides
    @Singleton
    fun provideRestaurantRepository(restaurantDao: RestaurantDao) = RestaurantRepository(restaurantDao)

    @Provides
    @Singleton
    fun provideCategoryRepository(categoryDao: CategoryDao) = CategoryRepository(categoryDao)

    @Provides
    @Singleton
    fun provideMenuRepository(menuDao: MenuDao) = MenuRepository(menuDao)

    @Provides
    @Singleton
    fun provideBillRepository(billDao: BillDao) = BillRepository(billDao)

    @Provides
    @Singleton
    fun provideInventoryRepository(inventoryDao: InventoryDao, menuDao: MenuDao) = InventoryRepository(inventoryDao, menuDao)
}
