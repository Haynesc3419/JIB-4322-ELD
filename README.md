# Electronic Logging Device for Michelin Connected Fleet

# Description
This repository outlines the product built for Michelin Connected Fleet, Electronic Logging Device (ELD). The purpose of the ELD is to provide fleet managers with the data to track the current status of the drivers in their fleet. The ELD will ensure the drivers are tasked within the requirements of FMSCA regulations and are paid accordingly. The application tracks what the driver is currently doing, whether driving, taking a break, etc. It keeps track of these logs to ensure they work within FMSCA hourly regulations.

# Technology
Frontend - Android Studio/Java \
Backend - MongoDB, Java

# Release Notes
## v0.3.0
### Features
* Support a horizontal view for use on tablet and phone.  
* Change between dark and light mode so that to accommodate driving at night or during the day and reduce eye strain. 
* Update my time zone automatically based on location so that logs are always accurate (might be out of scope or unnecessary – need to talk to client and maybe change). 
* Switch between metric and imperial units to view distance and speed in preferred format. 
### Bug Fixes
* Fixed bug where app could not call backend.
### Known Issues
* Bug with screen rotation.


## v0.2.0
### Features
* Switch between different driving statuses (personal conveyance, sleeper berth, etc) to log activities accurately. 
* View and filter my historic driving logs to ensure correctness. 
* Add colorblind accommodating features on the driver interface so colorblind drivers can still gather all relevant information (including hours remaining, warnings, etc). 
*As a driver, I want my app to be aware of legal restrictions and allow me to review them. 
### Bug Fixes
* Fixed bug where app would not load due to MongoDB incompatabilities.
### Known Issues
* App cannot call backend currently.

## v0.1.0
### Features
* Log in using a username and password to allow for secure account access. 
* See current driving status to verify compliance with regulations. 
* Display the hours remaining for the day so the driver can plan his or her schedule effectively. 
* Display my remaining hours using color codes (blue, yellow, red) To easily identify when the driver is approaching or exceeding limits.
### Bug Fixes
* Fixed bug where app would not load due to MongoDB incompatabilities.
### Known Issues
* None to our knowledge currently

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

