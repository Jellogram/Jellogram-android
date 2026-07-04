package org.telegram.ui.Cells;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.telegram.messenger.lua.LuaPlugin;
import org.telegram.messenger.R;
import org.telegram.ui.Theme;
import org.telegram.ui.Components.LayoutHelper;

/**
 * Plugin Cell for displaying plugin info in list
 */
public class PluginCell extends FrameLayout {
    private TextView nameView;
    private TextView versionView;
    private ImageView iconView;
    private LuaPlugin plugin;

    public PluginCell(Context context) {
        super(context);
        initViews(context);
    }

    private void initViews(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setWeightSum(1);
        addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 60, Gravity.LEFT | Gravity.TOP));

        iconView = new ImageView(context);
        iconView.setImageResource(R.drawable.msg_plugins);
        linearLayout.addView(iconView, LayoutHelper.createLinear(24, 24, Gravity.CENTER_VERTICAL, 16, 0, 0, 0));

        LinearLayout textLayout = new LinearLayout(context);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(textLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 1, Gravity.CENTER_VERTICAL, 16, 0, 16, 0));

        nameView = new TextView(context);
        nameView.setTextSize(16);
        nameView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textLayout.addView(nameView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        versionView = new TextView(context);
        versionView.setTextSize(13);
        versionView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        textLayout.addView(versionView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
    }

    public void setPlugin(LuaPlugin plugin) {
        this.plugin = plugin;
        nameView.setText(plugin.getName());
        String version = plugin.getMetadata(\"version\");
        versionView.setText(version != null ? \"v\" + version : \"v1.0\");
    }
}
", "path": "TMessagesProj/src/main/java/org/telegram/ui/Cells/PluginCell.java"}, "repo": "Jellogram-android"}