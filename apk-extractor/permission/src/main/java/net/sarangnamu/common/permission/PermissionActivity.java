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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by <a href="mailto:aucd29@gmail.com">Burke Choi</a> on 2016. 6. 15.. <p/>
 */
public class PermissionActivity extends Activity {
    private static final Logger mLog = LoggerFactory.getLogger(PermissionActivity.class);

    private static final int REQUEST_CODE = 0;
    private static final int CALLBACK_PERMISSION = 1;

    private String[] mPermissions;
    private static PermissionListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        mPermissions = getIntent().getStringArrayExtra(RealTimePermission.KEY_PERMISSIONS);
        if (mPermissions == null) {
            mLog.error("ERROR, PERMISSION == null");
            finish();
            return ;
        }

        requestPermissions();
    }

    public static void setOnPermissionListener(PermissionListener listener) {
        mListener = listener;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, int[] grantResults) {
        if (mListener == null) {
            mLog.error("ERROR, LISTENER == null");
            finish();
            return ;
        }

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mListener.result(true);
            mListener = null;
            finish();
        } else {
            showPermissionDialog();
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            ActivityCompat.requestPermissions(PermissionActivity.this, mPermissions, REQUEST_CODE);
        }
    }

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permission_title);
        builder.setMessage(R.string.permission_message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            requestPermissions();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CALLBACK_PERMISSION:
                boolean res = RealTimePermission.checkPermission(PermissionActivity.this, mPermissions);

                if (res) {
                    if (mListener == null) {
                        mLog.error("ERROR, LISTENER == null");
                        finish();

                        return ;
                    }

                    mListener.result(res);
                    mListener = null;

                    finish();
                } else {
                    showPermissionDialog();
                }
                break;
        }
    }
}
