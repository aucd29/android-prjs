/*
 * Copyright 2013 Burke Choi All rights reserved.
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

package net.sarangnamu.common.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by <a href="mailto:aucd29@gmail.com">Burke Choi</a> on 2016. 6. 15.. <p/>
 */
public class RealTimePermission {
    private static final Logger mLog = LoggerFactory.getLogger(RealTimePermission.class);

    public static final int KEY_REQ_CODE = 0;
    public static final String KEY_PERMISSIONS = "permissions";

    public static synchronized void check(Activity activity, String[] permissions, PermissionListener listener) {
        if (listener == null) {
            mLog.error("ERROR, LISTENER == null");
            return ;
        }

        if (activity == null) {
            mLog.error("ERROR, activity == null");
            listener.result(false);
            return ;
        }

        if (permissions == null) {
            mLog.error("ERROR, permissions == null");

            listener.result(true);
            return ;
        }

        PermissionActivity.setOnPermissionListener(listener);

        if (!checkPermission(activity, permissions)) {
            Intent intent = new Intent(activity, PermissionActivity.class);
            intent.putExtra(KEY_PERMISSIONS, permissions);

            activity.startActivityForResult(intent, KEY_REQ_CODE);
            return ;
        }

        listener.result(true);
    }

    public static boolean checkPermission(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (checkPermission(context, permission)) {
                return true;
            }
        }

        return false;
    }

    public static boolean checkPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }
}
