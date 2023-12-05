# SEGP-AntiSaccadeTest
## Anti-Saccade-Test for android studio  
To Import This Repository into a Local Repository in Android Studio Project.  
1) Go to File -> New -> Import Project from Version Control.  
2) Wait for gradle to build.  
3) Go to File -> Project Structure -> Modules and check if there is a module named opencv, if there is, delete it.  
4) Download OpenCV for android from https://opencv.org/releases/ 
5) In project. Go to File -> New -> Import Module -> Find 'OpenCV-android-sdk' in dir -> sdk  
6) Name the Module name as :opencv  
7) In your Project, go to opencv file -> build.gradle  
8) Change compileSdkVersion 33, minSdkVersion = 24, targetSdkVersion 33  
9) Add the following line above apply plugin: 'com.android.library':   
plugins {  
    id 'org.jetbrains.kotlin.android' version '1.7.10'  
}  
10) Click on 'Sync Project with Gradle'  
11) Run The App with Emulator or Device
