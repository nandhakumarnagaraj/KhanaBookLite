package com.khanabook.lite.pos.data.local

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.khanabook.lite.pos.data.local.dao.*
import com.khanabook.lite.pos.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        RestaurantProfileEntity::class,
        CategoryEntity::class,
        MenuItemEntity::class,
        ItemVariantEntity::class,
        BillEntity::class,
        BillItemEntity::class,
        BillPaymentEntity::class,
        StockLogEntity::class
    ],
    version = 11,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun restaurantDao(): RestaurantDao
    abstract fun categoryDao(): CategoryDao
    abstract fun menuDao(): MenuDao
    abstract fun billDao(): BillDao
    abstract fun inventoryDao(): InventoryDao

    companion object {
        const val DATABASE_NAME = "khanabook_lite_db"

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Remove duplicates before adding unique index
                db.execSQL("DELETE FROM categories WHERE id NOT IN (SELECT MIN(id) FROM categories GROUP BY name)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_categories_name` ON `categories` (`name`)")
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE restaurant_profile ADD COLUMN email_invoice_consent INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE menu_items ADD COLUMN food_type TEXT NOT NULL DEFAULT 'veg'")
                db.execSQL("ALTER TABLE menu_items ADD COLUMN description TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bills ADD COLUMN table_number TEXT")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE menu_items ADD COLUMN stock_quantity INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `bill_payments` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT, 
                        `bill_id` INTEGER NOT NULL, 
                        `payment_mode` TEXT NOT NULL, 
                        `amount` REAL NOT NULL, 
                        FOREIGN KEY(`bill_id`) REFERENCES `bills`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bill_payments_bill_id` ON `bill_payments` (`bill_id`)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE restaurant_profile ADD COLUMN is_tax_inclusive INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE menu_items ADD COLUMN low_stock_threshold INTEGER NOT NULL DEFAULT 10")
                db.execSQL("ALTER TABLE bills ADD COLUMN table_number TEXT")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `stock_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT, 
                        `menu_item_id` INTEGER NOT NULL, 
                        `delta` INTEGER NOT NULL, 
                        `reason` TEXT NOT NULL, 
                        `created_at` TEXT NOT NULL, 
                        FOREIGN KEY(`menu_item_id`) REFERENCES `menu_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_logs_menu_item_id` ON `stock_logs` (`menu_item_id`)")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE restaurant_profile ADD COLUMN session_timeout_minutes INTEGER NOT NULL DEFAULT 30")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // To remove a column in SQLite < 3.35 (standard Android for most API levels), 
                // we'd usually need to recreate the table. 
                // However, Room handles this best via schema exports or simple column ignores.
                // For a safe 'removal', we can just leave it in DB but remove from Entity.
                // If we want to be clean:
                db.execSQL("CREATE TABLE bills_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, daily_order_id INTEGER NOT NULL, daily_order_display TEXT NOT NULL, lifetime_order_id INTEGER NOT NULL, order_type TEXT NOT NULL DEFAULT 'order', customer_name TEXT, customer_whatsapp TEXT, subtotal REAL NOT NULL, gst_percentage REAL NOT NULL DEFAULT 0.0, cgst_amount REAL NOT NULL DEFAULT 0.0, sgst_amount REAL NOT NULL DEFAULT 0.0, custom_tax_amount REAL NOT NULL DEFAULT 0.0, total_amount REAL NOT NULL, payment_mode TEXT NOT NULL, part_amount_1 REAL NOT NULL DEFAULT 0.0, part_amount_2 REAL NOT NULL DEFAULT 0.0, payment_status TEXT NOT NULL, order_status TEXT NOT NULL, created_by INTEGER, created_at TEXT NOT NULL, paid_at TEXT, FOREIGN KEY(created_by) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE SET NULL)")
                db.execSQL("INSERT INTO bills_new (id, daily_order_id, daily_order_display, lifetime_order_id, order_type, customer_name, customer_whatsapp, subtotal, gst_percentage, cgst_amount, sgst_amount, custom_tax_amount, total_amount, payment_mode, part_amount_1, part_amount_2, payment_status, order_status, created_by, created_at, paid_at) SELECT id, daily_order_id, daily_order_display, lifetime_order_id, order_type, customer_name, customer_whatsapp, subtotal, gst_percentage, cgst_amount, sgst_amount, custom_tax_amount, total_amount, payment_mode, part_amount_1, part_amount_2, payment_status, order_status, created_by, created_at, paid_at FROM bills")
                db.execSQL("DROP TABLE bills")
                db.execSQL("ALTER TABLE bills_new RENAME TO bills")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bills_created_by` ON `bills` (`created_by`)")
            }
        }
    }
}


