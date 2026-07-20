package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.telegram.messenger.lua.LuaPlugin;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class PluginCell extends FrameLayout {
    private TextView nameView;

    private TextView descView;
    private ImageView iconView;
    private ImageView deleteButton;
    private LuaPlugin plugin;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(LuaPlugin plugin);
    }

    public PluginCell(Context context) {
        super(context);
        initViews(context);
    }

    private void initViews(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 64, Gravity.LEFT | Gravity.TOP));

        iconView = new ImageView(context);
        iconView.setImageResource(R.drawable.msg_plugins);
        iconView.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), PorterDuff.Mode.SRC_IN);
        linearLayout.addView(iconView, LayoutHelper.createLinear(24, 24, Gravity.CENTER_VERTICAL, 16, 0, 0, 0));

        LinearLayout textLayout = new LinearLayout(context);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(textLayout, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, Gravity.CENTER_VERTICAL, 12, 0, 8, 0));

        nameView = new TextView(context);
        nameView.setTextSize(16);
        nameView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textLayout.addView(nameView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        descView = new TextView(context);
        descView.setTextSize(13);
        descView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        descView.setMaxLines(1);
        descView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        textLayout.addView(descView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        deleteButton = new ImageView(context);
        deleteButton.setImageResource(R.drawable.msg_delete);
        deleteButton.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText), PorterDuff.Mode.SRC_IN);
        deleteButton.setScaleType(ImageView.ScaleType.CENTER);
        deleteButton.setPadding(dp(8), dp(8), dp(8), dp(8));
        linearLayout.addView(deleteButton, LayoutHelper.createLinear(40, 40, Gravity.CENTER_VERTICAL, 0, 0, 8, 0));
        deleteButton.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(plugin);
            }
        });
    }

    public void setPlugin(LuaPlugin plugin) {
        this.plugin = plugin;
        nameView.setText(plugin.getName());
        String version = plugin.getMetadata("version");
        String desc = plugin.getMetadata("description");
        if (version != null) {
            desc = (desc != null ? desc + " · " : "") + "v" + version;
        }
        descView.setText(desc != null ? desc : "");
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}