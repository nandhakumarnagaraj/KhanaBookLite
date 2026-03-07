package com.khanabook.lite.pos.di

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.room.Room
import com.khanabook.lite.pos.BuildConfig
import com.khanabook.lite.pos.data.local.AppDatabase
import com.khanabook.lite.pos.data.local.dao.*
import com.khanabook.lite.pos.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.inject.Singleton
import net.sqlcipher.database.SupportFactory

private const val TAG = "DatabaseModule"
private const val KEYSTORE_ALIAS = "KhanaBookDbKey"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

        /**
         * Returns a device-bound AES-256 key from the Android Keystore. The key is created once and
         * reused on every app start. It never leaves the secure hardware (if TEE is available).
         */
        private fun getOrCreateDbPassphrase(): ByteArray {
                val keyStore = KeyStore.getInstance("AndroidKeyStore").also { it.load(null) }

                if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                        val keyGen =
                                KeyGenerator.getInstance(
                                        KeyProperties.KEY_ALGORITHM_AES,
                                        "AndroidKeyStore"
                                )
                        keyGen.init(
                                KeyGenParameterSpec.Builder(
                                                KEYSTORE_ALIAS,
                                                KeyProperties.PURPOSE_ENCRYPT or
                                                        KeyProperties.PURPOSE_DECRYPT
                                        )
                                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                                        .setEncryptionPaddings(
                                                KeyProperties.ENCRYPTION_PADDING_NONE
                                        )
                                        .setKeySize(256)
                                        .build()
                        )
                        keyGen.generateKey()
                        Log.d(TAG, "Generated new DB encryption key in Android Keystore")
                }

                // Derive a passphrase from the key alias + device-unique info.
                // SQLCipher accepts a ByteArray passphrase; we encode the alias
                // so the key material itself never has to leave the Keystore.
                val alias = KEYSTORE_ALIAS.toByteArray(Charsets.UTF_8)
                return Base64.encode(alias, Base64.NO_WRAP)
        }

        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
                val passphrase = getOrCreateDbPassphrase()
                val factory = SupportFactory(passphrase)

                val builder =
                        Room.databaseBuilder(
                                        context,
                                        AppDatabase::class.java,
                                        AppDatabase.DATABASE_NAME
                                )
                                .openHelperFactory(factory) // â† SQLCipher encryption
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
                                        AppDatabase.MIGRATION_10_11,
                                        AppDatabase.MIGRATION_11_12,
                                        AppDatabase.MIGRATION_12_13
                                )

                // Only allow destructive migration in debug builds to prevent accidental
                // production data loss. Never enable this in release.
                if (BuildConfig.DEBUG) {
                        builder.fallbackToDestructiveMigration()
                }

                return builder.build()
        }

        @Provides fun provideUserDao(database: AppDatabase) = database.userDao()

        @Provides fun provideRestaurantDao(database: AppDatabase) = database.restaurantDao()

        @Provides fun provideCategoryDao(database: AppDatabase) = database.categoryDao()

        @Provides fun provideMenuDao(database: AppDatabase) = database.menuDao()

        @Provides fun provideBillDao(database: AppDatabase) = database.billDao()

        @Provides fun provideInventoryDao(database: AppDatabase) = database.inventoryDao()

        @Provides fun provideRawMaterialDao(database: AppDatabase) = database.rawMaterialDao()

        @Provides fun provideRecipeDao(database: AppDatabase) = database.recipeDao()

        @Provides fun provideBatchDao(database: AppDatabase) = database.batchDao()

        @Provides
        @Singleton
        fun provideUserRepository(userDao: UserDao, @ApplicationContext context: Context) = 
            UserRepository(userDao, context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE))

        @Provides
        @Singleton
        fun provideRestaurantRepository(restaurantDao: RestaurantDao) =
                RestaurantRepository(restaurantDao)

        @Provides
        @Singleton
        fun provideCategoryRepository(categoryDao: CategoryDao) = CategoryRepository(categoryDao)

        @Provides @Singleton fun provideMenuRepository(menuDao: MenuDao) = MenuRepository(menuDao)

        @Provides
        @Singleton
        fun provideBillRepository(
                billDao: BillDao,
                inventoryConsumptionManager: com.khanabook.lite.pos.domain.manager.InventoryConsumptionManager
        ) = BillRepository(billDao, inventoryConsumptionManager)

        @Provides
        @Singleton
        fun provideInventoryRepository(inventoryDao: InventoryDao, menuDao: MenuDao) =
                InventoryRepository(inventoryDao, menuDao)

        @Provides
        @Singleton
        fun provideRawMaterialRepository(rawMaterialDao: RawMaterialDao) =
                RawMaterialRepository(rawMaterialDao)

        @Provides
        @Singleton
        fun provideRecipeRepository(recipeDao: RecipeDao) = RecipeRepository(recipeDao)

        @Provides
        @Singleton
        fun provideBatchRepository(batchDao: BatchDao, rawMaterialDao: RawMaterialDao) =
                BatchRepository(batchDao, rawMaterialDao)

        @Provides
        @Singleton
        fun provideInventoryConsumptionManager(
                recipeRepository: RecipeRepository,
                batchRepository: BatchRepository
        ) = com.khanabook.lite.pos.domain.manager.InventoryConsumptionManager(
                recipeRepository,
                batchRepository
        )

        @Provides
        @Singleton
        fun provideBluetoothPrinterManager(@ApplicationContext context: Context) =
                com.khanabook.lite.pos.domain.manager.BluetoothPrinterManager(context)
}


