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

import android.text.TextUtils;

import net.sarangnamu.apk_extractor.model.AppList;
import net.sarangnamu.apk_extractor.model.Cfg;
import net.sarangnamu.common.BkApp;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by <a href="mailto:aucd29@gmail.com">Burke Choi</a> on 2016. 9. 1.. <p/>
 */
public class AppListManager {
    private static final org.slf4j.Logger mLog = org.slf4j.LoggerFactory.getLogger(AppListManager.class);
    private static AppListManager mInst;

    private boolean mSearchedList = false;
    private ArrayList<AppList.PkgInfo> mPkgInfoList = new ArrayList<>();
    private ArrayList<AppList.PkgInfo> mPkgInfoSearchedList = new ArrayList<>();

    public static AppListManager getInstance() {
        if (mInst == null) {
            mInst = new AppListManager();
        }

        return mInst;
    }

    private AppListManager() {

    }

    public int getCount() {
        if (mSearchedList) {
            if (mPkgInfoSearchedList == null) {
                return 0;
            }

            return mPkgInfoSearchedList.size();
        }

        return mPkgInfoList.size();
    }

    public boolean isSearchedList() {
        return mSearchedList;
    }

    public void initPkgInfoList() {
        mPkgInfoList = AppList.getInstance().getAllApps(BkApp.context()
                , getShowOption()
                , getSortByOption());
    }

    public AppList.PkgInfo getPkgInfo(int position) {
        if (mSearchedList) {
            return mPkgInfoSearchedList.get(position);
        } else {
            return mPkgInfoList.get(position);
        }
    }

    public ArrayList<AppList.PkgInfo> getPkgInfoList() {
        if (mSearchedList) {
            return mPkgInfoSearchedList;
        }

        return mPkgInfoList;
    }

    public void removeDataListAndRefereshList(int pos) {
        if (mSearchedList) {
            mPkgInfoSearchedList.remove(pos);
        } else {
            mPkgInfoList.remove(pos);
        }
    }

    public void resetSearchedList() {
        mPkgInfoSearchedList.clear();
        mSearchedList = false;
    }

    public void afterTextChanged(String keyword) {
        mPkgInfoSearchedList.clear();

        if (keyword.length() > 0) {
            mSearchedList = true;
            keyword = keyword.toLowerCase();

            for (AppList.PkgInfo info : mPkgInfoList) {
                if (info.appName.toLowerCase().contains(keyword)) {
                    mPkgInfoSearchedList.add(info);
                }
            }
        } else {
            mSearchedList = false;
        }
    }

    public void updatePkgInfoList(AppList.PkgInfo info, String keyword) {
        String sortBy = getSortByOption();
        ArrayList<AppList.PkgInfo> sortList;

        if (mSearchedList) {
            if (!TextUtils.isEmpty(keyword) && info.appName.contains(keyword)) {
                if (mLog.isDebugEnabled()) {
                    mLog.debug("added searched list");
                }

                mPkgInfoSearchedList.add(info);
                sortList = mPkgInfoSearchedList;
            } else {
                if (mLog.isDebugEnabled()) {
                    mLog.debug("added pkg list");
                }

                mPkgInfoList.add(info);
                sortList = mPkgInfoList;
            }
        } else {
            if (mLog.isDebugEnabled()) {
                mLog.debug("added pkg list");
            }

            mPkgInfoList.add(0, info);
            sortList = mPkgInfoList;
        }

        if (sortBy.equals(Cfg.SORT_ALPHABET_ASC)) {
            Collections.sort(sortList, new AppList.SortByAlphabetAsc());
        } else if (sortBy.equals(Cfg.SORT_ALPHABET_DESC)) {
            Collections.sort(sortList, new AppList.SortByAlphabetDesc());
        } else if (sortBy.equals(Cfg.SORT_FIRST_INSTALL_TIME)) {
            Collections.sort(sortList, new AppList.SortByFirstInstallTime());
        } else if (sortBy.equals(Cfg.SORT_LAST_INSTALL_TIME)) {
            Collections.sort(sortList, new AppList.SortByLastInstallTime());
        } else {
            Collections.sort(sortList, new AppList.SortByLastInstallTime());
        }
    }

    public boolean getShowOption() {
        String opt = Cfg.getShowOption(BkApp.context());

        return opt.equals("0");
    }

    private String getSortByOption() {
        String opt = Cfg.getSortBy(BkApp.context());
        return opt;
    }
}
