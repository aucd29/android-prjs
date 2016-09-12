/*
 * Copyright (C) Hanwha S&C Ltd., 2016. All rights reserved.
 *
 * This software is covered by the license agreement between
 * the end user and Hanwha S&C Ltd., and may be
 * used and copied only in accordance with the terms of the
 * said agreement.
 *
 * Hanwha S&C Ltd., assumes no responsibility or
 * liability for any errors or inaccuracies in this software,
 * or any consequential, incidental or indirect damage arising
 * out of the use of the software.
 */

package net.sarangnamu.apk_extractor.control;

import android.support.design.widget.Snackbar;

import net.sarangnamu.apk_extractor.model.AppList;
import net.sarangnamu.apk_extractor.MainActivity;
import net.sarangnamu.apk_extractor.R;
import net.sarangnamu.apk_extractor.model.Cfg;
import net.sarangnamu.common.BkFile;

import java.io.File;

/**
 * Created by <a href="mailto:aucd29@gmail.com">Burke Choi</a> on 2016. 9. 1.. <p/>
 */
public class BackupUtil {
    private static final org.slf4j.Logger mLog = org.slf4j.LoggerFactory.getLogger(BackupUtil.class);

    public static void sendToSd(final MainActivity activity, final AppList.PkgInfo info, final boolean sendEmail, BkFile.FileCopyDetailListener listener) {
//        final AppList.PkgInfo info = AppListManager.getInstance().getPkgInfo(position);
        if (info.size > DialogDelegate.SHOW_PROGRESS) {
            DialogDelegate.getInstance().showProgress(activity);
            activity.showProgressForCopyFile();
        }

        new Thread(() -> {
            try {
                File src = new File(info.srcDir);

                String fileName = "";
                String pattern = "^[A-Za-z0-9. ]+$";
                if (info.appName.matches(pattern)) {
                    fileName = info.appName;
                } else {
                    fileName = info.pkgName;
                }

                fileName += "-";
                fileName += info.versionName;
                fileName += ".apk";

                String apkFullPath = Cfg.getDownPath(activity) + fileName;

                if (new File(apkFullPath).exists()) {
                    if (DialogDelegate.getInstance().isShowing()) {
                        DialogDelegate.getInstance().hideProgress(activity);
                    }

                    if (sendEmail) {
                        activity.sharingApp(info, Cfg.getDownPath(activity) + fileName);
                    } else {
                        String msg = String.format(activity.getString(R.string.existApp), apkFullPath);
                        Snackbar.make(activity.getListView(), msg, Snackbar.LENGTH_SHORT).show();
                    }

                    return ;
                }

                BkFile.copyFileTo(src, apkFullPath, listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
