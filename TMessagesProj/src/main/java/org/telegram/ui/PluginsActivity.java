package org.telegram.ui;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.lua.LuaPlugin;
import org.telegram.messenger.lua.PluginManager;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.PluginCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import java.util.List;

public class PluginsActivity extends BaseFragment {
    private RecyclerListView listView;
    private PluginManager pluginManager;
    private PluginsAdapter adapter;
    private FrameLayout emptyView;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString(R.string.JellogramPlugins));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));

        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(adapter = new PluginsAdapter(context));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        emptyView = new FrameLayout(context);
        emptyView.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));
        TextInfoPrivacyCell emptyTextView = new TextInfoPrivacyCell(context);
        emptyTextView.setText(LocaleController.getString(R.string.JellogramPluginNotFound));
        emptyView.addView(emptyTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 17, 16, 32, 16, 32));
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        pluginManager = PluginManager.getInstance(context);
        pluginManager.loadAllPlugins();
        updatePluginsList();

        return fragmentView = frameLayout;
    }

    private void updatePluginsList() {
        List<LuaPlugin> plugins = pluginManager.getAllPlugins();
        boolean hasPlugins = !plugins.isEmpty();

        emptyView.setVisibility(hasPlugins ? View.GONE : View.VISIBLE);
        listView.setVisibility(hasPlugins ? View.VISIBLE : View.GONE);

        if (adapter != null) {
            adapter.setPlugins(plugins);
            adapter.notifyDataSetChanged();
        }
    }

    private void confirmUninstall(LuaPlugin plugin) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LocaleController.getString(R.string.JellogramPlugins));
        builder.setMessage(LocaleController.formatString("Uninstall plugin %s?", plugin.getName()));
        builder.setPositiveButton(LocaleController.getString(R.string.Delete), (dialog, which) -> {
            try {
                org.telegram.messenger.PluginManager.getInstance().removePlugin(plugin.getId());
                pluginManager.shutdown();
                pluginManager.loadAllPlugins();
                updatePluginsList();
            } catch (Exception e) {
                FileLog.e(e);
            }
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePluginsList();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (pluginManager != null) {
            pluginManager.shutdown();
        }
    }

    private class PluginsAdapter extends RecyclerListView.SelectionAdapter {
        private Context mContext;
        private List<LuaPlugin> plugins;

        public PluginsAdapter(Context context) {
            mContext = context;
        }

        public void setPlugins(List<LuaPlugin> plugins) {
            this.plugins = plugins;
        }

        @Override
        public int getItemCount() {
            return plugins != null ? plugins.size() + 1 : 1;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view;
            if (viewType == 0) {
                HeaderCell headerCell = new HeaderCell(mContext, Theme.key_windowBackgroundWhiteBlueHeader, 21, 15, false);
                headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                view = headerCell;
            } else {
                view = new PluginCell(mContext);
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position == 0) {
                HeaderCell headerCell = (HeaderCell) holder.itemView;
                headerCell.setText(LocaleController.getString(R.string.JellogramPlugins));
            } else if (holder.itemView instanceof PluginCell && plugins != null) {
                PluginCell cell = (PluginCell) holder.itemView;
                LuaPlugin plugin = plugins.get(position - 1);
                cell.setPlugin(plugin);
                cell.setOnDeleteClickListener(p -> confirmUninstall(p));
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? 0 : 1;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return false;
        }
    }
}
