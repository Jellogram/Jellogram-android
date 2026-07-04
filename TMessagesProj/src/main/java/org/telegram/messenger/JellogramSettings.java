package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.io.InputStream;

public class JellogramSettings {

    private static final String PREFS_NAME = "jellogram_settings";
    
    private static final String KEY_SHOW_USER_ID = "show_user_id";
    private static final String KEY_SHOW_CHAT_ID = "show_chat_id";
    private static final String KEY_CAMERA2_API = "enable_camera2_api";
    private static final String KEY_AVATAR_CORNER_RADIUS = "avatar_corner_radius";
    private static final String KEY_CENTER_CHAT_TITLES = "center_chat_titles";
    private static final String KEY_ENABLE_MD3 = "enable_md3";
    private static final String KEY_SHOW_BORDER = "show_border";
    private static final String KEY_HIDE_BOTTOM_TABS = "hide_bottom_tabs";
    private static final String KEY_MD3_SWITCHES = "md3_switches";
    private static final String KEY_PLUGINS_ENABLED = "plugins_enabled";
    private static final String KEY_LOGO_SCALE = "logo_scale";
    private static final String KEY_INVITE_LINK_PREFIX = "invite_link_prefix";
    private static final String KEY_PLUGIN_INSTALL_INFO = "plugin_install_info";
    private static final String KEY_CUSTOM_DNS = "custom_dns";
    private static final String KEY_ENABLE_IPV6 = "enable_ipv6";
    private static final String KEY_TCP_OPTIMIZATION = "tcp_optimization";
    private static final String KEY_CONNECTION_KEEPALIVE = "connection_keepalive";
    private static final String KEY_UPDATES_ENABLED = "updates_enabled";

    private static Boolean updatesEnabledDefault;

    private static SharedPreferences prefs;
    private static JellogramSettings instance;

