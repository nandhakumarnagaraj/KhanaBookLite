import sqlite3
import datetime
import os

# Database file path
db_path = r"c:\Users\nandh\AndroidStudioProjects\KhanaBookLite2\sample_data.db"

# Create/Connect to the database
conn = sqlite3.connect(db_path)
cursor = conn.cursor()

# SQL Create Table statements (extracted from Room schema)
schema_queries = [
    "CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `email` TEXT NOT NULL, `password_hash` TEXT NOT NULL, `role` TEXT NOT NULL, `whatsapp_number` TEXT, `is_active` INTEGER NOT NULL DEFAULT 1, `created_at` TEXT NOT NULL)",
    "CREATE TABLE IF NOT EXISTS `restaurant_profile` (`id` INTEGER NOT NULL, `shop_name` TEXT NOT NULL, `shop_address` TEXT NOT NULL, `whatsapp_number` TEXT NOT NULL, `email` TEXT, `logo_path` TEXT, `fssai_number` TEXT, `email_invoice_consent` INTEGER NOT NULL DEFAULT 0, `country` TEXT NOT NULL DEFAULT 'India', `gst_enabled` INTEGER NOT NULL DEFAULT 0, `gstin` TEXT, `is_tax_inclusive` INTEGER NOT NULL DEFAULT 0, `gst_percentage` REAL NOT NULL DEFAULT 0.0, `custom_tax_name` TEXT, `custom_tax_number` TEXT, `custom_tax_percentage` REAL NOT NULL DEFAULT 0.0, `currency` TEXT NOT NULL DEFAULT 'INR', `upi_enabled` INTEGER NOT NULL DEFAULT 0, `upi_qr_path` TEXT, `upi_handle` TEXT, `upi_mobile` TEXT, `cash_enabled` INTEGER NOT NULL DEFAULT 1, `pos_enabled` INTEGER NOT NULL DEFAULT 0, `zomato_enabled` INTEGER NOT NULL DEFAULT 0, `swiggy_enabled` INTEGER NOT NULL DEFAULT 0, `own_website_enabled` INTEGER NOT NULL DEFAULT 0, `printer_enabled` INTEGER NOT NULL DEFAULT 0, `printer_name` TEXT, `printer_mac` TEXT, `paper_size` TEXT NOT NULL DEFAULT '58mm', `auto_print_on_success` INTEGER NOT NULL DEFAULT 0, `include_logo_in_print` INTEGER NOT NULL DEFAULT 1, `print_customer_whatsapp` INTEGER NOT NULL DEFAULT 1, `daily_order_counter` INTEGER NOT NULL DEFAULT 0, `lifetime_order_counter` INTEGER NOT NULL DEFAULT 0, `last_reset_date` TEXT, `session_timeout_minutes` INTEGER NOT NULL DEFAULT 30, PRIMARY KEY(`id`))",
    "CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `is_veg` INTEGER NOT NULL, `sort_order` INTEGER NOT NULL DEFAULT 0, `is_active` INTEGER NOT NULL DEFAULT 1, `created_at` TEXT NOT NULL)",
    "CREATE TABLE IF NOT EXISTS `menu_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `category_id` INTEGER NOT NULL, `name` TEXT NOT NULL, `base_price` REAL NOT NULL, `food_type` TEXT NOT NULL DEFAULT 'veg', `description` TEXT, `is_available` INTEGER NOT NULL DEFAULT 1, `stock_quantity` INTEGER NOT NULL DEFAULT 0, `low_stock_threshold` INTEGER NOT NULL DEFAULT 10, `created_at` TEXT NOT NULL, FOREIGN KEY(`category_id`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `item_variants` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `menu_item_id` INTEGER NOT NULL, `variant_name` TEXT NOT NULL, `price` REAL NOT NULL, `is_available` INTEGER NOT NULL DEFAULT 1, `sort_order` INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(`menu_item_id`) REFERENCES `menu_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `bills` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `daily_order_id` INTEGER NOT NULL, `daily_order_display` TEXT NOT NULL, `lifetime_order_id` INTEGER NOT NULL, `order_type` TEXT NOT NULL DEFAULT 'order', `customer_name` TEXT, `customer_whatsapp` TEXT, `subtotal` REAL NOT NULL, `gst_percentage` REAL NOT NULL DEFAULT 0.0, `cgst_amount` REAL NOT NULL DEFAULT 0.0, `sgst_amount` REAL NOT NULL DEFAULT 0.0, `custom_tax_amount` REAL NOT NULL DEFAULT 0.0, `total_amount` REAL NOT NULL, `payment_mode` TEXT NOT NULL, `part_amount_1` REAL NOT NULL DEFAULT 0.0, `part_amount_2` REAL NOT NULL DEFAULT 0.0, `payment_status` TEXT NOT NULL, `order_status` TEXT NOT NULL, `created_by` INTEGER, `created_at` TEXT NOT NULL, `paid_at` TEXT, FOREIGN KEY(`created_by`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )",
    "CREATE TABLE IF NOT EXISTS `bill_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `bill_id` INTEGER NOT NULL, `menu_item_id` INTEGER, `item_name` TEXT NOT NULL, `variant_id` INTEGER, `variant_name` TEXT, `price` REAL NOT NULL, `quantity` INTEGER NOT NULL, `item_total` REAL NOT NULL, `special_instruction` TEXT, FOREIGN KEY(`bill_id`) REFERENCES `bills`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`menu_item_id`) REFERENCES `menu_items`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`variant_id`) REFERENCES `item_variants`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )",
    "CREATE TABLE IF NOT EXISTS `bill_payments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `bill_id` INTEGER NOT NULL, `payment_mode` TEXT NOT NULL, `amount` REAL NOT NULL, FOREIGN KEY(`bill_id`) REFERENCES `bills`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `stock_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `menu_item_id` INTEGER NOT NULL, `delta` INTEGER NOT NULL, `reason` TEXT NOT NULL, `created_at` TEXT NOT NULL, FOREIGN KEY(`menu_item_id`) REFERENCES `menu_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)",
    "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '88bf4d5624789b3a04a0e0a315446830')"
]

