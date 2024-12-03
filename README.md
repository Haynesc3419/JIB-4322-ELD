# Electronic Logging Device for Michelin Connected Fleet

# Description
This repository outlines the product built for Michelin Connected Fleet, Electronic Logging Device (ELD). The purpose of the ELD is to provide fleet managers with the data to track the current status of the drivers in their fleet. The ELD will ensure the drivers are tasked within the requirements of FMSCA regulations and are paid accordingly. The application tracks what the driver is currently doing, whether driving, taking a break, etc. It keeps track of these logs to ensure they work within FMSCA hourly regulations.

# Technology
Frontend - Android Studio/Java \
Backend - MongoDB, Java

# Release Notes
## v0.0.0
### Features
This release implemented three main pages, authentication, dashboard, and status change. 
* Authentication
  * Temporary page utilizing a username and password to log into the application and populate the information
* Dashboard
  * Showcases hours remaining in each sector of FMCSA regulations hours like driving, break, and total per day
  * Has a feature to change the current status of the driver on the job
* Status Change
  * Feature to change the status for fleet managers to see and log the proper hours
  * Options to change are Driving, On Break, Personal Conveyance, Loading, Unloading

