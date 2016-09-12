package net.sarangnamu.apk_extractor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.sarangnamu.apk_extractor.control.AppAdapter;
import net.sarangnamu.apk_extractor.control.BackupUtil;
import net.sarangnamu.apk_extractor.control.DialogDelegate;
import net.sarangnamu.apk_extractor.model.AppList;
import net.sarangnamu.apk_extractor.control.AppListManager;
import net.sarangnamu.apk_extractor.model.Cfg;
import net.sarangnamu.apk_extractor.control.receiver.Receiver;
import net.sarangnamu.common.BkCfg;
import net.sarangnamu.common.BkFile;
import net.sarangnamu.common.BkString;
import net.sarangnamu.common.DimTool;
import net.sarangnamu.common.ani.FadeColor;
import net.sarangnamu.common.ani.FadeStatusBar;
import net.sarangnamu.common.explorer.DirChooserActivity;
import net.sarangnamu.common.fonts.FontLoader;
import net.sarangnamu.common.permission.RunTimePermission;
import net.sarangnamu.common.ui.MenuManager;
import net.sarangnamu.common.ui.dlg.DlgTimer;
import net.sarangnamu.common.ui.list.AniBtnListView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final Logger mLog = LoggerFactory.getLogger(MainActivity.class);

    private static final int SHOW_POPUP = 1;
    private static final int UPDATE_PROGRESS_BAR = 2;
    private static final int HIDE_PROGRESS_BAR = 3;

    private static final int SLIDING_MARGIN = 160;

    private static final int SHARING_ACTIVITY = 100;
    private static final int DIR_ACTIVITY = 200;
    private static final int DEL_ACTIVITY = 300;

    private boolean mSendEmail = false;
    private int mDeletedPosition = -1;
    private TextView mTitle, mPath, mDev, mSearch, mEmpty;
    private EditText mEdtSearch;
    private AppAdapter mAdapter;
    private ImageButton mMenu;
    private RelativeLayout mTitleBar;
    private ProgressBar mSdProgressBar;


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
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String appEvent = intent.getStringExtra(Receiver.APPEVENT);
        String pkgName  = intent.getStringExtra(Receiver.PKGNAME);

        if (mLog.isDebugEnabled()) {
            String log = "";
            log += "===================================================================\n";
            log += "APPEVENT: " + appEvent + "\n";
            log += "    NAME: " + pkgName + "\n";
            log += "===================================================================\n";
            mLog.debug(log);
        }

        if (!TextUtils.isEmpty(appEvent) && !TextUtils.isEmpty(pkgName)) {
            runTimeUpdatePkgInfoList(appEvent, pkgName);
        }
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

            case SHARING_ACTIVITY:
                if (resultCode == RESULT_OK) {
                } else if (resultCode == RESULT_CANCELED) {
                } else {
                    showPopup(getString(R.string.sendMailFail));
                }
                break;

            case DEL_ACTIVITY:
                if (mDeletedPosition == -1) {
                    return;
                }

                AppList.PkgInfo info = AppListManager.getInstance().getPkgInfo(mDeletedPosition);
                try {
                    getPackageManager().getApplicationInfo(info.pkgName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    AppListManager.getInstance().removeDataListAndRefereshList(mDeletedPosition);

                    notifyDataSetChanged();
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
        if (AppListManager.getInstance().isSearchedList()) {
            AppListManager.getInstance().resetSearchedList();
            if (mEdtSearch.getVisibility() != View.GONE) {
                setSearchUi();
            }

            return;
        } else if (mEdtSearch.getVisibility() != View.GONE) {
            setSearchUi();
            return;
        } else if (((AniBtnListView) getListView()).isShowMenu()) {
            ((AniBtnListView) getListView()).hideMenu();
            return ;
        }

        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        BkCfg.hideKeyboard(mEdtSearch);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        String appEvent = BkCfg.get(getApplicationContext(), Receiver.APPEVENT, null);
        String pkgName  = BkCfg.get(getApplicationContext(), Receiver.PKGNAME, null);

        if (mLog.isDebugEnabled()) {
            String log = "";
            log += "===================================================================\n";
            log += "APPEVENT: " + appEvent + "\n";
            log += "    NAME: " + pkgName + "\n";
            log += "===================================================================\n";
            mLog.debug(log);
        }

        if (!TextUtils.isEmpty(appEvent) && !TextUtils.isEmpty(pkgName)) {
            runTimeUpdatePkgInfoList(appEvent, pkgName);
            resetAppEventPreference();
        }
    }

    private void runTimeUpdatePkgInfoList(String type, String pkgName) {
        if (Receiver.APP_REMOVED.equals(type)) {
            if (mLog.isDebugEnabled()) {
                mLog.debug("rumtime remove");
            }

            uninstallPkgInfo(pkgName, AppListManager.getInstance().getPkgInfoList());
            notifyDataSetChanged();
        } else if (Receiver.APP_ADDED.equals(type)) {
            if (mLog.isDebugEnabled()) {
                mLog.debug("runtime add");
            }

            AppList.PkgInfo info = AppList.getInstance().getPkgInfo(getApplicationContext(), pkgName);
            if (info == null) {
                mLog.error("ERROR: info == null");
                return ;
            }

            AppListManager.getInstance().updatePkgInfoList(info, mEdtSearch.getText().toString());
            notifyDataSetChanged();
        }
    }

    private void uninstallPkgInfo(String pkgName, ArrayList<AppList.PkgInfo> list) {
        if (list != null && list.size() > 0) {
            for (AppList.PkgInfo info : list) {
                if (info.pkgName.equals(pkgName)) {
                    list.remove(info);
                    break;
                }
            }
        }
    }

    private void resetAppEventPreference() {
        BkCfg.set(getApplicationContext(), Receiver.APPEVENT, null);
        BkCfg.set(getApplicationContext(), Receiver.PKGNAME, null);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // PRIVATE
    //
    ////////////////////////////////////////////////////////////////////////////////////

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
        MenuManager.getInstance().setListener(item -> {
            switch (item.getItemId()) {
                case R.id.mnu_search:               setSearchUi();          break;
                case R.id.mnu_search_from_mirror:   searchFromMirror();     break;
                case R.id.mnu_license:
                    DialogDelegate.getInstance().showLicenseDlg(MainActivity.this);
                    break;
                case R.id.mnu_setSdPath:            showFileExplorer();     break;
                case R.id.mnu_showSystemApp:        showSystemApp();        break;
                case R.id.mnu_showInstalledApp:     showInstalledApp();     break;
                case R.id.mnu_specialThanks:
                    DialogDelegate.getInstance().showSpecialThanks(MainActivity.this);
                    break;
                case R.id.mnu_sortBy:
                    DialogDelegate.getInstance().showSortBy(MainActivity.this);
                    break;
            }

            return false;
        });

        mMenu.setOnClickListener(v -> {
            MenuManager.getInstance().showMenu(MainActivity.this, v,
                    Cfg.getShowOption(MainActivity.this).equals("0") ? R.menu.main : R.menu.main2);
        });
    }

    private void initSearch() {
        BkCfg.forceHideKeyboard(getWindow());

        mEdtSearch.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mEdtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) {
                AppListManager.getInstance().afterTextChanged(mEdtSearch.getText().toString());

                notifyDataSetChanged();
            }
        });

        mEdtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                setSearchUi();
            }

            return false;
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
            FadeStatusBar.start(getWindow(), R.color.colorPrimaryDark, R.color.dBgSearch, null);

            mEdtSearch.setText("");

            BkCfg.showKeyboard(mEdtSearch);

            AniBtnListView list = (AniBtnListView) getListView();
            list.hideMenu();
        } else {
            mEdtSearch.setVisibility(View.GONE);
            mSearch.setVisibility(View.GONE);
            mTitle.setVisibility(View.VISIBLE);
            FadeColor.startResource(mTitleBar, R.color.dBgSearch, R.color.dBg, null);
            FadeStatusBar.start(getWindow(), R.color.dBgSearch, R.color.colorPrimaryDark, null);

            BkCfg.hideKeyboard(mEdtSearch);
        }
    }

    private void searchFromMirror() {
        BkCfg.hideKeyboard(findViewById(R.id.search));
        Intent intent = new Intent(MainActivity.this, WebActivity.class);
        startActivity(intent);
    }

    public void initData(final boolean initList) {
        if (Cfg.getSortBy(getApplicationContext()).equals("0")) {
            // default type
            Cfg.setSortBy(getApplicationContext(), Cfg.SORT_LAST_INSTALL_TIME);
        }

        new AsyncTask<Context, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                DialogDelegate.getInstance().showProgress(MainActivity.this);
            }

            @Override
            protected Boolean doInBackground(Context... contexts) {
                AppListManager.getInstance().initPkgInfoList();

                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                DialogDelegate.getInstance().hideProgress(MainActivity.this);

                if (initList) {
                    initListView();
                } else {
                    AniBtnListView list = (AniBtnListView) getListView();
                    list.hideMenu();
//                    list.resetCheckedList();
                    notifyDataSetChanged();
                }
            }
        }.execute(getApplicationContext());
    }

    private void notifyDataSetChanged() {
        BaseAdapter tmpAdapter = getListAdapter();
        if (tmpAdapter != null) {
            tmpAdapter.notifyDataSetChanged();
        }
    }

    private void initListView() {
        mAdapter = new AppAdapter(MainActivity.this, MainActivity.this);
        setListAdapter(mAdapter);

        AniBtnListView list = (AniBtnListView) getListView();
        list.setSlidingMargin(SLIDING_MARGIN);
        list.setBtnLayoutId(R.id.btnLayout);
        list.setRowId(R.id.row);
        list.setEmptyView(mEmpty);
    }

    public void sharingApp(AppList.PkgInfo info, String target) {
        // https://developer.android.com/training/sharing/send.html#send-binary-content
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("application/octet-stream");
//        intent.setPackage("com.android.bluetooth");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(target)));

        startActivityForResult(Intent.createChooser(intent, "Send To"), SHARING_ACTIVITY);
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
        RunTimePermission.check(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, result -> {
            Intent intent = new Intent(MainActivity.this, DirChooserActivity.class);
            startActivityForResult(intent, DIR_ACTIVITY);
        });
    }

    private void showSystemApp() {
        Cfg.setShowOption(MainActivity.this, "1");
        initData(false);
    }

    private void showInstalledApp() {
        Cfg.setShowOption(MainActivity.this, "0");
        initData(false);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // View.OnClickListener
    //
    ////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onClick(View v) {
        AppAdapter.PosHolder ph = (AppAdapter.PosHolder) v.getTag();

        if (ph.type == AppAdapter.ET_MENU) {
            if (mEdtSearch.getVisibility() != View.GONE) {
                mEdtSearch.setVisibility(View.GONE);
                mSearch.setVisibility(View.GONE);
                mTitle.setVisibility(View.VISIBLE);
                FadeColor.startResource(mTitleBar, R.color.dBgSearch, R.color.dBg, null);
                FadeStatusBar.start(getWindow(), R.color.dBgSearch, R.color.colorPrimaryDark, null);

                BkCfg.hideKeyboard(mEdtSearch);
            }

            ((AniBtnListView) getListView()).toggleMenu(v);
        } else if (ph.type == AppAdapter.ET_DELETE) {
            AppList.PkgInfo info = AppListManager.getInstance().getPkgInfo(ph.position);
            mDeletedPosition = ph.position;

            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + info.pkgName));

            startActivityForResult(intent, DEL_ACTIVITY);
        } else {
            RunTimePermission.check(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, result -> {
                if (result) {
                    mSendEmail = ph.type != 0;
                    final AppList.PkgInfo info = AppListManager.getInstance().getPkgInfo(ph.position);

                    BackupUtil.sendToSd(MainActivity.this, info, mSendEmail, new BkFile.FileCopyDetailListener() {
                        long fileSize;
                        private boolean cancelFlag = false;

                        @Override
                        public void onCancelled() { }

                        @Override
                        public boolean isCancelled() {
                            return cancelFlag;
                        }

                        @Override
                        public void onFinish(String name) {
                            if (info.size > DialogDelegate.SHOW_PROGRESS) {
                                sendMessage(HIDE_PROGRESS_BAR, null);

                                DialogDelegate.getInstance().hideProgress(MainActivity.this);
                            }

                            if (mSendEmail) {
                                sharingApp(info, name);
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
                }
            });
        }
    }

    public void showProgressForCopyFile() {
        mSdProgressBar.setVisibility(View.VISIBLE);
    }

    public ListView getListView() {
        return (ListView) findViewById(android.R.id.list);
    }

    public void setListAdapter(BaseAdapter adapter) {
        getListView().setAdapter(adapter);
    }

    public AppAdapter getListAdapter() {
        return (AppAdapter) getListView().getAdapter();
    }
}
