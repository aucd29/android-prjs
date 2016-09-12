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
package net.sarangnamu.apk_extractor.model;

import net.sarangnamu.common.BkCfg;
import net.sarangnamu.common.BkString;
import android.content.Context;

public class Cfg extends BkCfg {
    public static final String PATH         = "/apks/";

    private static final String EMAIL       = "email";
    private static final String USERPATH    = "usrPath";
    private static final String SHOW_OPT    = "showOpt";
    private static final String SORT_BY     = "sortBy";
    
    public static final String SORT_DEFAULT = "0";
    public static final String SORT_ALPHABET_ASC = "1";
    public static final String SORT_ALPHABET_DESC = "2";
    public static final String SORT_FIRST_INSTALL_TIME = "3";
    public static final String SORT_LAST_INSTALL_TIME = "4";

    public static String getDownPath(Context context) {
        String usrPath = getUserPath(context);

        if (usrPath == null) {
//            return BkCfg.sdPath() + PATH;
            return "/sdcard" + PATH;
        }

        return BkString.setLastSlash(usrPath);
    }

    public static String getEmail(Context context) {
        return get(context, EMAIL, null);
    }

    public static void setEmail(Context context, String email) {
        set(context, EMAIL, email);
    }

    public static void setUserPath(Context context, String path) {
        set(context, USERPATH, path);
    }

    public static String getUserPath(Context context) {
        return get(context, USERPATH, null);
    }

    public static String getShowOption(Context context) {
        return get(context, SHOW_OPT, "0");
    }

    public static void setShowOption(Context context, String val) {
        set(context, SHOW_OPT, val);
    }

    public static void setSortBy(Context context, String type) {
        set(context, SORT_BY, type);
    }

    public static String getSortBy(Context context) {
        return get(context, SORT_BY, "0");
    }
}
