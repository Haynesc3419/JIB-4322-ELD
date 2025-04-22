# Electronic Logging Device for Michelin Connected Fleet

## Description
This repository contains the Electronic Logging Device (ELD) application built for Michelin Connected Fleet. The purpose of the ELD is to provide fleet managers with the data to track the current status of the drivers in their fleet. The ELD ensures drivers are tasked within the requirements of FMCSA regulations and are paid accordingly. The application tracks what the driver is currently doing, whether driving, taking a break, etc., and keeps track of these logs to ensure they work within FMCSA hourly regulations.

## Technology Stack
- **Frontend**: Android Studio/Java
- **Backend**: Spring Boot, MongoDB, Java
- **Database**: MongoDB Atlas

## Features
- **Authentication**: Secure login system for drivers and fleet managers
- **Dashboard**: Real-time display of hours remaining in each sector of FMCSA regulations (driving, break, total per day)
- **Status Management**: Ability to change and log driver status (Driving, On Break, Personal Conveyance, Loading, Unloading)
- **Log Verification**: Submit change requests for incorrect logs and verify logs before submission
- **Notifications**: Receive alerts when approaching daily driving limits to safely plan rest stops
- **Accessibility**: Colorblind-friendly interface with color-coded indicators for hours remaining
- **Unit Conversion**: Switch between metric and imperial units for distance and speed
- **Theme Support**: Toggle between dark and light mode to reduce eye strain during night driving
- **Responsive Design**: Support for both phone and tablet views with horizontal orientation

## Known Issues
- Horizontal display issues in tablet view on certain resource files in Android Studio
- Occasional connectivity issues with the backend service

## Documentation
- [Installation Guide](INSTALLATION.md): Step-by-step instructions for setting up and running the application
- [Design Document](DESIGN.md): Detailed technical design and architecture of the system

## Getting Started
For installation and setup instructions, please refer to our [Installation Guide](INSTALLATION.md).

## License
This project is proprietary and confidential. Unauthorized copying, distribution, or use is strictly prohibited.

