package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class PluginManager {

    private static volatile PluginManager Instance = null;

    public static PluginManager getInstance() {
        PluginManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (PluginManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new PluginManager();
                }
            }
        }
        return localInstance;
    }

    public static class PluginInfo {
        public String id;
        public String title;
        public String description;
        public String photoUrl;
        public boolean enabled;
    }

    private final HashMap<String, PluginInfo> plugins = new HashMap<>();
    private final ArrayList<PluginInfo> pluginsList = new ArrayList<>();

    public PluginManager() {
        loadPlugins();
    }

    private void loadPlugins() {
        plugins.clear();
        pluginsList.clear();
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("jellogram_plugins", Context.MODE_PRIVATE);
        String pluginsJson = preferences.getString("plugins", "{}");
        try {
            JSONObject json = new JSONObject(pluginsJson);
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject pluginJson = json.getJSONObject(key);
                PluginInfo info = new PluginInfo();
                info.id = key;
                info.title = pluginJson.optString("title", key);
                info.description = pluginJson.optString("description", "");
                info.photoUrl = pluginJson.optString("photo", "");
                info.enabled = pluginJson.optBoolean("enabled", false);
                plugins.put(key, info);
                pluginsList.add(info);
            }
        } catch (JSONException e) {
            FileLog.e(e);
        }
    }

    private void savePlugins() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("jellogram_plugins", Context.MODE_PRIVATE);
        JSONObject json = new JSONObject();
        try {
            for (PluginInfo info : pluginsList) {
                JSONObject pluginJson = new JSONObject();
                pluginJson.put("title", info.title);
                pluginJson.put("description", info.description);
                pluginJson.put("photo", info.photoUrl);
                pluginJson.put("enabled", info.enabled);
                json.put(info.id, pluginJson);
            }
        } catch (JSONException e) {
            FileLog.e(e);
        }
        preferences.edit().putString("plugins", json.toString()).apply();
    }

    public PluginInfo installPlugin(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(sb.toString());
            PluginInfo info = new PluginInfo();
            info.id = file.getName().replace(".jello", "");
            info.title = json.optString("title", info.id);
            info.description = json.optString("description", "");
            info.photoUrl = json.optString("photo", "");
            info.enabled = false;

            if (plugins.containsKey(info.id)) {
                PluginInfo existing = plugins.get(info.id);
                existing.title = info.title;
                existing.description = info.description;
                existing.photoUrl = info.photoUrl;
            } else {
                plugins.put(info.id, info);
                pluginsList.add(info);
            }
            savePlugins();

            // Copy plugin file to app directory
            File pluginsDir = new File(ApplicationLoader.getFilesDirFixed(), "jellogram_plugins");
            if (!pluginsDir.exists()) {
                pluginsDir.mkdirs();
            }
            File destFile = new File(pluginsDir, file.getName());
            AndroidUtilities.copyFile(file, destFile);

            return info;
        } catch (Exception e) {
            FileLog.e(e);
        }
        return null;
    }

    public void setPluginEnabled(String id, boolean enabled) {
        PluginInfo info = plugins.get(id);
        if (info != null) {
            info.enabled = enabled;
            savePlugins();
        }
    }

    public boolean isPluginEnabled(String id) {
        PluginInfo info = plugins.get(id);
        return info != null && info.enabled;
    }

    public PluginInfo getPlugin(String id) {
        return plugins.get(id);
    }

    public ArrayList<PluginInfo> getPlugins() {
        return new ArrayList<>(pluginsList);
    }

    public void removePlugin(String id) {
        PluginInfo info = plugins.remove(id);
        if (info != null) {
            pluginsList.remove(info);
            savePlugins();
        }
    }
}