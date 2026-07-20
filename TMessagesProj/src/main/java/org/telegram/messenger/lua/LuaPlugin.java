package org.telegram.messenger.lua;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Represents a single Lua plugin
 * Handles script loading, execution, and lifecycle
 */
public class LuaPlugin {
    private final String name;
    private final File scriptFile;
    private final Globals globals;
    private LuaValue pluginTable;
    private LuaPluginAPI api;

    public LuaPlugin(String name, File scriptFile, Globals globals) {
        this.name = name;
        this.scriptFile = scriptFile;
        this.globals = globals;
    }

    /**
     * Initialize the plugin
     * Load the Lua script and setup API
     */
    public void init() throws IOException {
        if (globals == null) {
            return;
        }
        if (!scriptFile.exists()) {
            throw new IOException("Plugin script not found: " + scriptFile.getAbsolutePath());
        }

        String scriptContent = new String(Files.readAllBytes(scriptFile.toPath()));

        // Setup plugin API
        api = new LuaPluginAPI();
        globals.set("PluginAPI", api.asLuaValue());

        // Load and execute the plugin script
        try {
            pluginTable = globals.load(scriptContent).call();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize plugin: " + name, e);
        }
    }

    /**
     * Call a function in the plugin
     */
    public LuaValue callFunction(String functionName, LuaValue... args) {
        if (pluginTable == null || !pluginTable.istable()) {
            throw new IllegalStateException("Plugin not properly initialized: " + name);
        }

        LuaValue function = pluginTable.get(functionName);
        if (function == null || !function.isfunction()) {
            throw new IllegalArgumentException("Function not found in plugin: " + functionName);
        }

        try {
            if (args.length == 0) {
                return function.call();
            } else {
                return function.invoke(args).arg1();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error calling function " + functionName + " in plugin " + name, e);
        }
    }

    /**
     * Get plugin metadata
     */
    public String getMetadata(String key) {
        if (pluginTable == null || !pluginTable.istable()) {
            return null;
        }
        LuaValue value = pluginTable.get(key);
        return value.isstring() ? value.tojstring() : null;
    }

    /**
     * Cleanup plugin resources
     */
    public void cleanup() {
        if (pluginTable != null && pluginTable.istable()) {
            LuaValue onCleanup = pluginTable.get("onCleanup");
            if (onCleanup != null && onCleanup.isfunction()) {
                try {
                    onCleanup.call();
                } catch (Exception e) {
                    // Log but don't throw during cleanup
                    e.printStackTrace();
                }
            }
        }
        pluginTable = null;
    }

    public String getName() {
        return name;
    }

    public File getScriptFile() {
        return scriptFile;
    }
}
