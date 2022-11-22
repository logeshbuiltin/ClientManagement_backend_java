package com.razertech.clientmanagement.service;

import java.util.Random;

public class OtpGeneratorService {
    public Integer OTP(int len)
    {

        String otpData = "";
        // Using numeric values
        String numbers = "0123456789";

        // Using random method
        Random random_method = new Random();

        char[] otp = new char[len];

        for (int i = 0; i < len; i++)
        {
            // Use of charAt() method : to get character value
            // Use of nextInt() as it is scanning the value as int
            otp[i] = numbers.charAt(random_method.nextInt(numbers.length()));
            otpData = otpData + otp[i];
        }
        int otpInt = Integer.parseInt(otpData);
        return otpInt;
    }
}
