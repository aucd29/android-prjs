package net.sarangnamu.apk_extractor;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.SyncStateContract;
import android.text.TextUtils;
import android.util.Log;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    private static final String TAG = "ExampleUnitTest";

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void externalSdCard() {
//        http://lsit81.tistory.com/entry/%EC%B6%94%EA%B0%80-Micro-SD-Card-%EA%B2%BD%EB%A1%9C-%EC%96%BB%EA%B8%B0
//        https://gist.github.com/PauloLuan/4bcecc086095bce28e22
        Log.d(TAG, "===============");
        Log.d(TAG, "EXTERNAL SD CARD");
        Log.d(TAG, "===============");
        String path = getMicroSDCardDirectory();
        Log.d(TAG, "path : " + path);

//        try {
//            for (String key : datas.keySet()) {
//                Log.d(TAG, "DATA : " + key + ", " + datas.get(key).getAbsolutePath());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        Log.d(TAG, "===============");
    }

    private static final long ONE_GIGABYTE = 1024 * 1024 * 1024 * 1024;

    public static String getMicroSDCardDirectory() {
        List<String> mMounts = readMountsFile();
        List<String> mVold = readVoldFile();

        for (int i=0; i < mMounts.size(); i++) {
            String mount = mMounts.get(i);

            if (!mVold.contains(mount)) {
                mMounts.remove(i--);
                continue;
            }

            File root = new File(mount);
            if (!root.exists() || !root.isDirectory()) {
                mMounts.remove(i--);
                continue;
            }

            if (!isAvailableFileSystem(mount)) {
                mMounts.remove(i--);
                continue;
            }

            if (!checkMicroSDCard(mount)) {
                mMounts.remove(i--);
            }
        }

        if (mMounts.size() == 1) {
            return mMounts.get(0);
        }

        return null;
    }

    private static List<String> readMountsFile() {
        /**
         * Scan the /proc/mounts file and look for lines like this:
         * /dev/block/vold/179:1 /mnt/sdcard vfat rw,dirsync,nosuid,nodev,noexec,relatime,uid=1000,gid=1015,fmask=0602,dmask=0602,allow_utime=0020,codepage=cp437,iocharset=iso8859-1,shortname=mixed,utf8,errors=remount-ro 0 0
         *
         * When one is found, split it into its elements
         * and then pull out the path to the that mount point
         * and add it to the arraylist
         */
        List<String> mMounts = new ArrayList<String>();

        try {
            Scanner scanner = new Scanner(new File("/proc/mounts"));

            while (scanner.hasNext()) {
                String line = scanner.nextLine();

                if (line.startsWith("/dev/block/vold/")) {
                    String[] lineElements = line.split("[ \t]+");
                    String element = lineElements[1];

                    mMounts.add(element);
                }
            }
        } catch (Exception e) {
            // Auto-generated catch block
            e.printStackTrace();
        }

        return mMounts;
    }

    private static List<String> readVoldFile() {
        /**
         * Scan the /system/etc/vold.fstab file and look for lines like this:
         * dev_mount sdcard /mnt/sdcard 1 /devices/platform/s3c-sdhci.0/mmc_host/mmc0
         *
         * When one is found, split it into its elements
         * and then pull out the path to the that mount point
         * and add it to the arraylist
         */

        List<String> mVold = new ArrayList<String>();

        try {
            Scanner scanner = new Scanner(new File("/system/etc/vold.fstab"));

            while (scanner.hasNext()) {
                String line = scanner.nextLine();

                if (line.startsWith("dev_mount")) {
                    String[] lineElements = line.split("[ \t]+");
                    String element = lineElements[2];

                    if (element.contains(":")) {
                        element = element.substring(0, element.indexOf(":"));
                    }

                    mVold.add(element);
                }
            }
        } catch (Exception e) {
            // Auto-generated catch block
            e.printStackTrace();
        }

        return mVold;
    }

    private static boolean checkMicroSDCard(String fileSystemName) {
        StatFs statFs = new StatFs(fileSystemName);

        long totalSize = (long)statFs.getBlockSize() * statFs.getBlockCount();

        if (totalSize < ONE_GIGABYTE) {
            return false;
        }

        return true;
    }

    private static boolean isAvailableFileSystem(String fileSystemName) {
        final String[]  unAvailableFileSystemList = {"/dev", "/mnt/asec", "/mnt/obb", "/system", "/data", "/cache", "/efs", "/firmware"};   // 알려진 File System List입니다.

        for (String name : unAvailableFileSystemList) {
            if (fileSystemName.contains(name) == true) {
                return false;
            }
        }

        if (Environment.getExternalStorageDirectory().getAbsolutePath().equals(fileSystemName) == true) {
            /** 안드로이드에서 제공되는 getExternalStorageDirectory() 경로와 같은 경로일 경우에는 추가로 삽입된 SDCard가 아니라고 판단하였습니다. **/
            return false;
        }

        return true;
    }
}