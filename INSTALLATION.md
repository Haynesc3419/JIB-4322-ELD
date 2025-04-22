# Installation Guide for Electronic Logging Device (ELD)

This guide provides step-by-step instructions for setting up and running the Electronic Logging Device (ELD) application for Michelin Connected Fleet.

## Table of Contents
- [Pre-requisites](#pre-requisites)
- [Dependent Libraries](#dependent-libraries)
- [Download Instructions](#download-instructions)
- [Build Instructions](#build-instructions)
- [Installation Steps](#installation-steps)
- [Run Instructions](#run-instructions)
- [Troubleshooting](#troubleshooting)

## Pre-requisites

### Hardware Requirements
- Computer with at least 8GB RAM
- Android device or emulator:
  - Recommended: Pixel Tablet API 34 emulator (Android 14.0 "UpsideDownCake", arm64)
  - Alternative: Physical device running Android 8.0 or higher

### Software Requirements
- **Java Development Kit (JDK)**: Version 21 or higher
  - Download from: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)
- **Android Studio**: Latest version
  - Download from: [Android Studio](https://developer.android.com/studio)
- **Gradle**: Version 8.0 or higher
  - Download from: [Gradle](https://gradle.org/install/)
  - Can also be installed automatically using Android Studio
- **MongoDB**: Version 6.0 or higher
  - Download from: [MongoDB](https://www.mongodb.com/try/download/community)

## Dependent Libraries

The following libraries are automatically managed by Gradle and included in the build:

### Backend Dependencies
- Spring Boot 3.4.2
- Spring Data MongoDB
- Spring Security
- JWT (JSON Web Tokens)
- Lombok
- JUnit and Mockito

### Android App Dependencies
- Retrofit for API communication
- Gson for JSON parsing
- Material Design Components
- AndroidX libraries
- JUnit and Espresso for testing

## Download Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/JIB-4322-ELD.git
   cd JIB-4322-ELD
   ```

## Build Instructions

### Backend Build
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Build the backend:
   ```bash
   ./gradlew build
   ```

### Android App Build
1. Open the Android project in Android Studio:
   - Launch Android Studio
   - Open the `android` directory from the cloned repository

2. Build the Android app:
   - Click on "Build" > "Make Project" in Android Studio
  
Note: Alternatively, `gradle` can be used directly on the command line to build the Android App.
This is not recommended, so instructions are not provided.

## Installation Steps

### Backend Installation
1. Create a file named `secrets.properties` in the `backend/src/main/resources` directory containing your MongoDB connection string:
   ```
   spring.data.mongodb.uri=mongodb+srv://<username>:<password>@<cluster.mongodb.net>/eld_data?retryWrites=true&w=majority
   ```

2. Ensure MongoDB is running on your system or that you have proper access to a cloud instance.

### Android App Installation
1. Configure the API endpoint:
   - Open `app/src/main/java/com/michelin/connectedfleet/eld/network/ApiClient.java`
   - Update the `BASE_URL` constant to point to your backend server:
     ```java
     private static final String BASE_URL = "http://your-server-ip:8080/";
     ```

## Run Instructions

### Backend
1. Run the backend:
   ```bash
   ./gradlew bootRun
   ```
   The backend server will start on `http://localhost:8080`

### Android App
1. Run the Android app:
   - In Android Studio, create and start the Pixel Tablet API 34 emulator
     - Go to Tools > Device Manager
     - Click "Create Device"
     - Select "Pixel Tablet" and use API 34 (Android 14.0)
   - Click on "Run" > "Run 'app'" in Android Studio
   - Select the Pixel Tablet API 34 emulator and click "OK"

## Troubleshooting

### Common Issues and Solutions

| Issue | Solution |
|-------|----------|
| Backend fails to start | Check MongoDB connection in `secrets.properties` |
| Backend errors out | If using MongoDB Atlas (Cloud) ensure your IP is whitelisted |
| App cannot connect to backend | Verify the `BASE_URL` in `ApiClient.java` |
| Build failures | Run `./gradlew clean` and try again |
| App crashes on launch | Check logcat output in Android Studio |


