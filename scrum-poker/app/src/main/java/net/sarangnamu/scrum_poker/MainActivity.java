package net.sarangnamu.scrum_poker;

import java.util.ArrayList;

import net.sarangnamu.common.ui.BkActivity;
import net.sarangnamu.scrum_poker.db.DbHelper;
import net.sarangnamu.scrum_poker.page.PageManager;
import net.sarangnamu.scrum_poker.page.sub.AddFrgmt;
import net.sarangnamu.scrum_poker.page.sub.MainFrgmt;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import butterknife.Bind;

public class MainActivity extends BkActivity {
    private static final Logger mLog = LoggerFactory.getLogger(MainActivity.class);

    @Bind(R.id.drawer_layout) DrawerLayout mDrawer;
    @Bind(R.id.content_frame) FrameLayout mContentFrame;
    @Bind(R.id.naviView) NavigationView mNaviView;
    @Bind(R.id.fab) FloatingActionButton mFab;

    private ArrayList<MenuData> mMenuData;
    private ActionBarDrawerToggle mToggleActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            initPageManager();
        }

        initDrawer();
        initFloatingActionButton();
        initLeftMenu();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onBackPressed() {
        int count = PageManager.getInstance(MainActivity.this).getChildCount();
        if (count == 1) {
            setAddButtonAlpha(1);
        }

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DbHelper.getInstance().closeRealm(getApplicationContext());
    }

    private void initPageManager() {
        PageManager.getInstance(this).add(R.id.content_frame, MainFrgmt.class);
    }

    private void initDrawer() {
        mToggleActionBar = new ActionBarDrawerToggle(this, mDrawer, R.string.app_name, R.string.app_name);

//        mDrawer.setDrawerListener(mToggleActionBar);
//        mDrawer.setScrimColor(Color.TRANSPARENT);
//        mDrawer.setDrawerListener(new ContentSlidingDrawerListener() {
//            @Override
//            public View getListView() {
//                return leftMenu;
//            }
//
//            @Override
//            public View getContentFrame() {
//                return contentFrame;
//            }
//        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mToggleActionBar.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mToggleActionBar.onConfigurationChanged(newConfig);
    }

    private void initLeftMenu() {
        if (mMenuData == null) {
            mMenuData = new ArrayList<MenuData>();
        }

        mMenuData.add(new MenuData(LEFT_MENU_TYPE_BAR, getString(R.string.app_name)));
        mMenuData.add(new MenuData(LEFT_MENU_TYPE_ITEM, getString(R.string.add_rule)));

//        DbManager.getInstance().open(this, new DbHelper(this));
//        Cursor cr = DbHelper.select();
//        if (cr.getCount() > 0) {
//            mMenuData.add(new MenuData(LEFT_MENU_TYPE_BAR, getString(R.string.user_rule)));
//
//            while (cr.moveToNext()) {
//                MenuData mnuData = new MenuData(LEFT_MENU_TYPE_DB, cr.getString(1));
//                mnuData.primaryKey = cr.getInt(0);
//
//                mLog.debug("@@ USER RULE : " + cr.getString(1) + ", (" + cr.getInt(0) + ") " + cr.getString(2));
//
//                mMenuData.add(mnuData);
//            }
//        }

        mMenuData.add(new MenuData(LEFT_MENU_TYPE_BAR, getString(R.string.about)));
        mMenuData.add(new MenuData(LEFT_MENU_TYPE_ITEM, getString(R.string.license)));

//        leftMenu.setAdapter(new MenuAdapter());
//        leftMenu.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (mMenuData.get(position).menu.equals(getString(R.string.license))) {
//                    DlgLicense dlg = new DlgLicense(MainActivity.this);
//                    dlg.setTitleTypeface(FontLoader.getInstance(getApplicationContext()).getRobotoLight());
//                    dlg.show();
//                } else if (mMenuData.get(position).menu.equals(getString(R.string.add_rule))) {
////                    PageManager.getInstance(MainActivity.this).replace(R.id.content_frame, AddFrgmt.class);
//                } else if (mMenuData.get(position).type == LEFT_MENU_TYPE_DB) {
//                    Cfg.set(getApplicationContext(), Cfg.DB_ID, mMenuData.get(position).primaryKey + "");
//                }
//
//                mDrawer.closeDrawers();
//            }
//        });
    }

    private void initFloatingActionButton() {
        mFab.setOnClickListener(v -> {
            setAddButtonAlpha(0);
            PageManager.getInstance(MainActivity.this).replace(R.id.content_frame, AddFrgmt.class, null);
        });
    }

    public void setAddButtonAlpha(int value) {
        mFab.animate().alpha(value);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // MENU ADAPTER
    //
    ////////////////////////////////////////////////////////////////////////////////////

    private static final int LEFT_MENU_TYPE_BAR  = 0;
    private static final int LEFT_MENU_TYPE_ITEM = 1;
    private static final int LEFT_MENU_TYPE_DB   = 2;

    class MenuData {
        public MenuData(int type, String menu) {
            this.type = type;
            this.menu = menu;
        }

        int primaryKey;
        int type;
        String menu;
    }

    class MenuViewHolder {
        TextView menu;
    }

    class MenuAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (mMenuData == null) {
                return 0;
            }

            return mMenuData.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            return mMenuData.get(position).type;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public boolean isEnabled(int position) {
            if (mMenuData.get(position).type == LEFT_MENU_TYPE_BAR) {
                return false;
            }

            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MenuViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(getInflateId(position), null);
                holder = new MenuViewHolder();

                holder.menu = (TextView) convertView.findViewById(R.id.title);


                convertView.setTag(holder);
            } else {
                holder = (MenuViewHolder) convertView.getTag();
            }

            MenuData data = mMenuData.get(position);
            holder.menu.setText(data.menu);

            return convertView;
        }

        private int getInflateId(int position) {
            switch (mMenuData.get(position).type) {
            case LEFT_MENU_TYPE_BAR:
                return R.layout.page_main_menu_bar;
            default:
                return R.layout.page_main_menu_item;
            }
        }
    }
}