    private JellogramSettings() {
        prefs = ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static JellogramSettings getInstance() {
        if (instance == null) {
            synchronized (JellogramSettings.class) {
                if (instance == null) {
                    instance = new JellogramSettings();
                }
            }
        }
        return instance;
    }

    public boolean isShowUserId() {
        return prefs.getBoolean(KEY_SHOW_USER_ID, true);
    }

    public void setShowUserId(boolean value) {
        prefs.edit().putBoolean(KEY_SHOW_USER_ID, value).apply();
    }

    public boolean isShowChatId() {
        return prefs.getBoolean(KEY_SHOW_CHAT_ID, true);
    }

    public void setShowChatId(boolean value) {
        prefs.edit().putBoolean(KEY_SHOW_CHAT_ID, value).apply();
    }

    public boolean isCamera2ApiEnabled() {
        return prefs.getBoolean(KEY_CAMERA2_API, false);
    }

    public void setCamera2ApiEnabled(boolean value) {
        prefs.edit().putBoolean(KEY_CAMERA2_API, value).apply();
    }

    public int getAvatarCornerRadius() {
        return prefs.getInt(KEY_AVATAR_CORNER_RADIUS, 100); // 100 = полный круг
    }

    public void setAvatarCornerRadius(int value) {
        prefs.edit().putInt(KEY_AVATAR_CORNER_RADIUS, Math.max(0, Math.min(100, value))).apply();
    }

    public boolean isCenterChatTitles() {
        return prefs.getBoolean(KEY_CENTER_CHAT_TITLES, false);
    }

    public void setCenterChatTitles(boolean value) {
        prefs.edit().putBoolean(KEY_CENTER_CHAT_TITLES, value).apply();
    }

    public boolean isMd3Enabled() {
        return prefs.getBoolean(KEY_ENABLE_MD3, false);
    }

    public void setMd3Enabled(boolean value) {
        prefs.edit().putBoolean(KEY_ENABLE_MD3, value).apply();
    }

    public boolean isShowBorder() {
        return prefs.getBoolean(KEY_SHOW_BORDER, false);
    }

    public void setShowBorder(boolean value) {
        prefs.edit().putBoolean(KEY_SHOW_BORDER, value).apply();
    }

    public boolean isHideBottomTabs() {
        return prefs.getBoolean(KEY_HIDE_BOTTOM_TABS, false);
    }

    public void setHideBottomTabs(boolean value) {
        prefs.edit().putBoolean(KEY_HIDE_BOTTOM_TABS, value).apply();
    }

    public boolean isMd3SwitchesEnabled() {
        return prefs.getBoolean(KEY_MD3_SWITCHES, false);
    }

    public void setMd3SwitchesEnabled(boolean value) {
        prefs.edit().putBoolean(KEY_MD3_SWITCHES, value).apply();
    }

    public String getInviteLinkPrefix() {
        return prefs.getString(KEY_INVITE_LINK_PREFIX, "jg.me");
    }

    public void setInviteLinkPrefix(String value) {
        prefs.edit().putString(KEY_INVITE_LINK_PREFIX, value).apply();
    }

    public String getPluginInstallInfo() {
        return prefs.getString(KEY_PLUGIN_INSTALL_INFO, "");
    }

    public void setPluginInstallInfo(String value) {
        prefs.edit().putString(KEY_PLUGIN_INSTALL_INFO, value).apply();
    }

    public String getCustomDns() {
        return prefs.getString(KEY_CUSTOM_DNS, "");
    }

    public void setCustomDns(String value) {
        prefs.edit().putString(KEY_CUSTOM_DNS, value).apply();
    }

    public boolean isIpv6Enabled() {
        return prefs.getBoolean(KEY_ENABLE_IPV6, true);
    }

    public void setIpv6Enabled(boolean value) {
        prefs.edit().putBoolean(KEY_ENABLE_IPV6, value).apply();
    }

    public boolean isTcpOptimizationEnabled() {
        return prefs.getBoolean(KEY_TCP_OPTIMIZATION, true);
    }

    public void setTcpOptimizationEnabled(boolean value) {
        prefs.edit().putBoolean(KEY_TCP_OPTIMIZATION, value).apply();
    }

    public boolean isConnectionKeepaliveEnabled() {
        return prefs.getBoolean(KEY_CONNECTION_KEEPALIVE, true);
    }

    public void setConnectionKeepaliveEnabled(boolean value) {
        prefs.edit().putBoolean(KEY_CONNECTION_KEEPALIVE, value).apply();
    }

    private static final String KEY_DISABLE_PREMIUM_STATUS_EFFECTS = "disable_premium_status_effects";

    public boolean isDisablePremiumStatusEffects() {
        return prefs.getBoolean(KEY_DISABLE_PREMIUM_STATUS_EFFECTS, false);
    }

    public void setDisablePremiumStatusEffects(boolean value) {
        prefs.edit().putBoolean(KEY_DISABLE_PREMIUM_STATUS_EFFECTS, value).apply();
    }

    public boolean isUpdatesEnabled() {
        if (prefs.contains(KEY_UPDATES_ENABLED)) {
            return prefs.getBoolean(KEY_UPDATES_ENABLED, true);
        }
        if (updatesEnabledDefault == null) {
            try {
                InputStream is = ApplicationLoader.applicationContext.getAssets().open("config.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                String jsonStr = new String(buffer, "UTF-8");
                JSONObject json = new JSONObject(jsonStr);
                updatesEnabledDefault = json.optBoolean("updates", true);
            } catch (Exception e) {
                updatesEnabledDefault = true;
            }
        }
        return updatesEnabledDefault;
    }

    public void setUpdatesEnabled(boolean value) {
        prefs.edit().putBoolean(KEY_UPDATES_ENABLED, value).apply();
    }

    public static boolean isCrashDialogShown() {
        return ApplicationLoader.applicationContext != null &&
                ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .getBoolean("crash_dialog_shown", false);
    }

    public static void setCrashDialogShown(boolean shown) {
        if (ApplicationLoader.applicationContext == null) {
            return;
        }
        ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean("crash_dialog_shown", shown)
                .apply();
    }
}