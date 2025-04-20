package com.michelin.connectedfleet.eld.ui.data.util;

/**
 * Constants for ELD compliance and notifications
 */
public class EldConstants {
    // Speed limits (in MPH as per FMCSA standards)
    public static final double SPEED_WARNING_THRESHOLD_MPH = 65.0;  // Warning at 65 mph
    public static final double SPEED_VIOLATION_THRESHOLD_MPH = 70.0; // Violation at 70 mph

    // Distance limits (in miles)
    public static final double MAX_CONTINUOUS_DRIVING_MILES = 500.0; // Maximum continuous driving distance
    public static final double DISTANCE_WARNING_THRESHOLD = 450.0;   // Warning at 450 miles

    // Time limits (in hours)
    public static final int MAX_DRIVING_HOURS = 11;     // Maximum driving hours in a day
    public static final int MAX_ON_DUTY_HOURS = 14;     // Maximum on-duty hours
    public static final int REQUIRED_REST_HOURS = 10;   // Required consecutive rest hours
    
    // Break requirements
    public static final int BREAK_REQUIRED_AFTER_HOURS = 8; // Break required after 8 hours
    public static final int MINIMUM_BREAK_MINUTES = 30;     // Minimum break duration in minutes

    private EldConstants() {
        // Private constructor to prevent instantiation
    }
} 