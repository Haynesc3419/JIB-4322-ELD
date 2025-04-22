# Installation Guide for Electronic Logging Device (ELD)

This guide provides step-by-step instructions for setting up and running the Electronic Logging Device (ELD) application for Michelin Connected Fleet.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Backend Setup](#backend-setup)
- [Android App Setup](#android-app-setup)
- [Running the Application](#running-the-application)
- [Troubleshooting](#troubleshooting)

## Prerequisites

### For Backend Development
- **Java Development Kit (JDK)**: Version 21 or higher
  - Download from: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)
- **Gradle**: Version 8.0 or higher
  - Download from: [Gradle](https://gradle.org/install/)
- **MongoDB**: Version 6.0 or higher
  - Download from: [MongoDB](https://www.mongodb.com/try/download/community)
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code

### For Android App Development
- **Android Studio**: Latest version
  - Download from: [Android Studio](https://developer.android.com/studio)
- **Android SDK**: API level 33 (Android 13) or higher
- **Android Emulator** or physical Android device running Android 8.0 or higher, We use Pixel Tablet API 34 downloaded via Android Studio

## Backend Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/your-username/JIB-4322-ELD.git
   cd JIB-4322-ELD/backend
   ```

2. **Configure MongoDB Connection**
   - Create a file named `secrets.properties` in the `src/main/resources` directory
   - Add the following content (replace with your MongoDB connection string):
     ```
     spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/eld_data?retryWrites=true&w=majority
     ```

3. **Build the Backend**
   ```bash
   ./gradlew build
   ```

4. **Run the Backend**
   ```bash
   ./gradlew bootRun
   ```
   The backend server should start on `http://localhost:8080`

## Android App Setup

1. **Open the Android Project**
   - Launch Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to and select the `android` directory in the cloned repository

2. **Configure API Endpoint**
   - Open `app/src/main/java/com/michelin/connectedfleet/eld/network/ApiClient.java`
   - Update the `BASE_URL` constant to point to your backend server:
     ```java
     private static final String BASE_URL = "http://your-server-ip:8080/";
     ```

3. **Build the Android App**
   - Click on "Build" > "Make Project" in Android Studio
   - Wait for the build to complete

4. **Run the Android App**
   - Connect an Android device or start an emulator
   - Click on "Run" > "Run 'app'" in Android Studio
   - Select your target device and click "OK"

## Running the Application

### Backend
1. Ensure MongoDB is running
2. Navigate to the backend directory
3. Run `./gradlew bootRun`
4. Verify the server is running by accessing `http://localhost:8080/health`

### Android App
1. Launch the app on your Android device
2. Log in with your credentials
3. The dashboard should display your current status and hours remaining

## Troubleshooting


