package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.JellogramSettings;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.PluginManager;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SeekBarView;
import org.telegram.ui.Components.Switch;
import org.telegram.ui.Components.SwitchMD3;

import java.util.ArrayList;
import java.util.List;

public class JellogramSettingsActivity extends BaseFragment {

    private static final String KEY_CATEGORY = "category";
    public static final int CATEGORY_MAIN = 0;
    public static final int CATEGORY_GENERAL = 1;
    public static final int CATEGORY_APPEARANCE = 2;
    public static final int CATEGORY_NETWORK = 5;
    public static final int CATEGORY_OTHER = 3;
    public static final int CATEGORY_PLUGINS = 4;

    public JellogramSettingsActivity() {
        this(CATEGORY_MAIN);
    }

    public JellogramSettingsActivity(int category) {
        Bundle args = new Bundle();
        args.putInt(KEY_CATEGORY, category);
        setArguments(args);
    }

    private int currentCategory;
    private RecyclerListView listView;
    private ListAdapter adapter;
    private JellogramSettings settings;
    private PluginManager pluginManager;
    private final ArrayList<Item> items = new ArrayList<>();

    private static final int ITEM_SHOW_USER_ID = 1;
    private static final int ITEM_SHOW_CHAT_ID = 2;
    private static final int ITEM_AVATAR_CORNER_RADIUS = 3;
    private static final int ITEM_ENABLE_MD3 = 4;
    private static final int ITEM_CENTER_CHAT_TITLES = 5;
    private static final int ITEM_CAMERA2_API = 6;
    private static final int ITEM_SHOW_BORDER = 7;
    private static final int ITEM_HIDE_BOTTOM_TABS = 8;
    private static final int ITEM_MD3_SWITCHES = 9;
    private static final int ITEM_DISABLE_PREMIUM_EFFECTS = 10;
    private static final int ITEM_CUSTOM_DNS = 11;
    private static final int ITEM_ENABLE_IPV6 = 12;
    private static final int ITEM_TCP_OPTIMIZATION = 13;
    private static final int ITEM_CONNECTION_KEEPALIVE = 14;
    private static final int ITEM_PLUGIN_START = 1000;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_SLIDER = 1;
    private static final int TYPE_SWITCH = 2;
    private static final int TYPE_LINK_BUTTON = 3;
    private static final int TYPE_TOP_HEADER = 4;
    private static final int TYPE_CATEGORY_BUTTON = 5;
    private static final int TYPE_INPUT = 6;

