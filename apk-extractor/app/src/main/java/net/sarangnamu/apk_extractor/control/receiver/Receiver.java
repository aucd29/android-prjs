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

package net.sarangnamu.apk_extractor.control.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import net.sarangnamu.apk_extractor.MainActivity;
import net.sarangnamu.common.BkCfg;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by <a href="mailto:aucd29@gmail.com">Burke Choi</a> on 2016. 7. 22.. <p/>
 */
public class Receiver extends BroadcastReceiver {
    private static final org.slf4j.Logger mLog = org.slf4j.LoggerFactory.getLogger(Receiver.class);

    public static final String APPEVENT    = "appEventType";
    public static final String PKGNAME     = "appEventPkgName";
    public static final String APP_ADDED   = "added";
    public static final String APP_REMOVED = "removed";
    public static final String DENY_APK    = "net.sarangnamu.apk_extractor";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            String pkgName = intent.getData().getSchemeSpecificPart();
            if (DENY_APK.equals(pkgName)) {
                return ;
            }

            if (!isForegroundApp(context)) {
                setPreference(context, APP_ADDED, pkgName);
            } else {
                showActivity(context, APP_ADDED, pkgName);
            }
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            String pkgName = intent.getData().getSchemeSpecificPart();
            if (DENY_APK.equals(pkgName)) {
                return ;
            }

            if (!isForegroundApp(context)) {
                setPreference(context, APP_REMOVED, pkgName);
            } else {
                showActivity(context, APP_REMOVED, pkgName);
            }
        }
    }

    private void setPreference(Context context, String type, String pkgName) {
        BkCfg.set(context, APPEVENT, type);
        BkCfg.set(context, PKGNAME, pkgName);
    }

    private void showActivity(Context context, String type, String pkgName) {
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(APPEVENT, type);
        i.putExtra(PKGNAME, pkgName);

        context.startActivity(i);
    }

    private boolean isForegroundApp(Context context) {
        String[] activePackages = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            activePackages = getActivePackages(context);
        } else {
            try {
                activePackages = getActivePackagesCompat(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (activePackages != null) {
            for (String activePackage : activePackages) {
                if (activePackage.equals(context.getPackageName())) {
                    return true;
                }
            }
        }

        return false;
    }

    String[] getActivePackagesCompat(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
        final ComponentName componentName = taskInfo.get(0).topActivity;
        final String[] activePackages = new String[1];
        activePackages[0] = componentName.getPackageName();

        return activePackages;
    }

    String[] getActivePackages(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final Set<String> activePackages = new HashSet<>();
        final List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                activePackages.addAll(Arrays.asList(processInfo.pkgList));
            }
        }
        return activePackages.toArray(new String[activePackages.size()]);
    }
}
