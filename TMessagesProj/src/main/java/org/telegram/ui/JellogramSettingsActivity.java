package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.JellogramSettings;
import org.telegram.messenger.LocaleController;
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

    private static final int CAT_GENERAL = 0;
    private static final int CAT_APPEARANCE = 1;
    private static final int CAT_CHATS = 2;
    private static final int CAT_LINKS = 3;
    private static final int CAT_OTHER = 4;
    private static final int CAT_PLUGINS = 5;

    private static final int ITEM_SHOW_USER_ID = 1;
    private static final int ITEM_SHOW_CHAT_ID = 2;
    private static final int ITEM_AVATAR_CORNER_RADIUS = 3;
    private static final int ITEM_ENABLE_MD3 = 4;
    private static final int ITEM_CENTER_CHAT_TITLES = 5;
    private static final int ITEM_CAMERA2_API = 6;
    private static final int ITEM_SHOW_BORDER = 7;
    private static final int ITEM_HIDE_BOTTOM_TABS = 8;
    private static final int ITEM_MD3_SWITCHES = 9;

    private RecyclerListView listView;
    private ListAdapter adapter;
    private JellogramSettings settings;
    private PluginManager pluginManager;

    private final ArrayList<Item> items = new ArrayList<>();

    @Override
    public View createView(Context context) {
        settings = JellogramSettings.getInstance();
        pluginManager = PluginManager.getInstance();

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(false);
        actionBar.setTitle("");
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

        fragmentView = new FrameLayout(context);
        ((FrameLayout) fragmentView).setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(adapter = new ListAdapter());
        ((FrameLayout) fragmentView).addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener((view, position) -> {
            if (position < 0 || position >= items.size()) {
                return;
            }
            Item item = items.get(position);
            if (item.type == TYPE_SWITCH) {
                switch (item.id) {
                    case ITEM_SHOW_USER_ID: {
                        boolean showUserId = !settings.isShowUserId();
                        settings.setShowUserId(showUserId);
                        ((SwitchCell) view).setChecked(showUserId);
                        break;
                    }
                    case ITEM_SHOW_CHAT_ID: {
                        boolean showChatId = !settings.isShowChatId();
                        settings.setShowChatId(showChatId);
                        ((SwitchCell) view).setChecked(showChatId);
                        break;
                    }
                    case ITEM_CAMERA2_API: {
                        SharedConfig.toggleUseCamera2(currentAccount);
                        ((SwitchCell) view).setChecked(SharedConfig.isUsingCamera2(currentAccount));
                        break;
                    }
                    case ITEM_CENTER_CHAT_TITLES: {
                        boolean centerTitles = !settings.isCenterChatTitles();
                        settings.setCenterChatTitles(centerTitles);
                        ((SwitchCell) view).setChecked(centerTitles);
                        break;
                    }
                    case ITEM_ENABLE_MD3: {
                        boolean md3 = !settings.isMd3Enabled();
                        settings.setMd3Enabled(md3);
                        ((SwitchCell) view).setChecked(md3);
                        break;
                    }
                    case ITEM_SHOW_BORDER: {
                        boolean showBorder = !settings.isShowBorder();
                        settings.setShowBorder(showBorder);
                        ((SwitchCell) view).setChecked(showBorder);
                        break;
                    }
                    case ITEM_HIDE_BOTTOM_TABS: {
                        boolean hideTabs = !settings.isHideBottomTabs();
                        settings.setHideBottomTabs(hideTabs);
                        ((SwitchCell) view).setChecked(hideTabs);
                        break;
                    }
                    case ITEM_MD3_SWITCHES: {
                        boolean md3Switches = !settings.isMd3SwitchesEnabled();
                        settings.setMd3SwitchesEnabled(md3Switches);
                        ((SwitchCell) view).setChecked(md3Switches);
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
                    case 0:
                        Browser.openUrl(getContext(), "https://jg.me/Jellogram");
                        break;
                    case 1:
                        Browser.openUrl(getContext(), "https://jg.me/JellogramChat");
                        break;
                    case 2:
                        Browser.openUrl(getContext(), "https://jg.me/JellogramTranslate");
                        break;
                    case 3:
                        Browser.openUrl(getContext(), "https://jg.me");
                        break;
                    case 4:
                        Browser.openUrl(getContext(), "https://jg.midga3.ru/faq");
                        break;
                    case 5:
                        Browser.openUrl(getContext(), "https://jg.midga3.ru/tos");
                        break;
                    case 6:
                        Browser.openUrl(getContext(), "https://jg.midga3.ru/security");
                        break;
                }
            }
        });

        updateItems();
        return fragmentView;
    }

    private static final int ITEM_PLUGIN_START = 1000;

    private void handlePluginToggle(int itemId, View view) {
        int pluginIndex = itemId - ITEM_PLUGIN_START;
        List<PluginManager.PluginInfo> plugins = pluginManager.getPlugins();
        if (pluginIndex >= 0 && pluginIndex < plugins.size()) {
            PluginManager.PluginInfo plugin = plugins.get(pluginIndex);
            boolean newState = !plugin.enabled;
            pluginManager.setPluginEnabled(plugin.id, newState);
            ((SwitchCell) view).setChecked(newState);
        }
    }

    private void updateItems() {
        items.clear();

        items.add(new Item(TYPE_HEADER, CAT_GENERAL, LocaleController.getString(R.string.JellogramCategoryGeneral)));
        items.add(new Item(TYPE_SWITCH, ITEM_SHOW_USER_ID, LocaleController.getString(R.string.JellogramShowUserId)));
        items.add(new Item(TYPE_SWITCH, ITEM_SHOW_CHAT_ID, LocaleController.getString(R.string.JellogramShowChatId)));

        items.add(new Item(TYPE_HEADER, CAT_APPEARANCE, LocaleController.getString(R.string.JellogramCategoryAppearance)));
        items.add(new Item(TYPE_SLIDER, ITEM_AVATAR_CORNER_RADIUS, LocaleController.getString(R.string.JellogramAvatarRadius)));
        items.add(new Item(TYPE_SWITCH, ITEM_ENABLE_MD3, LocaleController.getString(R.string.JellogramMd3)));
        items.add(new Item(TYPE_SWITCH, ITEM_MD3_SWITCHES, LocaleController.getString(R.string.JellogramMd3Switches)));
        items.add(new Item(TYPE_SWITCH, ITEM_SHOW_BORDER, LocaleController.getString(R.string.JellogramShowBorder)));

        items.add(new Item(TYPE_HEADER, CAT_CHATS, LocaleController.getString(R.string.JellogramCategoryChats)));
        items.add(new Item(TYPE_SWITCH, ITEM_CENTER_CHAT_TITLES, LocaleController.getString(R.string.JellogramCenterTitles)));
        items.add(new Item(TYPE_SWITCH, ITEM_HIDE_BOTTOM_TABS, LocaleController.getString(R.string.JellogramHideBottomTabs)));

        items.add(new Item(TYPE_HEADER, CAT_LINKS, LocaleController.getString(R.string.JellogramCategoryLinks)));
        items.add(new Item(TYPE_LINK_BUTTON, 0, LocaleController.getString(R.string.JellogramChannelLink)));
        items.add(new Item(TYPE_LINK_BUTTON, 1, LocaleController.getString(R.string.JellogramChatLink)));
        items.add(new Item(TYPE_LINK_BUTTON, 2, LocaleController.getString(R.string.JellogramTranslateLink)));
        items.add(new Item(TYPE_LINK_BUTTON, 3, LocaleController.getString(R.string.JellogramWebsiteLink)));
        items.add(new Item(TYPE_LINK_BUTTON, 4, LocaleController.getString(R.string.JellogramFaqLink)));
        items.add(new Item(TYPE_LINK_BUTTON, 5, LocaleController.getString(R.string.JellogramTosLink)));
        items.add(new Item(TYPE_LINK_BUTTON, 6, LocaleController.getString(R.string.JellogramSecurityLink)));

        items.add(new Item(TYPE_HEADER, CAT_OTHER, LocaleController.getString(R.string.JellogramCategoryOther)));
        items.add(new Item(TYPE_SWITCH, ITEM_CAMERA2_API, LocaleController.getString(R.string.JellogramCamera2)));

        // Plugins section
        List<PluginManager.PluginInfo> plugins = pluginManager.getPlugins();
        if (!plugins.isEmpty()) {
            items.add(new Item(TYPE_HEADER, CAT_PLUGINS, LocaleController.getString(R.string.JellogramPlugins)));
            for (int i = 0; i < plugins.size(); i++) {
                PluginManager.PluginInfo plugin = plugins.get(i);
                Item pluginItem = new Item(TYPE_SWITCH, ITEM_PLUGIN_START + i, plugin.title);
                pluginItem.pluginId = plugin.id;
                items.add(pluginItem);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_SLIDER = 1;
    private static final int TYPE_SWITCH = 2;
    private static final int TYPE_LINK_BUTTON = 3;
    private static final int TYPE_TOP_HEADER = 4;

    private static class Item {
        final int type;
        final int id;
        final CharSequence text;
        String pluginId;

        Item(int type, int id, CharSequence text) {
            this.type = type;
            this.id = id;
            this.text = text;
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        @Override
        public int getItemCount() {
            return items.size() + 1;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int pos = holder.getAdapterPosition();
            if (pos == 0) return false;
            Item item = items.get(pos - 1);
            return item.type == TYPE_SWITCH || item.type == TYPE_LINK_BUTTON;
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
                SliderCell cell = (SliderCell) holder.itemView;
                cell.setItem(item);
            } else if (holder.getItemViewType() == TYPE_LINK_BUTTON) {
                LinkButtonCell cell = (LinkButtonCell) holder.itemView;
                cell.setText(item.text);
            } else if (holder.getItemViewType() == TYPE_SWITCH) {
                SwitchCell cell = (SwitchCell) holder.itemView;
                boolean checked = false;
                switch (item.id) {
                    case ITEM_SHOW_USER_ID:
                        checked = settings.isShowUserId();
                        break;
                    case ITEM_SHOW_CHAT_ID:
                        checked = settings.isShowChatId();
                        break;
                    case ITEM_CAMERA2_API:
                        checked = SharedConfig.isUsingCamera2(currentAccount);
                        break;
                    case ITEM_CENTER_CHAT_TITLES:
                        checked = settings.isCenterChatTitles();
                        break;
                    case ITEM_ENABLE_MD3:
                        checked = settings.isMd3Enabled();
                        break;
                    case ITEM_SHOW_BORDER:
                        checked = settings.isShowBorder();
                        break;
                    case ITEM_HIDE_BOTTOM_TABS:
                        checked = settings.isHideBottomTabs();
                        break;
                    case ITEM_MD3_SWITCHES:
                        checked = settings.isMd3SwitchesEnabled();
                        break;
                    default:
                        if (item.id >= ITEM_PLUGIN_START) {
                            int pluginIndex = item.id - ITEM_PLUGIN_START;
                            List<PluginManager.PluginInfo> plugins = pluginManager.getPlugins();
                            if (pluginIndex >= 0 && pluginIndex < plugins.size()) {
                                checked = plugins.get(pluginIndex).enabled;
                            }
                        }
                        break;
                }
                cell.setText(item.text, checked, position != getItemCount() - 1);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return TYPE_TOP_HEADER;
            Item item = items.get(position - 1);
            return item.type;
        }
    }

    private class TopHeaderCell extends FrameLayout {

        private final ImageView iconView;
        private final TextView titleView;
        private final TextView versionView;

        public TopHeaderCell(Context context) {
            super(context);
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            iconView = new ImageView(context);
            iconView.setImageResource(R.drawable.jellogram_intro_logo);
            addView(iconView, LayoutHelper.createFrame(48, 48, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 24, 0, 0));

            titleView = new TextView(context);
            titleView.setText("Jellogram");
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
            titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            titleView.setGravity(Gravity.CENTER_HORIZONTAL);
            titleView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            addView(titleView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 80, 0, 0));

            versionView = new TextView(context);
            versionView.setText("v" + BuildVars.BUILD_VERSION_STRING + " (" + BuildVars.BUILD_VERSION + ")");
            versionView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            versionView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
            versionView.setGravity(Gravity.CENTER_HORIZONTAL);
            addView(versionView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 112, 0, 0));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(dp(140), MeasureSpec.EXACTLY)
            );
        }
    }

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
                if (value == 0) {
                    valueView.setText("Квадрат");
                } else if (value == 100) {
                    valueView.setText("Круг");
                } else {
                    valueView.setText(String.format("%d%%", value));
                }
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

    private class SwitchCell extends FrameLayout {

        private final TextView textView;
        private final Switch switchView;
        private final SwitchMD3 switchMd3View;

        public SwitchCell(Context context) {
            super(context);
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            textView = new TextView(context);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL, 21, 0, 75, 0));

            switchView = new Switch(context);
            switchView.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
            addView(switchView, LayoutHelper.createFrame(37, 40, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT), 19, 0, 19, 0));

            switchMd3View = new SwitchMD3(context);
            switchMd3View.setColors(
                Theme.key_md3SwitchTrack, Theme.key_md3SwitchTrackChecked,
                Theme.key_md3SwitchThumb, Theme.key_md3SwitchThumbChecked,
                Theme.key_md3SwitchIconChecked
            );
            addView(switchMd3View, LayoutHelper.createFrame(37, 40, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT), 19, 0, 19, 0));
            switchMd3View.setVisibility(View.GONE);
        }

        public void setText(CharSequence text, boolean checked, boolean divider) {
            textView.setText(text);
            boolean useMd3 = settings.isMd3SwitchesEnabled();
            switchView.setVisibility(useMd3 ? View.GONE : View.VISIBLE);
            switchMd3View.setVisibility(useMd3 ? View.VISIBLE : View.GONE);
            if (useMd3) {
                switchMd3View.setChecked(checked, false);
            } else {
                switchView.setChecked(checked, false);
            }
        }

        public void setChecked(boolean checked) {
            boolean useMd3 = settings.isMd3SwitchesEnabled();
            if (useMd3) {
                switchMd3View.setChecked(checked, true);
            } else {
                switchView.setChecked(checked, true);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(dp(50), MeasureSpec.EXACTLY)
            );
        }
    }

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