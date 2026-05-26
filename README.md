# HBD PROCAST ProBill — Android App

> **GST Billing System for HBD PROCAST | Aluminium Die Casting Parts**

---

## 📱 About

Professional GST billing Android application for HBD PROCAST.
- Create Tax Invoices, Quotations, Proforma Invoices
- Manage Customers & Products
- Track Payments & Expenses  
- GST Reports (GSTR-1)
- Print / Share PDFs via WhatsApp / Email
- Works completely offline

---

## 🚀 Build APK on GitHub (No PC Required)

### Step 1 — Upload to GitHub
1. Create a new **private** GitHub repository
2. Upload all project files (drag & drop the folder contents)
3. Make sure these files are at the **root** of the repo:
   - `gradlew`
   - `gradlew.bat`
   - `build.gradle`
   - `settings.gradle`
   - `gradle.properties`
   - `app/build.gradle`
   - `.github/workflows/android.yml`

### Step 2 — Run GitHub Actions
1. Go to your repo → **Actions** tab
2. Click **"Build HBD ProBill APK"** workflow
3. Click **"Run workflow"** → **"Run workflow"**
4. Wait ~5–10 minutes for the build

### Step 3 — Download APK
1. Once the workflow shows ✅ green
2. Click on the completed run
3. Scroll to **Artifacts** section
4. Download **HBD-ProBill-Debug-vX.zip**
5. Extract → install `app-debug.apk` on your Android device

---

## 📁 Project Structure

```
hbd-android/
├── .github/
│   └── workflows/
│       └── android.yml          ← GitHub Actions (cloud build)
├── app/
│   ├── build.gradle             ← App Gradle config
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/www/
│       │   ├── index.html       ← App entry point
│       │   └── bundle.js        ← React billing app
│       ├── java/com/hbdprocast/billing/
│       │   └── MainActivity.java
│       └── res/
│           ├── drawable/
│           ├── mipmap-*/        ← App icons
│           ├── values/
│           │   ├── strings.xml
│           │   └── styles.xml
│           └── xml/
│               ├── file_paths.xml
│               └── network_security_config.xml
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
├── build.gradle                 ← Root Gradle config
├── settings.gradle
├── gradle.properties
├── gradlew                      ← Linux/macOS build script
└── gradlew.bat                  ← Windows build script
```

---

## 🏗️ Local Build (Optional)

### Requirements
- Android Studio (any recent version) OR
- JDK 17 + Android SDK

```bash
# Make gradlew executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📋 Technical Specs

| Property | Value |
|----------|-------|
| Package | `com.hbdprocast.billing` |
| Min Android | Android 6.0 (API 23) |
| Target Android | Android 14 (API 34) |
| Compile SDK | 34 |
| Java | 17 |
| AGP | 8.3.2 |
| Gradle | 8.7 |

---

## 🔒 Permissions

| Permission | Purpose | Android Version |
|-----------|---------|----------------|
| INTERNET | Network access | All |
| ACCESS_NETWORK_STATE | Connection check | All |
| WRITE_EXTERNAL_STORAGE | Save PDFs | Android 6–9 only |
| READ_EXTERNAL_STORAGE | Read files | Android 6–12 only |

*No dangerous permissions required on Android 10+*

---

## 🏢 HBD PROCAST
**CASTING THE FUTURE**  
Aluminium Die Casting Parts  
Village Chehlan, P.O. Ladhran, Tehsil Samrala  
Distt. Ludhiana, Punjab – 141124  
GSTIN: 03GJTPS1161J1ZS
