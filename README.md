# KhanaBook Lite 

**KhanaBook Lite** is a powerful, offline-first Android Point of Sale (POS) and billing application designed for small restaurants, food stalls, and cafes. It focuses on speed, simplicity, and data privacy by keeping everything on-device.

## 🚀 Key Features

- ⚡ **Offline-First Billing**: Create and manage bills without requiring an active internet connection.
- 🥘 **Menu Management**: Easily organize categories, items, and variants.
- 💳 **Multiple Payment Modes**: Support for Cash, UPI, POS, and online delivery platforms (Zomato/Swiggy).
- 🧾 **Professional Invoices**: Generate 58mm/80mm PDF invoices and share them via WhatsApp.
- 📦 **Inventory Tracking**: Monitor stock levels with low-stock alerts and history logs.
- 📊 **Insightful Reports**: View daily and monthly sales breakdowns and top-performing items.
- 🔐 **Secure & Private**: Local data is encrypted with SQLCipher; passwords are hashed with BCrypt.
- 🖨️ **Bluetooth Printing**: Connect to thermal printers over Bluetooth for instant receipts.

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Database**: Room Persistence Library + SQLCipher
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
1. Create a `local.properties` file in the root directory if it doesn't exist.
2. Refer to `secrets.properties.example` and add your `META_ACCESS_TOKEN`, `WHATSAPP_PHONE_NUMBER_ID`, and `WHATSAPP_OTP_TEMPLATE_NAME`.
3. These credentials will be injected into the app during build time via `BuildConfig`.

### 3. Build & Run
- Open the project in Android Studio.
- Sync Gradle.
- Run the `app` module on a physical device (recommended for Bluetooth features) or an emulator.

## 📁 Project Structure

- `data/local`: Room database configurations, DAOs, and Entities.
- `domain/manager`: Business logic handlers (Calculators, PDF Generators, etc.).
- `ui/screens`: Composable screens for the billing flow and dashboard.
- `ui/viewmodel`: Lifecycle-aware state management for UI components.

## 📄 License
Internal/Private Project. All rights reserved.
