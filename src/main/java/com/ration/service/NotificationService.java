package com.ration.service;
/**
 * Service to handle SMS/Email alerts to citizens.

public class NotificationService {
    
    public static void sendTransactionAlert(String mobileNumber, String itemName, double qty) {
        // In a real scenario, integrate with an API like Twilio or a Govt SMS Gateway
        String message = "Digital Ration Alert: " + qty + "kg of " + itemName + 
                         " has been issued to your card. If this wasn't you, contact helpline 1967.";
        
        System.out.println("Sending SMS to " + mobileNumber + ": " + message);
        // Gateway code here...
    }
} */