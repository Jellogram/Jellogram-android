package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.PluginManager;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;

import java.io.File;
import java.io.FileInputStream;

public class PluginInstallActivity extends BaseFragment {

    private String pluginFilePath;
    private PluginManager.PluginInfo pluginInfo;
    private ImageView photoView;
    private TextView titleView;
    private TextView descriptionView;
    private boolean installed;

    public PluginInstallActivity(Bundle args) {
        super(args);
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(getString(R.string.JellogramPluginInstallTitle));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        String filePath = getArguments().getString("file_path");
        if (filePath == null) {
            Intent intent = getParentActivity().getIntent();
            if (intent != null && intent.getData() != null) {
                filePath = intent.getData().getPath();
            }
        }

        if (filePath == null) {
            showError(context);
            return fragmentView;
        }

        pluginFilePath = filePath;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                showError(context);
                return fragmentView;
            }

            PluginManager.PluginInfo info = PluginManager.getInstance().getPlugin(file.getName().replace(".jello", ""));
            if (info != null) {
                pluginInfo = info;
                installed = true;
            } else {
                // Parse the plugin file
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                fis.close();

                String jsonStr = new String(data, "UTF-8");
                org.json.JSONObject json = new org.json.JSONObject(jsonStr);

                pluginInfo = new PluginManager.PluginInfo();
                pluginInfo.id = file.getName().replace(".jello", "");
                pluginInfo.title = json.optString("title", pluginInfo.id);
                pluginInfo.description = json.optString("description", "");
                pluginInfo.photoUrl = json.optString("photo", "");
                pluginInfo.enabled = false;
            }
        } catch (Exception e) {
            FileLog.e(e);
            showError(context);
            return fragmentView;
        }

        FrameLayout contentView = new FrameLayout(context);
        contentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(24), dp(24), dp(24));

        // Photo
        photoView = new ImageView(context);
        photoView.setLayoutParams(new LinearLayout.LayoutParams(dp(120), dp(120)));
        photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photoView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        ((LinearLayout.LayoutParams) photoView.getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;
        photoView.setImageResource(android.R.drawable.ic_menu_gallery);

        if (!TextUtils.isEmpty(pluginInfo.photoUrl)) {
            try {
                File imgFile = new File(pluginInfo.photoUrl);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    if (bitmap != null) {
                        photoView.setImageBitmap(bitmap);
                    }
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }

        layout.addView(photoView);
        layout.addView(createSpace(context, dp(16)));

        // Title
        titleView = new TextView(context);
        titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
        titleView.setGravity(Gravity.CENTER_HORIZONTAL);
        titleView.setText(pluginInfo.title);
        layout.addView(titleView);
        layout.addView(createSpace(context, dp(8)));

        // Description
        descriptionView = new TextView(context);
        descriptionView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        descriptionView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        descriptionView.setGravity(Gravity.CENTER_HORIZONTAL);
        descriptionView.setText(pluginInfo.description);
        layout.addView(descriptionView);
        layout.addView(createSpace(context, dp(32)));

        // Install / Open button
        TextView installButton = new TextView(context);
        installButton.setPadding(dp(32), dp(12), dp(32), dp(12));
        installButton.setGravity(Gravity.CENTER);
        installButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        installButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        installButton.setBackground(Theme.createSimpleSelectorRoundRectDrawable(dp(12), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
        installButton.setTextColor(0xffffffff);

        if (installed) {
            boolean enabled = pluginInfo.enabled;
            installButton.setText(enabled ? getString(R.string.JellogramPluginsDisable) : getString(R.string.JellogramPluginsEnable));
        } else {
            installButton.setText(getString(R.string.JellogramPluginInstall));
        }

        installButton.setOnClickListener(v -> {
            if (installed) {
                boolean newEnabled = !pluginInfo.enabled;
                PluginManager.getInstance().setPluginEnabled(pluginInfo.id, newEnabled);
                pluginInfo.enabled = newEnabled;
                installButton.setText(newEnabled ? getString(R.string.JellogramPluginsDisable) : getString(R.string.JellogramPluginsEnable));
            } else {
                PluginManager.PluginInfo result = PluginManager.getInstance().installPlugin(pluginFilePath);
                if (result != null) {
                    pluginInfo = result;
                    installed = true;
                    installButton.setText(getString(R.string.JellogramPluginsEnable));
                    installButton.setOnClickListener(null);
                }
            }
        });

        // Back button
        TextView backButton = new TextView(context);
        backButton.setPadding(dp(32), dp(12), dp(32), dp(12));
        backButton.setGravity(Gravity.CENTER);
        backButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        backButton.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        backButton.setText(getString(R.string.Back));
        backButton.setOnClickListener(v -> finishFragment());

        layout.addView(installButton);
        layout.addView(createSpace(context, dp(12)));
        layout.addView(backButton);

        contentView.addView(layout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
        fragmentView = contentView;

        return fragmentView;
    }

    private View createSpace(Context context, int height) {
        View space = new View(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, height));
        return space;
    }

    private void showError(Context context) {
        TextView errorView = new TextView(context);
        errorView.setText(getString(R.string.JellogramPluginNotFound));
        errorView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        errorView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        errorView.setGravity(Gravity.CENTER);
        fragmentView = errorView;
    }
}