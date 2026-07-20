package org.telegram.messenger.lua;

import android.content.Context;

import org.telegram.messenger.FileLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PluginManager {

    private static final String TAG = "LuaPluginManager";
    private static volatile PluginManager instance;

    private final List<LuaPlugin> plugins = new ArrayList<>();
    private org.telegram.messenger.PluginManager metadataManager;

    private PluginManager() {
    }

    public static PluginManager getInstance(Context context) {
        PluginManager localInstance = instance;
        if (localInstance == null) {
            synchronized (PluginManager.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new PluginManager();
                }
            }
        }
        return localInstance;
    }

    public void loadAllPlugins() {
        plugins.clear();
        try {
            metadataManager = org.telegram.messenger.PluginManager.getInstance();
            for (org.telegram.messenger.PluginManager.PluginInfo info : metadataManager.getPlugins()) {
                File pluginsDir = new File(org.telegram.messenger.ApplicationLoader.getFilesDirFixed(), "jellogram_plugins");
                File scriptFile = new File(pluginsDir, info.id + ".jello");
                LuaPlugin plugin;
                if (scriptFile.exists()) {
                    try {
                        org.luaj.vm2.Globals globals = org.luaj.vm2.lib.jse.JsePlatform.standardGlobals();
                        plugin = new LuaPlugin(info.id, scriptFile, globals);
                        plugin.init();
                    } catch (Exception e) {
                        plugin = new LuaPlugin(info.id, scriptFile, null);
                    }
                } else {
                    plugin = new LuaPlugin(info.id, null, null);
                }
                plugins.add(plugin);
            }
            FileLog.d(TAG + ": Loaded " + plugins.size() + " plugins");
        } catch (Exception e) {
            FileLog.e(TAG + ": Failed to load plugins", e);
        }
    }

    public List<LuaPlugin> getAllPlugins() {
        return new ArrayList<>(plugins);
    }

    public void shutdown() {
        for (LuaPlugin plugin : plugins) {
            try {
                plugin.cleanup();
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        plugins.clear();
    }
}
