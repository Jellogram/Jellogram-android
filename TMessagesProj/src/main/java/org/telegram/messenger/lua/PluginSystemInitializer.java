package org.telegram.messenger.lua;

import android.content.Context;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

/**
 * Plugin System Initializer
 * Called from Application.onCreate() or MessagesController
 */
public class PluginSystemInitializer {
    private static final String TAG = "PluginSystemInit";
    private static boolean initialized = false;
    private static PluginManager pluginManager;

    /**
     * Initialize plugin system
     */
    public static void initialize() {
        if (initialized) {
            return;
        }

        try {
            Context context = ApplicationLoader.applicationContext;
            pluginManager = PluginManager.getInstance(context);
            pluginManager.loadAllPlugins();
            initialized = true;
            FileLog.d(TAG + ": Plugin system initialized successfully");
        } catch (Exception e) {
            FileLog.e(TAG + ": Failed to initialize plugin system", e);
        }
    }

    /**
     * Get plugin manager
     */
    public static PluginManager getPluginManager() {
        if (!initialized) {
            initialize();
        }
        return pluginManager;
    }

    /**
     * Shutdown plugin system
     */
    public static void shutdown() {
        if (pluginManager != null) {
            pluginManager.shutdown();
            initialized = false;
        }
    }

    /**
     * Check if system is initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }
}