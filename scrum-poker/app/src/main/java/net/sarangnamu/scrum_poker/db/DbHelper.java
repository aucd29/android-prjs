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

package net.sarangnamu.scrum_poker.db;

import net.sarangnamu.common.realm.RealmHelper;

/**
 * Created by <a href="mailto:aucd29@gmail.com">Burke Choi</a> on 2016. 6. 3.. <p/>
 */
public class DbHelper extends RealmHelper {
    private static final org.slf4j.Logger mLog = org.slf4j.LoggerFactory.getLogger(DbHelper.class);

    private static DbHelper mInst;

    public static DbHelper getInstance() {
        if (mInst == null) {
            mInst = new DbHelper();
        }

        return mInst;
    }

    private DbHelper() {

    }
}
