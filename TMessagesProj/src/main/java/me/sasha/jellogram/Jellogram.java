package me.sasha.jellogram;

import android.app.Application;
import android.util.Log;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.JellogramSettings;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.PluginManager;

public class Jellogram {

    private static boolean initialized;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            if (ApplicationLoader.applicationContext != null) {
                resetThemeOverrides();
                NotificationCenter.getGlobalInstance().addObserver((id, account, args) -> {
                    if (id == NotificationCenter.jellogramSettingsChanged) {
                        resetThemeOverrides();
                    }
                }, NotificationCenter.jellogramSettingsChanged);
            }
        } catch (Throwable t) {
            Log.e("Jellogram", "init failed", t);
        }
    }

    public static void resetThemeOverrides() {
        try {
            PluginManager.getInstance().reload();
        } catch (Throwable t) {
            Log.e("Jellogram", "reset theme failed", t);
        }
    }
}
