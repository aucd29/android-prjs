/*
 * Api.java
 * Copyright 2013 Burke.Choi All rights reserved.
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
package net.sarangnamu.ems_tracking.api;

import net.sarangnamu.common.network.BkHttp;
import net.sarangnamu.ems_tracking.api.xml.Ems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Api {
    private static final Logger mLog = LoggerFactory.getLogger(Api.class);
    private static final String URL = "http://smart.epost.go.kr/servlet/kpl.vis.common.svl.VisSVL";
    private static final String VERSION = "1.5.1";

    private static BkHttp mHttp;

    private static void initHttp() {
        if (mHttp == null) {
            mHttp = new BkHttp();
        }
    }

    public static Ems tracking(String num) {
        initHttp();
        mHttp.setMethod("POST");

        Map<String, String> params = new HashMap<>();
        params.put("typeApp", "postSearch");
        params.put("typeSmart", "A");
        params.put("ver", VERSION);
        params.put("target_command", "kpl.vis.inh.rel.cmd.RetrieveOrderListMobileXmlCMD");
        params.put("register_No_From", num.toUpperCase());

        Ems ems = null;

        try {
            String res = mHttp.submit(URL, params);
            ems = new Ems(res, num);
        } catch (Exception e) {
            mLog.error(e.getMessage());
        }

        return ems;
    }
}
