/*
 * EmsDataManager.java
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
package net.sarangnamu.ems_tracking;

import android.os.AsyncTask;

import net.sarangnamu.ems_tracking.api.Api;
import net.sarangnamu.ems_tracking.api.xml.Ems;
import net.sarangnamu.ems_tracking.db.EmsDbHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

// 사용자에게 입력받은 ems number 를 db 에 넣고 서버에 상태 값을 전달 받은 뒤 그 결과를 db 내용에 갱신
public class EmsDataManager {
    private static final Logger mLog = LoggerFactory.getLogger(EmsDataManager.class);

    public static final String EMS_NUM = "emsNum";
    public static final String EMS_PRIMARY_KEY = "id";

    private static EmsDataManager sInst;
    private HashMap<String, Ems> mEmsMap;

    public static EmsDataManager getInstance() {
        if (sInst == null) {
            sInst = new EmsDataManager();
        }

        return sInst;
    }

    private EmsDataManager() {

    }

    public interface EmsDataListener {
        void onEmsData(Ems ems);
    }

    public void getAsyncEmsData(final MainActivity act, final String num, final EmsDataListener l) {
        if (mEmsMap == null) {
            mEmsMap = new HashMap<>();
        }

        Ems ems = mEmsMap.get(num);
        if (ems == null) {
            new AsyncTask<Void, Void, Boolean>() {
                Ems ems;

                @Override
                protected void onPreExecute() {
                    act.showProgress();
                }

                @Override
                protected Boolean doInBackground(Void... contexts) {
                    try {
                        ems = Api.tracking(num);
                        setEmsData(num, ems);

                        EmsDbHelper.update(0, ems, null);
                    } catch (Exception e) {
                        mLog.error(e.getMessage());

                        return false;
                    }

                    return true;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    act.hideProgress();

                    if (l != null) {
                        l.onEmsData(ems);
                    }
                }
            }.execute();
        }

        if (l != null) {
            l.onEmsData(ems);
        }
    }

    public Ems getEmsData(String emsNumber) {
        try {
            return mEmsMap.get(emsNumber);
        } catch (Exception e) {
            mLog.error(e.getMessage());
        }

        return null;
    }

    public void setEmsData(String num, Ems ems) {
        if (mEmsMap == null) {
            mEmsMap = new HashMap<>();
        }

        mEmsMap.put(num, ems);
    }
}
