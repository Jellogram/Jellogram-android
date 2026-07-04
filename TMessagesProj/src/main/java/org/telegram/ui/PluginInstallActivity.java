package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.PluginManager;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Stars.ExplainStarsSheet;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import java.io.File;
import java.io.FileInputStream;

public class PluginInstallActivity {

    public static void showPluginInstallSheet(Context context, String filePath, Theme.ResourcesProvider resourcesProvider) {
        if (context == null || filePath == null) return;

        BottomSheet.Builder builder = new BottomSheet.Builder(context, false, resourcesProvider);
        BottomSheet[] sheetRef = new BottomSheet[1];

        File file = new File(filePath);
        if (!file.exists()) {
            showErrorSheet(context, resourcesProvider);
            return;
        }

        final PluginManager.PluginInfo[] pluginInfoRef = {null};
        final boolean[] alreadyInstalledRef = {false};

        try {
            PluginManager.PluginInfo existing = PluginManager.getInstance().getPlugin(file.getName().replace(".jello", ""));
            if (existing != null) {
                pluginInfoRef[0] = existing;
                alreadyInstalledRef[0] = true;
            } else {
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                fis.close();

                String jsonStr = new String(data, "UTF-8");
                org.json.JSONObject json = new org.json.JSONObject(jsonStr);

                PluginManager.PluginInfo pi = new PluginManager.PluginInfo();
                pi.id = file.getName().replace(".jello", "");
                pi.title = json.optString("title", pi.id);
                pi.description = json.optString("description", "");
                pi.photoUrl = json.optString("photo", "");
                pi.enabled = false;
                pluginInfoRef[0] = pi;
                alreadyInstalledRef[0] = false;
            }
        } catch (Exception e) {
            FileLog.e(e);
            showErrorSheet(context, resourcesProvider);
            return;
        }

        final PluginManager.PluginInfo pi = pluginInfoRef[0];
        final boolean alreadyInstalled = alreadyInstalledRef[0];

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(dp(16), dp(20), dp(16), dp(8));

        RLottieImageView imageView = new RLottieImageView(context);
        imageView.setAnimation(R.raw.media_forbidden, dp(80), dp(80));
        imageView.playAnimation();
        linearLayout.addView(imageView, LayoutHelper.createLinear(80, 80, Gravity.CENTER, 0, 0, 0, 9));

        TextView titleView = new TextView(context);
        titleView.setTypeface(AndroidUtilities.bold());
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
        titleView.setText(pi.title);
        titleView.setGravity(Gravity.CENTER);
        linearLayout.addView(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 0, 0, 8));

        if (!TextUtils.isEmpty(pi.description)) {
            TextView descView = new TextView(context);
            descView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            descView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
            descView.setText(pi.description);
            descView.setGravity(Gravity.CENTER);
            linearLayout.addView(descView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 0, 0, 23));
        } else {
            linearLayout.addView(createSpace(context, dp(23)));
        }

        ButtonWithCounterView primaryButton = new ButtonWithCounterView(context, true, resourcesProvider);
        if (alreadyInstalled) {
            primaryButton.setText(pi.enabled
                ? getString(R.string.JellogramPluginsDisable)
                : getString(R.string.JellogramPluginsEnable), false);
        } else {
            primaryButton.setText(getString(R.string.JellogramPluginInstall), false);
        }
        primaryButton.setOnClickListener(v -> {
            if (alreadyInstalledRef[0]) {
                boolean newEnabled = !pluginInfoRef[0].enabled;
                PluginManager.getInstance().setPluginEnabled(pluginInfoRef[0].id, newEnabled);
                pluginInfoRef[0].enabled = newEnabled;
                primaryButton.setText(newEnabled
                    ? getString(R.string.JellogramPluginsDisable)
                    : getString(R.string.JellogramPluginsEnable), false);
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.jellogramSettingsChanged);
            } else {
                PluginManager.PluginInfo result = PluginManager.getInstance().installPlugin(filePath);
                if (result != null) {
                    pluginInfoRef[0] = result;
                    alreadyInstalledRef[0] = true;
                    primaryButton.setText(getString(R.string.JellogramPluginsEnable), false);
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.jellogramSettingsChanged);
                }
            }
        });
        linearLayout.addView(primaryButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.FILL_HORIZONTAL, 0, 0, 0, 4));

        ButtonWithCounterView closeButton = new ButtonWithCounterView(context, false, resourcesProvider);
        closeButton.setText(getString(R.string.Close), false);
        closeButton.setOnClickListener(v -> sheetRef[0].dismiss());
        linearLayout.addView(closeButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.FILL_HORIZONTAL, 0, 0, 0, 0));

        builder.setCustomView(linearLayout);
        sheetRef[0] = builder.create();
        sheetRef[0].useBackgroundTopPadding = false;
        sheetRef[0].fixNavigationBar();
        sheetRef[0].show();
    }

    private static View createSpace(Context context, int height) {
        View space = new View(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, height));
        return space;
    }

    private static void showErrorSheet(Context context, Theme.ResourcesProvider resourcesProvider) {
        BottomSheet.Builder builder = new BottomSheet.Builder(context, false, resourcesProvider);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(dp(16), dp(40), dp(16), dp(24));

        TextView errorView = new TextView(context);
        errorView.setText(getString(R.string.JellogramPluginNotFound));
        errorView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
        errorView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        errorView.setGravity(Gravity.CENTER);
        linearLayout.addView(errorView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        ButtonWithCounterView closeButton = new ButtonWithCounterView(context, false, resourcesProvider);
        closeButton.setText(getString(R.string.Close), false);
        closeButton.setOnClickListener(v -> { });
        linearLayout.addView(closeButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.FILL_HORIZONTAL, 0, 16, 0, 0));

        builder.setCustomView(linearLayout);
        BottomSheet sheet = builder.create();
        sheet.useBackgroundTopPadding = false;
        sheet.fixNavigationBar();
        sheet.show();
    }
}
