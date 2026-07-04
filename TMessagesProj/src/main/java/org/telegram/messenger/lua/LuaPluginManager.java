package org.telegram.messenger.lua;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Lua Plugin System Manager
 * Manages loading, executing, and lifecycle of Lua plugins
 */
public class LuaPluginManager {
    private final Globals globals;
    private final Map<String, LuaPlugin> plugins = new HashMap<>();
    private final String pluginsDir;
    private static LuaPluginManager instance;

    private LuaPluginManager(String pluginsDir) {
        this.pluginsDir = pluginsDir;
        this.globals = JsePlatform.standardGlobals();
    }

    /**
     * Get singleton instance
     */
    public static synchronized LuaPluginManager getInstance(String pluginsDir) {
        if (instance == null) {
            instance = new LuaPluginManager(pluginsDir);
        }
        return instance;
    }

    /**
     * Load a Lua plugin from file
     */
    public void loadPlugin(String pluginName) throws Exception {
        File pluginFile = new File(pluginsDir, pluginName + ".lua");
        if (!pluginFile.exists()) {
            throw new IllegalArgumentException("Plugin not found: " + pluginName);
        }

        try {
            LuaPlugin plugin = new LuaPlugin(pluginName, pluginFile, globals);
            plugin.init();
            plugins.put(pluginName, plugin);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load plugin: " + pluginName, e);
        }
    }

    /**
     * Execute a plugin function
     */
    public LuaValue executePluginFunction(String pluginName, String functionName, LuaValue... args) {
        LuaPlugin plugin = plugins.get(pluginName);
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin not loaded: " + pluginName);
        }
        return plugin.callFunction(functionName, args);
    }

    /**
     * Unload a plugin and cleanup
     */
    public void unloadPlugin(String pluginName) {
        LuaPlugin plugin = plugins.remove(pluginName);
        if (plugin != null) {
            plugin.cleanup();
        }
    }

    /**
     * Get loaded plugin
     */
    public LuaPlugin getPlugin(String pluginName) {
        return plugins.get(pluginName);
    }

    /**
     * Check if plugin is loaded
     */
    public boolean isPluginLoaded(String pluginName) {
        return plugins.containsKey(pluginName);
    }

    /**
     * Unload all plugins and cleanup
     */
    public void shutdown() {
        for (LuaPlugin plugin : plugins.values()) {
            plugin.cleanup();
        }
        plugins.clear();
    }
}
