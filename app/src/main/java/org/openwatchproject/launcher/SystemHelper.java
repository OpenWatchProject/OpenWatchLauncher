package org.openwatchproject.launcher;

import android.content.Context;
import android.provider.Settings;

public class SystemHelper {
    public static int getSteps(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "today_steps", 0);
    }

    public static int getTargetSteps(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "step_target_count", 8000);
    }

    public static int getHeartRate(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "heart_rate", 0);
    }

    public static String getDistance(Context context) {
        String distance = Settings.System.getString(context.getContentResolver(), "today_distance");
        return distance == null ? "0.0" : distance;
    }

    public static float getTargetDistance(Context context) {
        return Settings.System.getFloat(context.getContentResolver(), "today_distancetarget", 5.0f);
    }

    public static String getCalories(Context context) {
        String calories = Settings.System.getString(context.getContentResolver(), "today_calories");
        return calories == null ? "0" : calories;
    }

    public static int getTargetCalories(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "today_caloriestarget", 300);
    }

    public static int getWeatherTemp(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "WeatherTemp", 23);
    }

    public static int getWeatherIcon(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "WeatherIcon", 10);
    }
}
