
package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.JellogramSettings;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SeekBarView;
import org.telegram.ui.Components.Switch;

import java.util.ArrayList;

public class JellogramSettingsActivity extends BaseFragment {

    private static final int ITEM_HEADER_GENERAL = 0;
    private static final int ITEM_SHOW_USER_ID = 1;
    private static final int ITEM_CAMERA2_API = 2;
    private static final int ITEM_AVATAR_CORNER_RADIUS = 3;
    private static final int ITEM_CENTER_CHAT_TITLES = 4;
    private static final int ITEM_ENABLE_MD3 = 5;

    private RecyclerListView listView;
    private ListAdapter adapter;
    private JellogramSettings settings;

    private final ArrayList<Item> items = new ArrayList<>();

    @Override
    public View createView(Context context) {
        settings = JellogramSettings.getInstance();

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Настройки Jellogram");
        actionBar.setBackgroundColor(Color.BLACK);
        actionBar.setTitleColor(Color.WHITE);
        actionBar.setItemsColor(Color.WHITE, false);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        ((FrameLayout) fragmentView).setBackgroundColor(Color.BLACK);

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
                    case ITEM_SHOW_USER_ID:
                        boolean showUserId = !settings.isShowUserId();
                        settings.setShowUserId(showUserId);
                        ((SwitchCell) view).setChecked(showUserId);
                        break;
                    case ITEM_CAMERA2_API:
                        SharedConfig.toggleUseCamera2(currentAccount);
                        ((SwitchCell) view).setChecked(SharedConfig.isUsingCamera2(currentAccount));
                        break;
                    case ITEM_CENTER_CHAT_TITLES:
                        boolean centerTitles = !settings.isCenterChatTitles();
                        settings.setCenterChatTitles(centerTitles);
                        ((SwitchCell) view).setChecked(centerTitles);
                        break;
                    case ITEM_ENABLE_MD3:
                        boolean md3 = !settings.isMd3Enabled();
                        settings.setMd3Enabled(md3);
                        ((SwitchCell) view).setChecked(md3);
                        break;
                }
            }
        });

        updateItems();
        return fragmentView;
    }

    private void updateItems() {
        items.clear();
        items.add(new Item(TYPE_HEADER, ITEM_HEADER_GENERAL, "Основные"));
        items.add(new Item(TYPE_SWITCH, ITEM_SHOW_USER_ID, "Показывать ID пользователя"));
        items.add(new Item(TYPE_SWITCH, ITEM_CAMERA2_API, "Использовать Camera 2 API"));
        items.add(new Item(TYPE_SLIDER, ITEM_AVATAR_CORNER_RADIUS, "Скругление аватарок"));
        items.add(new Item(TYPE_SWITCH, ITEM_CENTER_CHAT_TITLES, "Выравнивать заголовки чатов по центру"));
        items.add(new Item(TYPE_SWITCH, ITEM_ENABLE_MD3, "Включить Material Design 3"));
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_SLIDER = 1;
    private static final int TYPE_SWITCH = 2;

    private static class Item {
        final int type;
        final int id;
        final CharSequence text;

        Item(int type, int id, CharSequence text) {
            this.type = type;
            this.id = id;
            this.text = text;
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getItemViewType() == TYPE_SWITCH;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == TYPE_HEADER) {
                HeaderCell headerCell = new HeaderCell(parent.getContext(), Theme.key_windowBackgroundWhiteBlueHeader, 21, 15, false, resourceProvider);
                headerCell.setBackgroundColor(Color.BLACK);
                headerCell.setTextColor(Color.WHITE);
                view = headerCell;
            } else if (viewType == TYPE_SLIDER) {
                view = new SliderCell(parent.getContext());
            } else {
                view = new SwitchCell(parent.getContext());
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Item item = items.get(position);
            if (holder.getItemViewType() == TYPE_HEADER) {
                ((HeaderCell) holder.itemView).setText(item.text);
            } else if (holder.getItemViewType() == TYPE_SLIDER) {
                SliderCell cell = (SliderCell) holder.itemView;
                cell.setItem(item);
            } else if (holder.getItemViewType() == TYPE_SWITCH) {
                SwitchCell cell = (SwitchCell) holder.itemView;
                boolean checked = false;
                switch (item.id) {
                    case ITEM_SHOW_USER_ID:
                        checked = settings.isShowUserId();
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
                }
                cell.setText(item.text, checked, position != items.size() - 1);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).type;
        }
    }

    private class SliderCell extends FrameLayout {

        private final TextView titleView;
        private final TextView valueView;
        private final SeekBarView seekBarView;
        private Item item;

        public SliderCell(Context context) {
            super(context);
            setBackgroundColor(Color.parseColor("#111111"));
            setPadding(dp(21), dp(12), dp(21), dp(12));

            titleView = new TextView(context);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            titleView.setTextColor(Color.WHITE);
            titleView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            addView(titleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL));

            valueView = new TextView(context);
            valueView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            valueView.setTextColor(Color.parseColor("#AAAAAA"));
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

        public SwitchCell(Context context) {
            super(context);
            setBackgroundColor(Color.parseColor("#111111"));

            textView = new TextView(context);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setTextColor(Color.WHITE);
            textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL, 21, 0, 75, 0));

            switchView = new Switch(context);
            switchView.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
            addView(switchView, LayoutHelper.createFrame(37, 40, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT), 19, 0, 19, 0));
        }

        public void setText(CharSequence text, boolean checked, boolean divider) {
            textView.setText(text);
            switchView.setChecked(checked, false);
        }

        public void setChecked(boolean checked) {
            switchView.setChecked(checked, true);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(dp(50), MeasureSpec.EXACTLY)
            );
        }
    }
}
