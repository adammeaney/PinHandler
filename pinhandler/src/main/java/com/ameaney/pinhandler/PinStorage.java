package com.ameaney.pinhandler;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PinStorage
{
    private final String PIN_KEY = "PinKey";

    public boolean confirmPin(Activity activity, String pin)
    {
        if (pin == null || pin.isEmpty())
        {
            return false;
        }

        SharedPreferences preferences = activity.getSharedPreferences(PIN_KEY, Context.MODE_PRIVATE);
        String token = preferences.getString(PIN_KEY, "");

        if (token.isEmpty())
        {
            return false;
        }

        String hash = MD5Encode(pin);

        return token.equals(hash);
    }

    public boolean setPin(Activity activity, String pin)
    {
        if (pin == null || pin.isEmpty())
        {
            return false;
        }

        SharedPreferences preferences = activity.getSharedPreferences(PIN_KEY, Context.MODE_PRIVATE);
        return preferences.edit().putString(PIN_KEY, MD5Encode(pin)).commit();
    }

    public boolean hasPin(Activity activity)
    {
        SharedPreferences preferences = activity.getSharedPreferences(PIN_KEY, Context.MODE_PRIVATE);

        String token = preferences.getString(PIN_KEY, "");

        return !token.isEmpty();
    }

    public String MD5Encode(String pin)
    {
        try
        {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(pin.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuilder MD5Hash = new StringBuilder();
            for (byte letter : messageDigest)
            {
                String hexString = Integer.toHexString(0xFF & letter);
                while (hexString.length() < 2)
                {
                    hexString = "0" + hexString;
                }
                MD5Hash.append(hexString);
            }
            return MD5Hash.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return "";
        }
    }
}
