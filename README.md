PROG7313 - PROGRAMMING 3C PART 3 POE
----------------------------------------------------------------
Memebers involved in project:
Ayush Mahabeer - ST10306742
Tyron Jeremiah Naidoo - ST10385722
Kayden Padayachee - ST10385543
----------------------------------------------------------------
GitHub Repositries:
Main: https://github.com/VCWVL/prog7313-poe-ST10306742.git

Backup: https://github.com/ST10385543/SpendySenseApp.git
----------------------------------------------------------------
Youtube demonstration video:
https://youtu.be/s3XVzBEavJc
----------------------------------------------------------------

Plugins Used:

com.android.application

Kotlin Android plugin (via alias libs.plugins.kotlin.android)

com.google.gms.google-services

Kotlin KAPT for annotation processing

--Dependencies Included

AndroidX and Core Libraries:
androidx.core.ktx
androidx.appcompat
com.google.android.material:material
androidx.constraintlayout
androidx.lifecycle:livedata-ktx
androidx.lifecycle:viewmodel-ktx
androidx.navigation:navigation-fragment-ktx
androidx.navigation:navigation-ui-ktx
androidx.legacy.support.v4
androidx.fragment.ktx
androidx.activity
Firebase (via BoM version 33.13.0):
firebase-auth
firebase-auth-ktx
firebase-firestore
firebase-database
firebase-database-ktx
firebase-storage

-Room Database:
androidx.room:room-runtime:2.7.1
androidx.room:room-compiler:2.7.1 (requires kapt)
androidx.room:room-ktx:2.7.1

Other Libraries:
Gson for JSON parsing: com.google.code.gson:gson:2.10.1
Glide for image loading: com.github.bumptech.glide:glide:4.16.0 (with kapt for compiler)
SkeletonLayout for loading placeholders: com.faltenreich:skeletonlayout:6.0.0
MPAndroidChart for chart visualizations: com.github.PhilJay:MPAndroidChart:v3.1.0

-Testing Libraries:
JUnit for unit testing
androidx.test.ext:junit for Android test support
androidx.test.espresso:espresso-core for UI testing
Requirements for Running the Android Application:

-Operating System:
Windows, macOS, or Linux
Ensure your OS is updated for compatibility with Android Studio and development tools
Java Development Kit (JDK):
JDK 11 or higher (recommended OpenJDK 11+)

-Android Studio:
Version 4.2 or higher recommended for best support and compatibility

-Android SDK:
Comes bundled with Android Studio
Ensure required API levels and tools are installed via SDK Manager

-Gradle:
Included with Android Studio
Used as the build system for your Android app

-Kotlin Version:
Kotlin 1.9.0 or higher (as required by your plugins and libraries)

-Git:
Installed and configured for version control and repository cloning

-Room Database Dependencies:
room-runtime: 2.7.1
room-compiler: 2.7.1
room-ktx: 2.7.1
Ensure kapt is enabled for annotation processing

 
---------------------------------------------------------------------------------------------------------------------------------------------------------
 
Purpose of the app
 
SpendySense is your all-in-one budget app that makes managing money easy, fun, and social! With SpendySense,
you can track your income and expenses, create your own spending categories (with cool icons and even photos of your receipts), and see everything in easy-to-read charts and graphs.
You can set savings goals, check your progress, and earn fun achievements along the way to keep you motivated. Want to share your progress? You can add friends and see each other’s achievements too! There’s even a bubble-popping mini-game when you need a quick break.
SpendySense also lets you leave feedback to help us make the app even better.
Your data is safely stored in the cloud, and you'll get an email to confirm your account when you sign up. Once you log in, you're welcomed with a dashboard showing your financial progress at a glance. SpendySense isn’t just a budget tracker—it’s a fun and friendly way to stay on top of your money.
--------------------------------------------------------------------------------------------------------------------------------------------------------
 
Steps on how to use the app
 
-Download and Install
 
Get GitHub repo link, Open on android studio
 
-Register an Account
 
Open the app and create an account by providing your email and a secure password.
 
A verification email will be sent—click the link in the email to verify your account.
 
-Login
 
After verifying, return to the app and log in using your credentials.
 
-Set Up Your Profile
 
Go to your user profile to customize settings, and manage categories.
 
-Create a Budget
 
Set your minimum and maximum savings or spending goals from the home page.
 
-Add a Transaction
 
Click “Add Transaction” to record income or expenses.
 
Choose or create a category, add an amount, and optionally take/upload a receipt photo.
 
-Manage Categories
 
Use the Category Manager to create or delete your personal transaction categories.
 
Assign icons to categories for easier visual tracking.
 
-View Transactions
 
Tap “View All Transactions” to see a list of past transactions.
 
Click on any transaction to view details or delete it.
 
-Track Your Spending
 
Go to the Analytics Page to view charts, graphs, and a pie chart showing your financial data.
 
Monitor how your expenses align with your set goals.
 
-Earn Achievements
 
Perform tasks like saving money or staying under budget to unlock achievements.
 
View your progress on the Achievements Page.
 
-Add Friends
 
