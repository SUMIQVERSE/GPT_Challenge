# Smart Gallery Cleaner (SaaS MVP)

An optimized, offline AI-powered gallery and file manager app for Android. Designed to solve severe storage bloat caused by auto-downloaded media. Meticulously built with memory-safe architectures capable of running on low-resource devices without OOM exceptions. 

## Key Features
- **MT Manager-like Root Access:** Deep `/Android/data/` scanning via **Shizuku** Binder API.
- **Eco-Scan Engine:** Layer 1 metadata filtering combined with simulated pHash indexing to bypass heavy AI workloads for non-media files.
- **On-Device VLM & OCR Engine:** Utilizes Google ML Kit and TFLite (quantized models) strictly offline to score a file from `0 to 100` dynamically, instead of rigid preset categories.
- **Chunk Processing:** Background processes chunk file scanning 50 items at a time, invoking Garbage Collection natively to strictly stay under 150MB of RAM.
- **Compose UI:** Fully reactive Compose Jetpack UI observing Room DB via StateFlow logic.

## How to Compile & Run
The repository is structured as a standard Android Kotlin project without the `gradlew` boilerplate which standard terminals may lack. 
To build:
1. Open **Android Studio**.
2. Select **File > Open** and select the `/asap-sorted/` project folder.
3. Android Studio will automatically identify `settings.gradle.kts`, download the Kotlin 1.9.0 & KSP 1.9.0 plugins, generate `gradlew` via the Gradle Daemon, and perform a Sync.
4. Ensure your physical device has **Shizuku** started via wireless debugging.
5. Click **Run**.
