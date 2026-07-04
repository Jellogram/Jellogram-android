package org.telegram.messenger.lua;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.telegram.messenger.FileLog;
import java.util.HashMap;
import java.util.Map;

/**
 * API exposed to Lua plugins
 * Provides core functionality for plugins to interact with the app
 */
public class LuaPluginAPI {
    private final Map<String, PluginAPIFunction> functions = new HashMap<>();

    public LuaPluginAPI() {
        registerBuiltInFunctions();
    }

    /**
     * Register built-in API functions available to plugins
     */
    private void registerBuiltInFunctions() {
        // Logging function
        functions.put("log", new PluginAPIFunction() {
            @Override
            public LuaValue call(LuaValue... args) {
                StringBuilder message = new StringBuilder();
                for (LuaValue arg : args) {
                    if (message.length() > 0) message.append(" ");
                    message.append(arg.tojstring());
                }
                FileLog.d("[LuaPlugin] " + message.toString());
                return LuaValue.NIL;
            }
        });

        // Error logging function
        functions.put("logError", new PluginAPIFunction() {
            @Override
            public LuaValue call(LuaValue... args) {
                StringBuilder message = new StringBuilder();
                for (LuaValue arg : args) {
                    if (message.length() > 0) message.append(" ");
                    message.append(arg.tojstring());
                }
                FileLog.e("[LuaPlugin] ERROR: " + message.toString());
                return LuaValue.NIL;
            }
        });
    }

    /**
     * Register a custom API function
     */
    public void registerFunction(String name, PluginAPIFunction function) {
        functions.put(name, function);
    }

    /**
     * Get a function by name
     */
    public PluginAPIFunction getFunction(String name) {
        return functions.get(name);
    }

    /**
     * Convert to Lua value for use in scripts
     */
    public LuaValue asLuaValue() {
        return CoerceJavaToLua.coerce(this);
    }

    /**
     * Base interface for API functions
     */
    public interface PluginAPIFunction {
        LuaValue call(LuaValue... args);
    }
}
