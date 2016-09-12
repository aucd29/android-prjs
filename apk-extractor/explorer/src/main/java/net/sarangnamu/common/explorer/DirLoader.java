/*
 * DirLoader.java
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
package net.sarangnamu.common.explorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.ipaulpro.afilechooser.FileChooserActivity;
import com.ipaulpro.afilechooser.FileLoader;
import com.ipaulpro.afilechooser.utils.FileUtils;

public class DirLoader extends FileLoader {
    private static final String TAG = "DirLoader";

    public DirLoader(Context context, String path) {
        super(context, path);
    }

    @Override
    public List<File> loadInBackground() {
        Log.d(TAG, "PATH(0) : " + mPath);

        List<File> pathes = FileUtils.getDirList(mPath);
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (root.equals(mPath)) {
            String extSdPath = getExternalSdStorage();

            if (!TextUtils.isEmpty(extSdPath)) {
                File fpExtSd = new File(extSdPath);
                if (fpExtSd.exists()) {
                    pathes.add(fpExtSd);
                }
            }
        }

        return pathes;
    }

    public static String getExternalSdStorage() {
        File fp = Environment.getExternalStorageDirectory();
        String exts = null;

        if (fp != null) {
            exts = fp.getPath();
        }

        try {
            File mountFp = new File("/proc/mounts");
            if (!mountFp.exists()) {
                return null;
            }

            String sdCard = null;
            Scanner scanner = new Scanner(mountFp);

            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.contains("secure") || line.contains("asec")) {
                    continue;
                }

                if (line.contains("fat")) {
                    String[] pars = line.split("\\s");
                    if (pars.length < 2) {
                        continue;
                    }

                    if (pars[1].equals(exts) || "/mnt/sdcard".equals(pars[1])) {
                        continue;
                    }

                    sdCard = pars[1];
                    break;
                }
            }

            if (!TextUtils.isEmpty(sdCard) && sdCard.contains("media_rw")) {
                int pos = sdCard.lastIndexOf('/');
                String name = sdCard.substring(pos, sdCard.length());
                sdCard = "/storage" + name;
            }

            return sdCard;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
