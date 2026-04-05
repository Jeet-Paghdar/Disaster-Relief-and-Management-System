package com.disasterrelief.utils;

import java.util.regex.Pattern;

public class ValidationUtils {

    // Regex patterns
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]{2,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");
    private static final Pattern PINCODE_PATTERN = Pattern.compile("^\\d{6}$");

    /**
     * Checks if a string is a valid name (Alpha only, 2-50 chars).
     */
    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Checks if a string is a valid 10-digit phone number.
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Checks if a string is a valid email address.
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Checks if a string is a valid 6-digit pincode.
     */
    public static boolean isValidPincode(String pincode) {
        return pincode != null && PINCODE_PATTERN.matcher(pincode.trim()).matches();
    }

    /**
     * Checks if a string is a valid positive integer.
     */
    public static boolean isValidNumber(String number) {
        if (number == null || number.trim().isEmpty()) return false;
        try {
            int val = Integer.parseInt(number.trim());
            return val >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if a string is not null or empty.
     */
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }
}
