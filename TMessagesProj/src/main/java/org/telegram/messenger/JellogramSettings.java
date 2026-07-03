package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

public class JellogramSettings {

    private static final String PREFS_NAME = "jellogram_settings";
    
    private static final String KEY_SHOW_USER_ID = "show_user_id";
    private static final String KEY_SHOW_CHAT_ID = "show_chat_id";
    private static final String KEY_CAMERA2_API = "enable_camera2_api";
    private static final String KEY_AVATAR_CORNER_RADIUS = "avatar_corner_radius"; // 0-100
    private static final String KEY_CENTER_CHAT_TITLES = "center_chat_titles";
    private static final String KEY_ENABLE_MD3 = "enable_md3";
    private static final String KEY_SHOW_BORDER = "show_border";
    private static final String KEY_HIDE_BOTTOM_TABS = "hide_bottom_tabs";
    private static final String KEY_MD3_SWITCHES = "md3_switches";

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

    private static final String KEY_DISABLE_PREMIUM_STATUS_EFFECTS = "disable_premium_status_effects";

    public boolean isDisablePremiumStatusEffects() {
        return prefs.getBoolean(KEY_DISABLE_PREMIUM_STATUS_EFFECTS, false);
    }

    public void setDisablePremiumStatusEffects(boolean value) {
        prefs.edit().putBoolean(KEY_DISABLE_PREMIUM_STATUS_EFFECTS, value).apply();
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