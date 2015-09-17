package it.angelic.soulissclient.net;

public class Utils {

    public static Float celsiusToFahrenheit(float in) {
        return Float.valueOf((9.0f / 5.0f) * in + 32);
    }

    public static Float fahrenheitToCelsius(float fahrenheit) {
        return Float.valueOf((5.0f / 9.0f) * (fahrenheit - 32));
    }
}