# Indices and other queries
indices_queries = [
    "CREATE UNIQUE INDEX IF NOT EXISTS `index_categories_name` ON `categories` (`name`)",
    "CREATE INDEX IF NOT EXISTS `index_menu_items_category_id` ON `menu_items` (`category_id`)",
    "CREATE INDEX IF NOT EXISTS `index_item_variants_menu_item_id` ON `item_variants` (`menu_item_id`)",
    "CREATE INDEX IF NOT EXISTS `index_bills_created_by` ON `bills` (`created_by`)",
    "CREATE INDEX IF NOT EXISTS `index_bill_items_bill_id` ON `bill_items` (`bill_id`)",
    "CREATE INDEX IF NOT EXISTS `index_bill_items_menu_item_id` ON `bill_items` (`menu_item_id`)",
    "CREATE INDEX IF NOT EXISTS `index_bill_items_variant_id` ON `bill_items` (`variant_id`)",
    "CREATE INDEX IF NOT EXISTS `index_bill_payments_bill_id` ON `bill_payments` (`bill_id`)",
    "CREATE INDEX IF NOT EXISTS `index_stock_logs_menu_item_id` ON `stock_logs` (`menu_item_id`)"
]

# Execute schema creation
for query in schema_queries:
    cursor.execute(query)

for query in indices_queries:
    cursor.execute(query)

# Sample Data
now = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
today_date = datetime.datetime.now().strftime("%Y-%m-%d")

# 1. Users (Admin)
cursor.execute("INSERT INTO users (name, email, password_hash, role, whatsapp_number, created_at) VALUES (?, ?, ?, ?, ?, ?)",
               ("Admin User", "admin@khanabook.com", "$2a$12$Kj/g/6L1F.x8v6C8Wv7W1uXv1v1v1v1v1v1v1v1v1v1v1v1v1v1", "admin", "9876543210", now))

# 2. Restaurant Profile
cursor.execute("""
INSERT INTO restaurant_profile (
    id, shop_name, shop_address, whatsapp_number, email, country, currency,
    gst_enabled, gst_percentage, is_tax_inclusive, cash_enabled, upi_enabled,
    daily_order_counter, lifetime_order_counter, last_reset_date
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
""", (1, "The Grand Kitchen", "123 Foodie Street, Bangalore", "9876543210", "hello@grandkitchen.com", "India", "INR",
      1, 5.0, 1, 1, 1, 10, 100, today_date))

