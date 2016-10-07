package fi.metropolia.chatclient.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Settings {
    private static Context staticContext;

    public static void initialize(@NonNull Context context) {
        staticContext = context;
    }

    public static void put(@NonNull String key, @NonNull String value) {
        defaultPreferences().edit().putString(key, value).apply();
    }

    @Nullable
    public static String get(@NonNull String key) {
        return defaultPreferences().getString(key, null);
    }

    public static void remove(@NonNull String key) {
        defaultPreferences().edit().remove(key).apply();
    }

    private static SharedPreferences defaultPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(staticContext);
    }
}
