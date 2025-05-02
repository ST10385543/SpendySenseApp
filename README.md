PROG7313 - PROGRAMMING 3C PART 2 POE
----------------------------------------------------------------
Memebers involved in project:
Ayush Mahabeer - ST10306742
Tyron Jeremiah Naidoo - ST10385722
Kayden Padayachee - ST10385543
----------------------------------------------------------------
Requirements for running android application
1. Operating System:
Windows, macOS, or Linux (depending on your development environment)

Ensure your OS is up-to-date for compatibility with development tools.

2. Java Development Kit (JDK):
JDK 11 or higher: Android development requires a JDK version 11 or higher.

Download JDK 11 (or use OpenJDK)

3. Android Studio:
Android Studio for app development, build, and testing.

Recommended version 4.2 or higher for optimal experience.

4. Android SDK:
The Android SDK comes bundled with Android Studio, but make sure you have the necessary SDK versions installed via the SDK Manager.

5. Gradle:
Your project will be built using Gradle, which is the default build system for Android apps.

Gradle is included with Android Studio, so no extra installation is necessary.

6. Kotlin Version:
Kotlin 1.9.0 or higher (depending on the version specified in your project setup).

7. Git:
Ensure Git is installed to clone the repository and manage version control.

8.  Android Room Dependencies:
Make sure that the Room dependencies are included in your build.gradle file. You should have the following dependencies:

gradle
Copy
Edit
// Room dependencies
implementation "androidx.room:room-runtime:2.7.1"
kapt "androidx.room:room-compiler:2.7.1"  // For annotation processing
implementation "androidx.room:room-ktx:2.7.1"  // For Kotlin extensions

kotlin("kapt")

----------------------------------------------------------------------------------------------------