Invite or add friends to your profile to see their achievements and stay motivated together.
 
Play the Bubble Popping Game
 
Take a break and play the built-in game to relax while staying within the app.
 
-Give Feedback
 
Visit the User Feedback page to share suggestions, report bugs, or rate your experience.
 
-Log Out When Done
 
Use the logout option in your profile to securely exit the app.

---------------------------------------------------------------------------------------------------------------------------------------------------------
Innovative features

Achievements features- This is a page dedicated to show a user all its achievements gained from our app. You can access this page by navigating to the user profile page and click the achievements button. This feature work when a user finds hidden easter eggs/ gems within the app. They are in random pages that a user uses. Even if a user enters a specific number on the calculator, it gives a scary jump scare. It also guides the user on how to use the app.

Bubble game feature- This is the special gem in the app or easter egg. When a user taps rapidly on the logo on the home page, the bubble game pops up and you have to try and pop as much bubbles as you can within the time limit. This is also an achievement.

Adding Friends feature- This feature allows a user to add their friends or users to view there achievements. Just go to the user profile page and click friends, and here you can type in your friends code or if your friend sent you a friend request, just click the button to see friend request and you can accept or decline it. After accepting it you can now view there achievements.

Suggestion/tip or warning feature- This feature is responsible for suggesting a user to keep in the green or to keep the expense below the income status. This feature is tested by when a user creates a transaction and the expense is higher than the income when they press create, the message will pop up and give it stats on how much above the income it is.


---------------------------------------------------------------------------------------------------------------------------------------------------------
References:
Foxandroid. 2025. How to Make Calculator App in Android Studio || Calculator App Tutorial || 2022, Youtube [Online], Avaiable at: https://www.youtube.com/watch?v=-VsatCUSxek [Accessed 30 April 2025]

gotten from Fahlteich, P. 2025. SkeletonLayout: Skeleton view pattern for Android, Github. [Online]. Available at: https://github.com/Faltenreich/SkeletonLayout [Accessed 29 May 2025]

Pie Chart inspired by CodingWithMitch (2016) Creating a Simple Pie Chart in Android Studio, YouTube video, [Online]. Available at: https://www.youtube.com/watch?v=8BcTXbwDGbg [Accessed: 29 May 2025].

Bar graph inspired by KGP Talkie (2017) MPAndroidChart Tutorial Better Than Android GraphView 5- Beautiful Multiple Bar Chart, YouTube video, [Online]. Available at: https://www.youtube.com/watch?v=_uQrJ0TkZlc [Accessed: 29 May 2025].

Induct Automation, 2020 .  Code review: Modifying RGB by Manipulating HSV colors. [Online].Available at: https://forum.inductiveautomation.com/t/code-review-modifying-rgb-by-manipulating-hsv-colors/58295 [Accessed 29 May 2025]

Kumar, M. 2025. Goodbye to onBackPressed(): A Guide to Modern Back press Handling in Android [Online]. Available at: https://www.youtube.com/watch?v=dxqD8FqMPRs [Accessed 30 May 2025]

Stack Overflow, ow to underline text of button in android, n.d., [Online]. Available at: https://stackoverflow.com/questions/31718707/how-to-underline-text-of-button-in-android [Accessed May 20]

Firebase. n.d. Get started with firebase authentication on android. [Online]. Available at: https://firebase.google.com/docs/auth/android/start [Accessed 20 May 2025]

Biraj Zalavadia. 2014. Regular expressions in android for password field, 22 April 2014. [Online]. Available at: https://stackoverflow.com/questions/23214434/regular-expression-in-android-for-password-field [Accessed 20 May 2025]

Blackbelt. 2015. How to hide app name from title bar in android, StackOverflow. 13 May 2015. [Online]. Available at: https://stackoverflow.com/questions/30216233/how-to-hide-app-name-from-title-bar-in-android [Accessed 22 May 2025]

Biraj Zalavadia. 2014. Regular expressions in android for password field, 22 April 2014. [Online]. Available at: https://stackoverflow.com/questions/23214434/regular-expression-in-android-for-password-field [Accessed 20 May 2025]
 
Foxandroid, 2023, ROOM Database - #2 Read and Write Data | Android Studio Tutorial | Kotlin | 2023, Youtube, [Online]. Available at https://www.youtube.com/watch?v=oeRF5mSaM4Q&t=152s [Accessed 1 May 2025]

Android Mate, 2023, How to create an APK file in Android Studio, Youtube, [Online] Available at: https://www.youtube.com/watch?v=3FujlwQoKuk [Accessed at: 05 May 2025]

Audio Resources:
https://www.youtube.com/watch?v=h2eBoIoq5vw
https://www.youtube.com/watch?v=qZVCaD6CCE0
https://www.youtube.com/watch?v=LMWHcTqUcco
https://www.youtube.com/watch?v=AFYWsbUSasw
 
Icons Resource:
https://www.flaticon.com/
 
ChatGPT reference:
https://chatgpt.com/share/68420050-0770-800b-a938-164f64c5e017
 - YouTube
Enjoy the videos and music you love, upload original content, and share it all with friends, family, and the world on YouTube.
