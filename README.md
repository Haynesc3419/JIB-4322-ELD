# Electronic Logging Device for Michelin Connected Fleet
First Release (Version 1.0)

## Description
This repository contains the Electronic Logging Device (ELD) application built for Michelin Connected Fleet. The purpose of the ELD is to provide fleet managers with the data to track the current status of the drivers in their fleet. The ELD ensures drivers are tasked within the requirements of FMCSA regulations and are paid accordingly. The application tracks what the driver is currently doing, such as driving, taking a break, etc.; and keeps track of these logs to ensure they work within FMCSA hourly regulations.

## Technology Stack
- **Frontend**: Android Studio/Java (MVC model)
- **Backend**: Spring Boot, MongoDB, Java
- **Database**: MongoDB Atlas

## Features
### Authentication and Security
- Secure login system with username/password authentication
- Session management with automatic login retention
- Secure logout functionality

### Driver Status Management
- Real-time status tracking (Driving, On Break, Personal Conveyance, etc.)
- Color-coded display of remaining hours (blue, yellow, red)
- Historical log viewing and filtering capabilities
- Log verification and submission system
- Change request system for incorrect logs

### Hours of Service (HOS) Tracking
- Real-time display of remaining daily driving hours
- Automatic notifications for approaching driving limits
- End-of-shift log verification reminders
- Access to legal restrictions and requirements

### User Experience
- Tablet-optimized interface with horizontal view support
- Dark and light mode for different driving conditions
- Accessibility features for colorblind drivers
- Support for both metric and imperial units
- Automatic time zone updates based on location
- Confirmation notifications for log submissions

## Bug Fixes
The following issues were identified and fixed during development:
- Screen rotation issues that caused data loss
- MongoDB connection issues in the backend
- Hard-coded metric to imperial unit conversions
- Authentication token expiration handling
- Log submission validation
- UI layout issues in tablet mode

## Known Issues and Missing Features
### Current Issues
- Full functionality only tested and verified on Pixel Tablet API 34 emulator; other devices may experience compatibility issues
- Some layouts are only fully operational in horizontal mode
- Unexpected automatic logouts occur occasionally, and are not always obvious
- Horizontal display issues in tablet view on certain resource files in Android Studio
- Occasional connectivity issues with the backend service

### Planned Features Not Included
- Bluetooth integration with vehicle for real-time data collection (postponed due to hardware availability)
- Automatic time zone updates based on location (partially implemented)
- Integration with external fleet management systems

## Documentation
- [Installation Guide](INSTALLATION.md): Step-by-step instructions for setting up and running the application
- [Design Document](DesignDocument.pdf): Detailed technical design and architecture of the system

## Getting Started
For installation and setup instructions, please refer to our [Installation Guide](INSTALLATION.md).

## License
This project is proprietary and confidential. Unauthorized copying, distribution, or use is strictly prohibited.