    private void notifySettingsChanged() {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.jellogramSettingsChanged);
    }

    @Override
    public View createView(Context context) {
        settings = JellogramSettings.getInstance();
        pluginManager = PluginManager.getInstance();
        currentCategory = getArguments() != null ? getArguments().getInt(KEY_CATEGORY, CATEGORY_MAIN) : CATEGORY_MAIN;

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(false);
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        actionBar.setTitleColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        actionBar.setItemsColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), false);
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarActionModeDefaultSelector), false);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        if (currentCategory == CATEGORY_MAIN) {
            actionBar.setTitle("Jellogram");
        } else {
            actionBar.setTitle(getCategoryTitle(currentCategory));
        }

        fragmentView = new FrameLayout(context);
        ((FrameLayout) fragmentView).setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        if (currentCategory == CATEGORY_MAIN) {
            buildMainMenu(context);
        } else {
            buildCategoryList(context);
        }

        return fragmentView;
    }

    private String getCategoryTitle(int category) {
        switch (category) {
            case CATEGORY_GENERAL: return LocaleController.getString(R.string.JellogramCategoryGeneral);
            case CATEGORY_APPEARANCE: return LocaleController.getString(R.string.JellogramCategoryAppearance);
            case CATEGORY_NETWORK: return LocaleController.getString(R.string.JellogramCategoryNetwork);
            case CATEGORY_OTHER: return LocaleController.getString(R.string.JellogramCategoryOther);
            case CATEGORY_PLUGINS: return LocaleController.getString(R.string.JellogramPlugins);
            default: return "Jellogram";
        }
    }

    private void buildMainMenu(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        ((FrameLayout) fragmentView).addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP));

        ImageView logoView = new ImageView(context);
        logoView.setImageResource(R.drawable.jellogram_intro_logo);
        logoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        linearLayout.addView(logoView, LayoutHelper.createLinear(80, 80, Gravity.CENTER_HORIZONTAL, 0, 24, 0, 0));

        TextView titleView = new TextView(context);
        titleView.setText("Jellogram");
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
        titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        titleView.setGravity(Gravity.CENTER_HORIZONTAL);
        titleView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        linearLayout.addView(titleView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 8, 0, 4));

        TextView versionView = new TextView(context);
        versionView.setText("v" + BuildVars.BUILD_VERSION_STRING + " (" + BuildVars.BUILD_VERSION + ")");
        versionView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        versionView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        versionView.setGravity(Gravity.CENTER_HORIZONTAL);
        linearLayout.addView(versionView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 20));

        addCategoryButton(linearLayout, context, R.drawable.settings_chat, LocaleController.getString(R.string.JellogramCategoryGeneral),
            null, CATEGORY_GENERAL);
        addCategoryButton(linearLayout, context, R.drawable.settings_power, LocaleController.getString(R.string.JellogramCategoryAppearance),
            null, CATEGORY_APPEARANCE);
        addCategoryButton(linearLayout, context, R.drawable.settings_data, LocaleController.getString(R.string.JellogramCategoryNetwork),
            null, CATEGORY_NETWORK);
        addCategoryButton(linearLayout, context, R.drawable.ic_ab_other, LocaleController.getString(R.string.JellogramCategoryOther),
            null, CATEGORY_OTHER);
        addCategoryButton(linearLayout, context, R.drawable.settings_features, LocaleController.getString(R.string.JellogramPlugins),
            null, CATEGORY_PLUGINS);
    }

    private void addCategoryButton(LinearLayout parent, Context context, int icon, String title, String subtitle, int category) {
        FrameLayout button = new FrameLayout(context);
        button.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        button.setPadding(dp(21), dp(12), dp(21), dp(12));
        button.setBackground(Theme.createSimpleSelectorRoundRectDrawable(0, Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_actionBarActionModeDefaultSelector)));
        button.setOnClickListener(v -> presentFragment(new JellogramSettingsActivity(category)));

        ImageView iconView = new ImageView(context);
        iconView.setImageResource(icon);
        iconView.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        button.addView(iconView, LayoutHelper.createFrame(28, 28, Gravity.LEFT | Gravity.CENTER_VERTICAL, 0, 0, 16, 0));

        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        titleView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        titleView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        button.addView(titleView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 44, 0, 0, 0));

        if (!TextUtils.isEmpty(subtitle)) {
            TextView subView = new TextView(context);
            subView.setText(subtitle);
            subView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            subView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
            subView.setGravity(Gravity.LEFT);
            button.addView(subView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 44, 18, 0, 0));
        }

        ImageView arrowView = new ImageView(context);
        arrowView.setImageResource(R.drawable.msg_arrowright);
        arrowView.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        button.addView(arrowView, LayoutHelper.createFrame(24, 24, Gravity.RIGHT | Gravity.CENTER_VERTICAL));

        View divider = new View(context);
        divider.setBackgroundColor(Theme.getColor(Theme.key_divider));
        button.addView(divider, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 1, Gravity.BOTTOM));

        parent.addView(button, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, dp(64)));
    }

    private void buildCategoryList(Context context) {
        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(adapter = new ListAdapter());
        ((FrameLayout) fragmentView).addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener((view, position) -> {
            int itemPos = position - 1;
            if (position <= 0 || itemPos < 0 || itemPos >= items.size()) {
                return;
            }
            Item item = items.get(itemPos);
            if (item.isPlaceholder) return;
            if (item.type == TYPE_INPUT) {
                switch (item.id) {
                    case ITEM_CUSTOM_DNS: {
                        showDnsInputDialog();
                        break;
                    }
                }
            } else if (item.type == TYPE_SWITCH) {
                switch (item.id) {
                    case ITEM_SHOW_USER_ID: {
                        boolean v = !settings.isShowUserId();
                        settings.setShowUserId(v);
                        ((SwitchCell) view).setChecked(v);
                        notifySettingsChanged();
                        break;
                    }
                    case ITEM_SHOW_CHAT_ID: {
                        boolean v = !settings.isShowChatId();
                        settings.setShowChatId(v);
                        ((SwitchCell) view).setChecked(v);
                        notifySettingsChanged();
                        break;
                    }
                    case ITEM_CAMERA2_API: {
                        SharedConfig.toggleUseCamera2(currentAccount);
                        ((SwitchCell) view).setChecked(SharedConfig.isUsingCamera2(currentAccount));
                        notifySettingsChanged();
                        break;
                    }
                    case ITEM_CENTER_CHAT_TITLES: {
                        boolean v = !settings.isCenterChatTitles();
                        settings.setCenterChatTitles(v);
                        ((SwitchCell) view).setChecked(v);
                        notifySettingsChanged();
                        break;
                    }
                    case ITEM_ENABLE_MD3: {
                        boolean v = !settings.isMd3Enabled();
                        settings.setMd3Enabled(v);
                        ((SwitchCell) view).setChecked(v);
                        notifySettingsChanged();
                        break;
                    }
                    case ITEM_SHOW_BORDER: {
                        boolean v = !settings.isShowBorder();
                        settings.setShowBorder(v);
                        ((SwitchCell) view).setChecked(v);
                        notifySettingsChanged();
                        break;
                    }
                    case ITEM_HIDE_BOTTOM_TABS: {
                        boolean v = !settings.isHideBottomTabs();
                        settings.setHideBottomTabs(v);
                        ((SwitchCell) view).setChecked(v);
                        notifySettingsChanged();
                        break;
                    }
                    case ITEM_MD3_SWITCHES: {
                        boolean v = !settings.isMd3SwitchesEnabled();
                        settings.setMd3SwitchesEnabled(v);
                        ((SwitchCell) view).setChecked(v);
                        notifySettingsChanged();
                        break;
                    }
                    case ITEM_DISABLE_PREMIUM_EFFECTS: {
                        boolean v = !settings.isDisablePremiumStatusEffects();
                        settings.setDisablePremiumStatusEffects(v);
                        ((SwitchCell) view).setChecked(v);
                        notifySettingsChanged();
                        break;
                    }
                    case ITEM_ENABLE_IPV6: {
                        boolean v = !settings.isIpv6Enabled();
                        settings.setIpv6Enabled(v);
                        ((SwitchCell) view).setChecked(v);
                        notifySettingsChanged();
                        break;
                    }
                    case ITEM_TCP_OPTIMIZATION: {
                        boolean v = !settings.isTcpOptimizationEnabled();
                        settings.setTcpOptimizationEnabled(v);
                        ((SwitchCell) view).setChecked(v);
                        notifySettingsChanged();
                        break;
                    }
                    case ITEM_CONNECTION_KEEPALIVE: {
                        boolean v = !settings.isConnectionKeepaliveEnabled();
                        settings.setConnectionKeepaliveEnabled(v);
                        ((SwitchCell) view).setChecked(v);
                        notifySettingsChanged();
                        break;
                    }
                    default:
                        if (item.id >= ITEM_PLUGIN_START) {
                            handlePluginToggle(item.id, view);
                        }
                        break;
                }
            } else if (item.type == TYPE_LINK_BUTTON) {
                switch (item.id) {
                    case 0: Browser.openUrl(getContext(), "https://jg.me/Jellogram"); break;
                    case 1: Browser.openUrl(getContext(), "https://jg.me/JellogramChat"); break;
                    case 2: Browser.openUrl(getContext(), "https://jg.me/JellogramTranslate"); break;
                    case 3: Browser.openUrl(getContext(), "https://jg.me"); break;
                    case 4: Browser.openUrl(getContext(), "https://jg.midga3.ru/faq"); break;
                    case 5: Browser.openUrl(getContext(), "https://jg.midga3.ru/tos"); break;
                    case 6: Browser.openUrl(getContext(), "https://jg.midga3.ru/security"); break;
                }
            }
        });

        updateItems();
    }

    private void handlePluginToggle(int itemId, View view) {
        int pluginIndex = itemId - ITEM_PLUGIN_START;
        List<PluginManager.PluginInfo> plugins = pluginManager.getPlugins();
        if (pluginIndex >= 0 && pluginIndex < plugins.size()) {
            PluginManager.PluginInfo plugin = plugins.get(pluginIndex);
            boolean newState = !plugin.enabled;
            pluginManager.setPluginEnabled(plugin.id, newState);
            ((SwitchCell) view).setChecked(newState);
            notifySettingsChanged();
        }
    }

    private void updateItems() {
        items.clear();

        switch (currentCategory) {
            case CATEGORY_GENERAL:
                items.add(new Item(TYPE_HEADER, 0, LocaleController.getString(R.string.JellogramCategoryGeneral)));
                items.add(new Item(TYPE_SWITCH, ITEM_SHOW_USER_ID, LocaleController.getString(R.string.JellogramShowUserId)));
                items.add(new Item(TYPE_SWITCH, ITEM_SHOW_CHAT_ID, LocaleController.getString(R.string.JellogramShowChatId)));
                items.add(new Item(TYPE_SWITCH, ITEM_CENTER_CHAT_TITLES, LocaleController.getString(R.string.JellogramCenterTitles)));
                items.add(new Item(TYPE_SWITCH, ITEM_HIDE_BOTTOM_TABS, LocaleController.getString(R.string.JellogramHideBottomTabs)));
                items.add(new Item(TYPE_SWITCH, ITEM_CAMERA2_API, LocaleController.getString(R.string.JellogramCamera2)));
                items.add(new Item(TYPE_SWITCH, ITEM_DISABLE_PREMIUM_EFFECTS, LocaleController.getString(R.string.JellogramDisablePremiumStatusEffects)));
                break;

            case CATEGORY_APPEARANCE:
                items.add(new Item(TYPE_HEADER, 0, LocaleController.getString(R.string.JellogramCategoryAppearance)));
                items.add(new Item(TYPE_SLIDER, ITEM_AVATAR_CORNER_RADIUS, LocaleController.getString(R.string.JellogramAvatarRadius)));
                items.add(new Item(TYPE_SWITCH, ITEM_ENABLE_MD3, LocaleController.getString(R.string.JellogramMd3)));
                items.add(new Item(TYPE_SWITCH, ITEM_MD3_SWITCHES, LocaleController.getString(R.string.JellogramMd3Switches)));
                items.add(new Item(TYPE_SWITCH, ITEM_SHOW_BORDER, LocaleController.getString(R.string.JellogramShowBorder)));
                break;

            case CATEGORY_NETWORK:
                items.add(new Item(TYPE_HEADER, 0, LocaleController.getString(R.string.JellogramCategoryNetwork)));
                items.add(new Item(TYPE_INPUT, ITEM_CUSTOM_DNS, LocaleController.getString(R.string.JellogramCustomDns)));
                items.add(new Item(TYPE_SWITCH, ITEM_ENABLE_IPV6, LocaleController.getString(R.string.JellogramEnableIpv6)));
                items.add(new Item(TYPE_SWITCH, ITEM_TCP_OPTIMIZATION, LocaleController.getString(R.string.JellogramTcpOptimization)));
                items.add(new Item(TYPE_SWITCH, ITEM_CONNECTION_KEEPALIVE, LocaleController.getString(R.string.JellogramConnectionKeepalive)));
                break;

            case CATEGORY_OTHER:
                items.add(new Item(TYPE_HEADER, 0, LocaleController.getString(R.string.JellogramCategoryOther)));
                items.add(new Item(TYPE_LINK_BUTTON, 0, LocaleController.getString(R.string.JellogramChannelLink)));
                items.add(new Item(TYPE_LINK_BUTTON, 1, LocaleController.getString(R.string.JellogramChatLink)));
                items.add(new Item(TYPE_LINK_BUTTON, 2, LocaleController.getString(R.string.JellogramTranslateLink)));
                items.add(new Item(TYPE_LINK_BUTTON, 3, LocaleController.getString(R.string.JellogramWebsiteLink)));
                items.add(new Item(TYPE_LINK_BUTTON, 4, LocaleController.getString(R.string.JellogramFaqLink)));
                items.add(new Item(TYPE_LINK_BUTTON, 5, LocaleController.getString(R.string.JellogramTosLink)));
                items.add(new Item(TYPE_LINK_BUTTON, 6, LocaleController.getString(R.string.JellogramSecurityLink)));
                break;

            case CATEGORY_PLUGINS:
                items.add(new Item(TYPE_HEADER, 0, LocaleController.getString(R.string.JellogramPlugins)));
                List<PluginManager.PluginInfo> plugins = pluginManager.getPlugins();
                if (plugins.isEmpty()) {
                    Item noPlugins = new Item(TYPE_SWITCH, -1, LocaleController.getString(R.string.JellogramPluginNotFound));
                    noPlugins.isPlaceholder = true;
                    items.add(noPlugins);
                } else {
                    for (int i = 0; i < plugins.size(); i++) {
                        PluginManager.PluginInfo p = plugins.get(i);
                        Item pi = new Item(TYPE_SWITCH, ITEM_PLUGIN_START + i, p.title);
                        pi.pluginId = p.id;
                        items.add(pi);
                    }
                }
                break;
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void showDnsInputDialog() {
        if (getParentActivity() == null) return;
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getParentActivity());
        builder.setTitle(LocaleController.getString(R.string.JellogramCustomDns));

        android.widget.EditText input = new android.widget.EditText(getParentActivity());
        input.setText(settings.getCustomDns());
        input.setHint(LocaleController.getString(R.string.JellogramCustomDnsHint));
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        input.setSelection(input.length());
        input.setPadding(dp(21), dp(12), dp(21), dp(12));
        builder.setView(input);

        builder.setPositiveButton(LocaleController.getString(R.string.OK), (dialog, which) -> {
            String value = input.getText().toString().trim();
            settings.setCustomDns(value);
            if (adapter != null) adapter.notifyDataSetChanged();
            notifySettingsChanged();
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        builder.setNeutralButton(LocaleController.getString(R.string.Default), (dialog, which) -> {
            settings.setCustomDns("");
            if (adapter != null) adapter.notifyDataSetChanged();
            notifySettingsChanged();
        });
        builder.show();
    }

    // --- Item model ---

    private static class Item {
        final int type;
        final int id;
        final CharSequence text;
        String pluginId;
        boolean isPlaceholder;

        Item(int type, int id, CharSequence text) {
            this.type = type;
            this.id = id;
            this.text = text;
        }
    }

    // --- Adapter ---

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        @Override
        public int getItemCount() {
            return items.size() + 1;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int pos = holder.getAdapterPosition();
            if (pos <= 0 || pos > items.size()) return false;
            Item item = items.get(pos - 1);
            return (item.type == TYPE_SWITCH || item.type == TYPE_LINK_BUTTON || item.type == TYPE_INPUT) && !item.isPlaceholder;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == TYPE_TOP_HEADER) {
                view = new TopHeaderCell(parent.getContext());
            } else if (viewType == TYPE_HEADER) {
                HeaderCell headerCell = new HeaderCell(parent.getContext(), Theme.key_windowBackgroundWhiteBlueHeader, 21, 15, false, resourceProvider);
                headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                headerCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                view = headerCell;
            } else if (viewType == TYPE_SLIDER) {
                view = new SliderCell(parent.getContext());
            } else if (viewType == TYPE_LINK_BUTTON) {
                view = new LinkButtonCell(parent.getContext());
            } else if (viewType == TYPE_INPUT) {
                view = new InputCell(parent.getContext());
            } else {
                view = new SwitchCell(parent.getContext());
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (position == 0) return;
            Item item = items.get(position - 1);
            if (holder.getItemViewType() == TYPE_HEADER) {
                ((HeaderCell) holder.itemView).setText(item.text);
            } else if (holder.getItemViewType() == TYPE_SLIDER) {
                ((SliderCell) holder.itemView).setItem(item);
            } else if (holder.getItemViewType() == TYPE_LINK_BUTTON) {
                ((LinkButtonCell) holder.itemView).setText(item.text);
            } else if (holder.getItemViewType() == TYPE_INPUT) {
                String value = "";
                if (item.id == ITEM_CUSTOM_DNS) {
                    value = settings.getCustomDns();
                }
                ((InputCell) holder.itemView).setValue(item.text, value);
            } else if (holder.getItemViewType() == TYPE_SWITCH) {
                SwitchCell cell = (SwitchCell) holder.itemView;
                boolean checked = false;
                boolean showToggle = true;
                switch (item.id) {
                    case ITEM_SHOW_USER_ID: checked = settings.isShowUserId(); break;
                    case ITEM_SHOW_CHAT_ID: checked = settings.isShowChatId(); break;
                    case ITEM_CAMERA2_API: checked = SharedConfig.isUsingCamera2(currentAccount); break;
                    case ITEM_CENTER_CHAT_TITLES: checked = settings.isCenterChatTitles(); break;
                    case ITEM_ENABLE_MD3: checked = settings.isMd3Enabled(); break;
                    case ITEM_SHOW_BORDER: checked = settings.isShowBorder(); break;
                    case ITEM_HIDE_BOTTOM_TABS: checked = settings.isHideBottomTabs(); break;
                    case ITEM_MD3_SWITCHES: checked = settings.isMd3SwitchesEnabled(); break;
                    case ITEM_DISABLE_PREMIUM_EFFECTS: checked = settings.isDisablePremiumStatusEffects(); break;
                    case ITEM_ENABLE_IPV6: checked = settings.isIpv6Enabled(); break;
                    case ITEM_TCP_OPTIMIZATION: checked = settings.isTcpOptimizationEnabled(); break;
                    case ITEM_CONNECTION_KEEPALIVE: checked = settings.isConnectionKeepaliveEnabled(); break;
                    default:
                        if (item.id == -1) {
                            showToggle = false;
                        } else if (item.id >= ITEM_PLUGIN_START) {
                            int idx = item.id - ITEM_PLUGIN_START;
                            List<PluginManager.PluginInfo> pl = pluginManager.getPlugins();
                            if (idx >= 0 && idx < pl.size()) checked = pl.get(idx).enabled;
                        }
                        break;
                }
                cell.setText(item.text, checked, position != getItemCount() - 1);
                if (!showToggle) cell.hideSwitch();
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return TYPE_TOP_HEADER;
            return items.get(position - 1).type;
        }
    }

    // --- Input cell (for DNS, etc.) ---

    private class InputCell extends FrameLayout {
        private final TextView titleView;
        private final TextView valueView;

        public InputCell(Context context) {
            super(context);
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            setPadding(dp(21), 0, dp(21), 0);
            setBackground(Theme.createSimpleSelectorRoundRectDrawable(0, Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_actionBarActionModeDefaultSelector)));

            titleView = new TextView(context);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            titleView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            addView(titleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 0, 10, 56, 0));

            valueView = new TextView(context);
            valueView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            valueView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
            valueView.setGravity((LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL);
            valueView.setEllipsize(TextUtils.TruncateAt.END);
            valueView.setMaxLines(1);
            addView(valueView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.RIGHT | Gravity.TOP, 56, 10, 0, 0));

            ImageView arrowView = new ImageView(context);
            arrowView.setImageResource(R.drawable.msg_arrowright);
            arrowView.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            addView(arrowView, LayoutHelper.createFrame(24, 24, Gravity.CENTER_VERTICAL | Gravity.RIGHT));
        }

        public void setValue(CharSequence title, String value) {
            titleView.setText(title);
            if (value != null && !value.isEmpty()) {
                valueView.setText(value);
                valueView.setVisibility(View.VISIBLE);
            } else {
                valueView.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(dp(56), MeasureSpec.EXACTLY)
            );
        }
    }

    // --- Top header (empty for category pages) ---

    private class TopHeaderCell extends FrameLayout {
        public TopHeaderCell(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(dp(1), MeasureSpec.EXACTLY)
            );
        }
    }

    // --- Slider ---

    private class SliderCell extends FrameLayout {
        private final TextView titleView;
        private final TextView valueView;
        private final SeekBarView seekBarView;
        private Item item;

        public SliderCell(Context context) {
            super(context);
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            setPadding(dp(21), dp(12), dp(21), dp(12));

            titleView = new TextView(context);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            titleView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            addView(titleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL));

            valueView = new TextView(context);
            valueView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            valueView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
            valueView.setGravity(LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT);
            addView(valueView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.RIGHT));

            seekBarView = new SeekBarView(context, true, resourceProvider);
            seekBarView.setReportChanges(true);
            seekBarView.setDelegate(new SeekBarView.SeekBarViewDelegate() {
                @Override
                public void onSeekBarDrag(boolean stop, float progress) {
                    int value = Math.round(progress * 100f);
                    if (item != null && item.id == ITEM_AVATAR_CORNER_RADIUS) {
                        settings.setAvatarCornerRadius(value);
                    }
                    updateValue(value);
                }

                @Override
                public void onSeekBarPressed(boolean pressed) {}

                @Override
                public CharSequence getContentDescription() {
                    return titleView.getText();
                }
            });
            addView(seekBarView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 38, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0, 0, 0));
        }

        public void setItem(Item item) {
            this.item = item;
            titleView.setText(item.text);
            int savedValue = 0;
            if (item.id == ITEM_AVATAR_CORNER_RADIUS) {
                savedValue = settings.getAvatarCornerRadius();
            }
            seekBarView.setProgress(savedValue / 100f);
            updateValue(savedValue);
        }

        private void updateValue(int value) {
            if (item != null && item.id == ITEM_AVATAR_CORNER_RADIUS) {
                if (value == 0) valueView.setText("Квадрат");
                else if (value == 100) valueView.setText("Круг");
                else valueView.setText(String.format("%d%%", value));
            } else {
                valueView.setText(String.format("%d%%", value));
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(dp(88), MeasureSpec.EXACTLY)
            );
        }
    }

    // --- Switch ---

    private class SwitchCell extends FrameLayout {
        private final TextView textView;
        private final TextView subtitleView;
        private final Switch switchView;
        private final SwitchMD3 switchMd3View;

        public SwitchCell(Context context) {
            super(context);
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            setPadding(dp(21), 0, dp(21), 0);

            textView = new TextView(context);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 0, 10, 56, 0));

            subtitleView = new TextView(context);
            subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            subtitleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
            subtitleView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT));
            subtitleView.setVisibility(View.GONE);
            addView(subtitleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 0, 30, 56, 0));

            switchView = new Switch(context);
            switchView.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
            addView(switchView, LayoutHelper.createFrame(37, 40, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT), 0, 0, 0, 0));

            switchMd3View = new SwitchMD3(context);
            switchMd3View.setColors(Theme.key_md3SwitchTrack, Theme.key_md3SwitchTrackChecked, Theme.key_md3SwitchThumb, Theme.key_md3SwitchThumbChecked, Theme.key_md3SwitchIconChecked);
            addView(switchMd3View, LayoutHelper.createFrame(37, 40, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT), 0, 0, 0, 0));
            switchMd3View.setVisibility(View.GONE);
        }

        public void setText(CharSequence text, boolean checked, boolean divider) {
            textView.setText(text);
            boolean useMd3 = settings.isMd3SwitchesEnabled();
            switchView.setVisibility(useMd3 ? View.GONE : View.VISIBLE);
            switchMd3View.setVisibility(useMd3 ? View.VISIBLE : View.GONE);
            if (useMd3) switchMd3View.setChecked(checked, false);
            else switchView.setChecked(checked, false);
        }

        public void setSubtitle(CharSequence subtitle) {
            if (subtitle != null && subtitle.length() > 0) {
                subtitleView.setText(subtitle);
                subtitleView.setVisibility(View.VISIBLE);
            } else {
                subtitleView.setVisibility(View.GONE);
            }
        }

        public void setChecked(boolean checked) {
            boolean useMd3 = settings.isMd3SwitchesEnabled();
            if (useMd3) switchMd3View.setChecked(checked, true);
            else switchView.setChecked(checked, true);
        }

        public void hideSwitch() {
            switchView.setVisibility(View.GONE);
            switchMd3View.setVisibility(View.GONE);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(dp(56), MeasureSpec.EXACTLY)
            );
        }
    }

    // --- Link ---

    private class LinkButtonCell extends FrameLayout {
        private final TextView textView;

        public LinkButtonCell(Context context) {
            super(context);
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            setPadding(dp(21), 0, dp(21), 0);

            textView = new TextView(context);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
            textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        }

        public void setText(CharSequence text) {
            textView.setText(text);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(dp(48), MeasureSpec.EXACTLY)
            );
        }
    }
}
