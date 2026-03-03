# KhanaBook Lite 

**KhanaBook Lite** is a powerful, offline-first Android Point of Sale (POS) and billing application designed for small restaurants, food stalls, and cafes. It focuses on speed, simplicity, and data privacy by keeping everything on-device.

## 🚀 Key Features

- ⚡ **Offline-First Billing**: Create and manage bills without requiring an active internet connection.
- 🥘 **Menu Management**: Easily organize categories, items, and variants.
- 💳 **Multiple Payment Modes**: Support for Cash, UPI, POS, and online delivery platforms (Zomato/Swiggy).
- 🧾 **Professional Invoices**: Generate 58mm/80mm PDF invoices and share them via WhatsApp.
- 📦 **Inventory Tracking**: Monitor stock levels with low-stock alerts and history logs.
- 📊 **Insightful Reports**: View daily and monthly sales breakdowns and top-performing items.
- 🔐 **Secure & Private**: Local data is encrypted with SQLCipher (Android Keystore key); passwords are hashed with BCrypt.
- 🖨️ **Bluetooth Printing**: Connect to thermal printers over Bluetooth for instant receipts.

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Database**: Room Persistence Library + SQLCipher (AES-256, Android Keystore-bound key)
- **Dependency Injection**: Hilt
- **Architecture**: MVVM + Repository Pattern
- **Networking**: Retrofit + OkHttp (for WhatsApp OTP)
- **PDF Generation**: Native Android Graphics & PDF API

## ⚙️ Setup Instructions

### 1. Prerequisites
- Android Studio Ladybug or newer.
- JDK 17.

### 2. Configure WhatsApp Meta API
The app uses the Meta Cloud API for sending OTPs. To set this up:
1. Create or open `local.properties` in the root directory.
2. Refer to `secrets.properties.example` and add your credentials:
   ```
   META_ACCESS_TOKEN=<your_token>
   WHATSAPP_PHONE_NUMBER_ID=<your_phone_id>
   WHATSAPP_OTP_TEMPLATE_NAME=verification_otp
   ```
3. These are injected at build time via `BuildConfig`. **Never commit real tokens to version control.**

> ⚠️ **Security Note**: BuildConfig fields are baked into the APK binary. For production, move OTP dispatch to a backend Cloud Function that holds the token server-side.

### 3. Configure Release Signing
Add the following to `local.properties` (never commit this file):
```
SIGNING_STORE_FILE=release-key.jks
SIGNING_STORE_PASSWORD=your_keystore_password
SIGNING_KEY_ALIAS=your_key_alias
SIGNING_KEY_PASSWORD=your_key_password
```
To generate a new keystore:
```
keytool -genkey -v -keystore release-key.jks -alias <alias> -keyalg RSA -keysize 2048 -validity 10000
```

### 4. Build & Run
- Open the project in Android Studio.
- Sync Gradle.
- Run the `app` module on a physical device (recommended for Bluetooth features) or an emulator.

## 🔒 Security Architecture

| Layer | Protection |
|-------|-----------|
| Database | SQLCipher AES-256, key stored in Android Keystore |
| Passwords | BCrypt (work factor 12) |
| API tokens | Stored in `local.properties` (gitignored), read at compile-time |
| Session | Auto-expiry + periodic background check |
| Backups | Excluded from Google auto-backup via `data_extraction_rules.xml` |
| Network | Cleartext denied for all domains (`network_security_config.xml`) |
| Release APK | R8 minification + ProGuard obfuscation enabled |

## 📁 Project Structure

- `data/local`: Room database configurations, DAOs, and Entities.
- `domain/manager`: Business logic handlers (Calculators, PDF Generators, etc.).
- `ui/screens`: Composable screens for the billing flow and dashboard.
- `ui/viewmodel`: Lifecycle-aware state management for UI components.

## 📄 License
Internal/Private Project. All rights reserved.
