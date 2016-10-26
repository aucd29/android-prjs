/*
 * Copyright 2016 Burke Choi All rights reserved.
 *             http://www.sarangnamu.net
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