# 3. Categories
categories = [
    ("Starters", 1, 1),
    ("Main Course", 1, 2),
    ("Beverages", 1, 3),
    ("Desserts", 1, 4)
]
for cat in categories:
    cursor.execute("INSERT INTO categories (name, is_veg, sort_order, created_at) VALUES (?, ?, ?, ?)", (cat[0], cat[1], cat[2], now))

# 4. Menu Items
menu_items = [
    (1, "Paneer Tikka", 180.0, "veg", "Spicy grilled cottage cheese", 50, now),
    (1, "Veg Spring Roll", 120.0, "veg", "Crispy vegetable rolls", 30, now),
    (2, "Dal Makhani", 220.0, "veg", "Slow-cooked black lentils", 100, now),
    (2, "Butter Naan", 40.0, "veg", "Soft Indian bread with butter", 200, now),
    (3, "Fresh Lime Soda", 60.0, "veg", "Refreshing citrus drink", 500, now),
    (3, "Masala Chai", 30.0, "veg", "Spiced Indian tea", 1000, now),
]
for item in menu_items:
    cursor.execute("INSERT INTO menu_items (category_id, name, base_price, food_type, description, stock_quantity, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)", item)

# 5. Item Variants (e.g., Full/Half)
cursor.execute("INSERT INTO item_variants (menu_item_id, variant_name, price) VALUES (?, ?, ?)", (3, "Full", 220.0))
cursor.execute("INSERT INTO item_variants (menu_item_id, variant_name, price) VALUES (?, ?, ?)", (3, "Half", 120.0))

# 6. Some sample bills
# Bill 1
cursor.execute("""
INSERT INTO bills (
    daily_order_id, daily_order_display, lifetime_order_id, customer_name, customer_whatsapp,
    subtotal, gst_percentage, cgst_amount, sgst_amount, total_amount, payment_mode,
    payment_status, order_status, created_by, created_at
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
""", (1, "D-001", 101, "John Doe", "9876500001", 300.0, 5.0, 7.5, 7.5, 315.0, "CASH", "PAID", "COMPLETED", 1, now))

bill_id = cursor.lastrowid
cursor.execute("INSERT INTO bill_items (bill_id, menu_item_id, item_name, price, quantity, item_total) VALUES (?, ?, ?, ?, ?, ?)",
               (bill_id, 1, "Paneer Tikka", 180.0, 1, 180.0))
cursor.execute("INSERT INTO bill_items (bill_id, menu_item_id, item_name, price, quantity, item_total) VALUES (?, ?, ?, ?, ?, ?)",
               (bill_id, 2, "Veg Spring Roll", 120.0, 1, 120.0))
cursor.execute("INSERT INTO bill_payments (bill_id, payment_mode, amount) VALUES (?, ?, ?)", (bill_id, "CASH", 315.0))

# Bill 2
cursor.execute("""
INSERT INTO bills (
    daily_order_id, daily_order_display, lifetime_order_id, customer_name, customer_whatsapp,
    subtotal, gst_percentage, cgst_amount, sgst_amount, total_amount, payment_mode,
    payment_status, order_status, created_by, created_at
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
""", (2, "D-002", 102, "Alice Smith", "9876500002", 440.0, 5.0, 11.0, 11.0, 462.0, "UPI", "PAID", "COMPLETED", 1, now))

bill_id = cursor.lastrowid
cursor.execute("INSERT INTO bill_items (bill_id, menu_item_id, item_name, price, quantity, item_total) VALUES (?, ?, ?, ?, ?, ?)",
               (bill_id, 3, "Dal Makhani", 220.0, 2, 440.0))
cursor.execute("INSERT INTO bill_payments (bill_id, payment_mode, amount) VALUES (?, ?, ?)", (bill_id, "UPI", 462.0))

# Commit and close
conn.commit()
conn.close()

print(f"Sample database created successfully at: {db_path}")
