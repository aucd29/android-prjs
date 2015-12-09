package net.sarangnamu.apk_extractor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.PopupMenu.OnMenuItemClickListener;

import net.sarangnamu.apk_extractor.cfg.Cfg;
import net.sarangnamu.apk_extractor.dlg.DlgEmail;
import net.sarangnamu.apk_extractor.dlg.DlgSortBy;
import net.sarangnamu.apk_extractor.dlg.DlgSpecialThanks;
import net.sarangnamu.common.BkCfg;
import net.sarangnamu.common.BkFile;
import net.sarangnamu.common.BkString;
import net.sarangnamu.common.DimTool;
import net.sarangnamu.common.ani.FadeColor;
import net.sarangnamu.common.explorer.DirChooserActivity;
import net.sarangnamu.common.fonts.FontLoader;
import net.sarangnamu.common.ui.MenuManager;
import net.sarangnamu.common.ui.dlg.DlgLicense;
import net.sarangnamu.common.ui.dlg.DlgTimer;
import net.sarangnamu.common.ui.list.AniBtnListView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final long SHOW_PROGRESS = 2000000;
    private static final int SHOW_POPUP = 1;
    private static final int UPDATE_PROGRESS_BAR = 2;
    private static final int HIDE_PROGRESS_BAR = 3;

    private static final int SLIDING_MARGIN = 160;

    private static final int ET_SDCARD = 0;
    private static final int ET_EMAIL  = 1;
    private static final int ET_MENU   = 2;
    private static final int ET_DELETE = 3;

    private static final int EMAIL_ACTIVITY = 100;
    private static final int DIR_ACTIVITY = 200;
    private static final int DEL_ACTIVITY = 300;

    private boolean mSendEmail = false, mSearchedList = false;
    private int mDeletedPosition = -1;
    private TextView mTitle, mPath, mDev, mSearch, mEmpty;
    private EditText mEdtSearch;
    private AppAdapter mAdapter;
    private ImageButton mMenu;
    private RelativeLayout mTitleBar;
    private ProgressBar mSdProgressBar;
    private ProgressDialog mDlg;
    private ArrayList<AppList.PkgInfo> mPkgInfoList;
    private ArrayList<AppList.PkgInfo> mPkgInfoSearchedList;

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // HANDLER
    //
    ////////////////////////////////////////////////////////////////////////////////////

    private Handler mHandler = new Handler(new ProcessHandler());

    private class ProcessHandler implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_POPUP:
                    String fileName = (String) msg.obj;

                    DlgTimer dlg = new DlgTimer(MainActivity.this, R.layout.dlg_timer_extract_file);
                    dlg.setMessage(fileName);
                    dlg.setTime(1500);
                    dlg.show();
                    dlg.setTransparentBaseLayout();
                    break;

                case UPDATE_PROGRESS_BAR:
                    mSdProgressBar.setProgress(msg.arg1);
                    break;

                case HIDE_PROGRESS_BAR:
                    mSdProgressBar.setVisibility(View.GONE);
                    mSdProgressBar.setProgress(0);
                    break;
            }

            return true;
        }
    }

    private void sendMessage(int type, Object obj) {
        Message msg = mHandler.obtainMessage();
        msg.what = type;
        msg.obj = obj;
        mHandler.sendMessage(msg);
    }

    private void sendMessage(int type, int arg) {
        Message msg = mHandler.obtainMessage();
        msg.what = type;
        msg.arg1 = arg;
        mHandler.sendMessage(msg);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // SYSTEM METHODS
    //
    ////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle         = (TextView) findViewById(R.id.title);
        mPath          = (TextView) findViewById(R.id.path);
        mDev           = (TextView) findViewById(R.id.dev);
        mSearch        = (TextView) findViewById(R.id.tvSearch);
        mEmpty         = (TextView) findViewById(android.R.id.empty);
        mEdtSearch     = (EditText) findViewById(R.id.search);
        mMenu          = (ImageButton) findViewById(R.id.menu);
        mTitleBar      = (RelativeLayout) findViewById(R.id.titleBar);
        mSdProgressBar = (ProgressBar) findViewById(R.id.sdProgressBar);

        initLabel();
        initMenu();
        initSearch();
        initData(true);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case DIR_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    String path = intent.getStringExtra("path");
                    if (path != null) {
                        Cfg.setUserPath(this, path);
                        setDownloadPath();
                    }
                }
                break;

            case EMAIL_ACTIVITY:
                if (resultCode == RESULT_OK) {
                } else if (resultCode == RESULT_CANCELED) {
                } else {
                    showPopup(getString(R.string.sendMailFail));
                }
                break;

            case DEL_ACTIVITY:
                if (mDeletedPosition == -1) {
                    return ;
                }

                AppList.PkgInfo info = getPkgInfo(mDeletedPosition);
                try {
                    getPackageManager().getApplicationInfo(info.pkgName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    removeDataListAndRefereshList(mDeletedPosition);
                }

                mDeletedPosition = -1;
                break;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            mMenu.performClick();
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (mSearchedList) {
            mPkgInfoSearchedList.clear();
            mSearchedList = false;
            notifyDataSetChanged();

            return;
        } else if (mEdtSearch.getVisibility() != View.GONE) {
            setSearchUi();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        BkCfg.hideKeyboard(mEdtSearch);

        super.onPause();
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // PRIVATE
    //
    ////////////////////////////////////////////////////////////////////////////////////

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    private void initLabel() {
        mTitle.setText(Html.fromHtml(getString(R.string.appName)));

        setDownloadPath();

        String src = String.format("<b>%s</b> <a href='http://sarangnamu.net'>@aucd29</a>", getString(R.string.dev));
        mDev.setText(Html.fromHtml(src));
        mSdProgressBar.setMax(100);
    }

    private void setDownloadPath() {
        String dnPath = Cfg.getDownPath(this);

        String src = String.format("<b>%s</b> : %s", getString(R.string.downloadPath), dnPath.replace(BkCfg.sdPath(), "/sdcard"));
        mPath.setText(Html.fromHtml(src));
    }

    private void initMenu() {
        MenuManager.getInstance().setListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.mnu_search:
                        setSearchUi();
                        break;
                    case R.id.mnu_email:
                        showEmailDlg();
                        break;
                    case R.id.mnu_license:
                        showLicenseDlg();
                        break;
                    case R.id.mnu_setSdPath:
                        showFileExplorer();
                        break;
                    case R.id.mnu_showSystemApp:
                        showSystemApp();
                        break;
                    case R.id.mnu_showInstalledApp:
                        showInstalledApp();
                        break;
                    case R.id.mnu_specialThanks:
                        showSpecialThanks();
                        break;
                    case R.id.mnu_sortBy:
                        showSortBy();
                        break;
                }

                return false;
            }

            void showEmailDlg() {
                DlgEmail dlg = new DlgEmail(MainActivity.this);
                dlg.show();
            }

            void showLicenseDlg() {
                DlgLicense dlg = new DlgLicense(MainActivity.this);
                dlg.setTitleTypeface(FontLoader.getInstance(getApplicationContext()).getRobotoLight());
                dlg.show();
            }

            void showSpecialThanks() {
                DlgSpecialThanks dlg = new DlgSpecialThanks(MainActivity.this);
                dlg.setTitleTypeface(FontLoader.getInstance(getApplicationContext()).getRobotoLight());
                dlg.setTitle("Special Thanks");
                dlg.show();
            }

            void showSortBy() {
                DlgSortBy dlg = new DlgSortBy(MainActivity.this);
                dlg.setTitle(R.string.mnu_sortBy);
                dlg.show();
                dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        initData(false);
                    }
                });
            }
        });

        mMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int resid;

                if (getShowOption()) {
                    resid = R.menu.main;
                } else {
                    resid = R.menu.main2;
                }

                MenuManager.getInstance().showMenu(MainActivity.this, v, resid);
            }
        });
    }

    private void initSearch() {
        BkCfg.forceHideKeyboard(getWindow());

        mEdtSearch.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mEdtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mPkgInfoSearchedList == null) {
                    mPkgInfoSearchedList = new ArrayList<AppList.PkgInfo>();
                }

                mPkgInfoSearchedList.clear();
                String keyword = mEdtSearch.getText().toString();

                if (keyword != null && keyword.length() > 0) {
                    mSearchedList = true;
                    keyword = keyword.toLowerCase();

                    for (AppList.PkgInfo info : mPkgInfoList) {
                        if (info.appName.toLowerCase().contains(keyword)) {
                            mPkgInfoSearchedList.add(info);
                        }
                    }
                } else {
                    mSearchedList = false;
                }

                notifyDataSetChanged();
            }
        });

        mEdtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    setSearchUi();
                }

                return false;
            }
        });

        mEdtSearch.setTypeface(FontLoader.getInstance(MainActivity.this).getFont("Roboto-Light"));
        BkCfg.hideKeyboard(mEdtSearch);
    }

    private void setSearchUi() {
        if (mEdtSearch.getVisibility() == View.GONE) {
            mEdtSearch.setVisibility(View.VISIBLE);
            mSearch.setVisibility(View.VISIBLE);
            mTitle.setVisibility(View.GONE);
            FadeColor.startResource(mTitleBar, R.color.dBg, R.color.dBgSearch, null);

            mEdtSearch.setText("");

            BkCfg.showKeyboard(mEdtSearch);
        } else {
            mEdtSearch.setVisibility(View.GONE);
            mSearch.setVisibility(View.GONE);
            mTitle.setVisibility(View.VISIBLE);
            FadeColor.startResource(mTitleBar, R.color.dBgSearch, R.color.dBg, null);

            BkCfg.hideKeyboard(mEdtSearch);
        }
    }

    private AppList.PkgInfo getPkgInfo(int position) {
        if (mSearchedList) {
            return mPkgInfoSearchedList.get(position);
        } else {
            return mPkgInfoList.get(position);
        }
    }

    private void removeDataListAndRefereshList(int pos) {
        if (mSearchedList) {
            mPkgInfoSearchedList.remove(mDeletedPosition);
        } else {
            mPkgInfoList.remove(mDeletedPosition);
        }

        notifyDataSetChanged();
    }

    private void initData(final boolean initList) {
        new AsyncTask<Context, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                showProgress();
            }

            @Override
            protected Boolean doInBackground(Context... contexts) {
                Context context = contexts[0];

                if (mPkgInfoList != null) {
                    mPkgInfoList.clear();
                    mPkgInfoList = null;
                }

                mPkgInfoList = AppList.getInstance().getAllApps(context
                        , getShowOption()
                        , getSortByOption());

                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                mDlg.dismiss();

                if (initList) {
                    initListView();
                } else {
                    AniBtnListView list = (AniBtnListView) getListView();
                    list.resetCheckedList();
                    notifyDataSetChanged();
                }
            }
        }.execute(getApplicationContext());
    }

    private void notifyDataSetChanged() {
        BaseAdapter tmpAdapter = (BaseAdapter) getListAdapter();
        if (tmpAdapter != null) {
            tmpAdapter.notifyDataSetChanged();
        }
    }

    private void showProgress() {
        mDlg = new ProgressDialog(MainActivity.this);
        mDlg.setCancelable(false);
        mDlg.setMessage(getString(R.string.plsWait));
        mDlg.show();
        mDlg.setContentView(R.layout.dlg_progress);
    }

    private void initListView() {
        mAdapter = new AppAdapter();
        setListAdapter(mAdapter);

        AniBtnListView list = (AniBtnListView) getListView();
        list.setSlidingMargin(SLIDING_MARGIN);
        list.setBtnLayoutId(R.id.btnLayout);
        list.setRowId(R.id.row);
        list.setEmptyView(mEmpty);
    }

    private void sendToSd(int position) {
        final AppList.PkgInfo info = getPkgInfo(position);
        if (info.size > SHOW_PROGRESS) {
            showProgress();
            mSdProgressBar.setVisibility(View.VISIBLE);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File src = new File(info.srcDir);

                    String fileName = BkString.getFileName(info.srcDir);
                    int pos = fileName.lastIndexOf("-");
                    if (pos != -1) {
                        fileName = fileName.substring(0, pos);
                        fileName += "-" + info.versionName + ".apk";
                    } else {
                        fileName = fileName.replace(".apk", "-" + info.versionName + ".apk");
                    }

                    BkFile.copyFileTo(src, Cfg.getDownPath(MainActivity.this) + fileName, new BkFile.FileCopyDetailListener() {
                        long fileSize;
                        private boolean cancelFlag = false;

                        @Override
                        public void onCancelled() {
                        }

                        @Override
                        public boolean isCancelled() {
                            return cancelFlag;
                        }

                        @Override
                        public void onFinish(String name) {
                            if (info.size > SHOW_PROGRESS) {
                                sendMessage(HIDE_PROGRESS_BAR, null);
                                mDlg.dismiss();
                            }

                            if (mSendEmail) {
                                sendToEmail(info, name);
                            } else {
                                String fileName = BkString.getFileName(name);
                                sendMessage(SHOW_POPUP, fileName);
                            }
                        }

                        @Override
                        public void onProcess(int percent) {
                            sendMessage(UPDATE_PROGRESS_BAR, percent);
                        }

                        @Override
                        public void onFileSize(long size) {
                            fileSize = size;
                        }

                        @Override
                        public long getFileSize() {
                            return fileSize;
                        }

                        @Override
                        public void onError(String errMsg) {
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendToEmail(AppList.PkgInfo info, String target) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");

        String regEmail = Cfg.getEmail(MainActivity.this);

        if (regEmail == null) {
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] {});
        } else {
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] { regEmail });
        }

        intent.putExtra(Intent.EXTRA_SUBJECT, "[APK Extractor] Backup " + info.appName);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + target));
        // intent.putExtra(Intent.EXTRA_TEXT, "");

        try {
            startActivityForResult(Intent.createChooser(intent, "Send mail..."), EMAIL_ACTIVITY);
        } catch (android.content.ActivityNotFoundException ex) {
            showPopup(getString(R.string.errorEmail));
        }
    }

    private void showPopup(String msg) {
        DlgTimer dlg = new DlgTimer(MainActivity.this, R.layout.dlg_timer);
        dlg.setMessage(msg);
        dlg.setTime(1000);
        dlg.show();
        dlg.setTransparentBaseLayout();
    }

    private int dpToPixelInt(int dp) {
        return DimTool.dpToPixelInt(getApplicationContext(), dp);
    }

    private void showFileExplorer() {
        Intent intent = new Intent(this, DirChooserActivity.class);

        startActivityForResult(intent, DIR_ACTIVITY);
    }

    private void showSystemApp() {
        Cfg.setShowOption(MainActivity.this, "1");
        initData(false);
    }

    private void showInstalledApp() {
        Cfg.setShowOption(MainActivity.this, "0");
        initData(false);
    }

    private boolean getShowOption() {
        String opt = Cfg.getShowOption(MainActivity.this);
        if (opt.equals("0")) {
            return true;
        }

        return false;
    }

    private String getSortByOption() {
        String opt = Cfg.getSortBy(MainActivity.this);
        return opt;
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // APP ADAPTER
    //
    ////////////////////////////////////////////////////////////////////////////////////

    class ViewHolder {
        ImageView icon;
        TextView name, size, pkgName, version, sd, email, delete;
        LinearLayout btnLayout;
        RelativeLayout row;
    }

    class PosHolder {
        int position;
        int type;
        View row;

        public PosHolder(int pos, int type, View row) {
            this.position = pos;
            this.type = type;
            this.row = row;
        }
    }

    class AppAdapter extends BaseAdapter {
        public int margin;

        public AppAdapter() {
            margin = dpToPixelInt(SLIDING_MARGIN) * -1;
        }

        @Override
        public int getCount() {
            if (mSearchedList) {
                if (mPkgInfoSearchedList == null) {
                    return 0;
                }

                return mPkgInfoSearchedList.size();
            }

            return mPkgInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressWarnings("deprecation")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item, null);

                holder = new ViewHolder();
                holder.icon      = (ImageView) convertView.findViewById(R.id.icon);
                holder.name      = (TextView) convertView.findViewById(R.id.name);
                holder.size      = (TextView) convertView.findViewById(R.id.size);
                holder.pkgName   = (TextView) convertView.findViewById(R.id.pkgName);
                holder.version   = (TextView) convertView.findViewById(R.id.version);
                holder.sd        = (TextView) convertView.findViewById(R.id.sd);
                holder.email     = (TextView) convertView.findViewById(R.id.email);
                holder.delete    = (TextView) convertView.findViewById(R.id.delete);
                holder.row       = (RelativeLayout) convertView.findViewById(R.id.row);
                holder.btnLayout = (LinearLayout) convertView.findViewById(R.id.btnLayout);

                holder.sd.setOnClickListener(MainActivity.this);
                holder.email.setOnClickListener(MainActivity.this);
                holder.delete.setOnClickListener(MainActivity.this);
                holder.row.setOnClickListener(MainActivity.this);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AppList.PkgInfo info = getPkgInfo(position);
            if (info.icon != null) {
                if (Build.VERSION_CODES.JELLY_BEAN < Build.VERSION.SDK_INT) {
                    holder.icon.setBackground(info.icon);
                } else {
                    holder.icon.setBackgroundDrawable(info.icon);
                }
            }
            holder.name.setText(info.appName);
            holder.size.setText(info.appSize);
            holder.pkgName.setText(info.pkgName);
            holder.version.setText("(" + info.versionName + ")");

            holder.sd.setTag(new PosHolder(position, ET_SDCARD, holder.row));
            holder.email.setTag(new PosHolder(position, ET_EMAIL, holder.row));
            holder.delete.setTag(new PosHolder(position, ET_DELETE, holder.row));
            holder.row.setTag(new PosHolder(position, ET_MENU, holder.row));

            return convertView;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // View.OnClickListener
    //
    ////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onClick(View v) {
        PosHolder ph = (PosHolder) v.getTag();

        if (ph.type == ET_MENU) {
            if (mEdtSearch.getVisibility() != View.GONE) {
                mEdtSearch.setVisibility(View.GONE);
                mSearch.setVisibility(View.GONE);
                mTitle.setVisibility(View.VISIBLE);
                FadeColor.startResource(mTitleBar, R.color.dBgSearch, R.color.dBg, null);

                BkCfg.hideKeyboard(mEdtSearch);
            }

            ((AniBtnListView) getListView()).showAnimation(v);
        } else if (ph.type == ET_DELETE) {
            AppList.PkgInfo info = getPkgInfo(ph.position);
            mDeletedPosition = ph.position;

            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + info.pkgName));

            startActivityForResult(intent, DEL_ACTIVITY);
        } else {
            mSendEmail = ph.type == 0 ? false : true;
            sendToSd(ph.position);
        }
    }

    public ListView getListView() {
        return (ListView) findViewById(android.R.id.list);
    }

    public void setListAdapter(BaseAdapter adapter) {
        getListView().setAdapter(adapter);
    }

    public ListAdapter getListAdapter() {
        return getListView().getAdapter();
    }
}
