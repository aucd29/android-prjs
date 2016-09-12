/*
 * Copyright 2016  Burke Choi All rights reserved.
 *              http://www.sarangnamu.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sarangnamu.apk_extractor.control;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Build;
import android.support.annotation.NonNull;

import net.sarangnamu.apk_extractor.MainActivity;
import net.sarangnamu.apk_extractor.R;
import net.sarangnamu.apk_extractor.view.DlgSortBy;
import net.sarangnamu.apk_extractor.view.DlgSpecialThanks;
import net.sarangnamu.common.fonts.FontLoader;
import net.sarangnamu.common.ui.dlg.DlgLicense;

/**
 * Created by <a href="mailto:aucd29@gmail.com">Burke Choi</a> on 2016. 9. 1.. <p/>
 */
public class DialogDelegate {
    private static final org.slf4j.Logger mLog = org.slf4j.LoggerFactory.getLogger(DialogDelegate.class);
    private static DialogDelegate mInst;

    public static final long SHOW_PROGRESS = 2000000;

    private ProgressDialog mDlg;

    public static DialogDelegate getInstance() {
        if (mInst == null) {
            mInst = new DialogDelegate();
        }

        return mInst;
    }

    private DialogDelegate() {

    }

    public void showProgress(@NonNull Activity activity) {
        mDlg = new ProgressDialog(activity);
        mDlg.setCancelable(false);
        mDlg.setMessage(activity.getString(R.string.plsWait));
        mDlg.show();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            mDlg.setContentView(R.layout.dlg_progress);
        }
    }

    public boolean isShowing() {
        if (mDlg == null) {
            return false;
        }

        return mDlg.isShowing();
    }


    public void hideProgress(Activity activity) {
        if (mDlg == null) {
            return ;
        }

        activity.runOnUiThread(mDlg::dismiss);
    }

    public void showLicenseDlg(@NonNull Activity activity) {
        DlgLicense dlg = new DlgLicense(activity);
        dlg.setTitleTypeface(FontLoader.getInstance(activity).getRobotoLight());
        dlg.show();
    }

    public void showSpecialThanks(@NonNull Activity activity) {
        DlgSpecialThanks dlg = new DlgSpecialThanks(activity);
        dlg.setTitleTypeface(FontLoader.getInstance(activity).getRobotoLight());
        dlg.setTitle("Special Thanks");
        dlg.show();
    }

    public void showSortBy(@NonNull MainActivity activity) {
        DlgSortBy dlg = new DlgSortBy(activity);
        dlg.setTitle(R.string.mnu_sortBy);
        dlg.show();
        dlg.setOnDismissListener(dialog -> activity.initData(false));
    }
}
