/*
 * Ems.java
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
package net.sarangnamu.ems_tracking.api.xml;

import net.sarangnamu.common.XPathParser;
import net.sarangnamu.ems_tracking.cfg.Cfg;
import net.sarangnamu.ems_tracking.db.EmsDbHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import javax.xml.xpath.XPathConstants;

public class Ems extends XPathParser {
    private static final Logger mLog = LoggerFactory.getLogger(Ems.class);

    public String mEmsNum;
    public String mErrMsg;

    public final String mTmpNum;
    public ArrayList<EmsTrackingData> mTrackingList = new ArrayList<>();

    public Ems(String ems, String emsNum) {
        super();

        mTmpNum = emsNum.toUpperCase();
        loadXmlString(ems);
    }

    @Override
    protected void parsing() throws Exception {
        if (mTrackingList == null) {
            mTrackingList = new ArrayList<>();
        }

        /*
         *  <?xml version='1.0' encoding="utf-8"?>
            <xsync>
            <xsyncData>
                <rgist><![CDATA[RB832426012CN]]></rgist>
            </xsyncData>
            <xsyncData>
                <processDe><![CDATA[2013-10-06 16:05]]></processDe>
                    <processSttus><![CDATA[접수]]></processSttus>
                    <nowLc><![CDATA[528032]]></nowLc>
                    <detailDc><![CDATA[]]></detailDc>
                </xsyncData>
            <xsyncData>
                <processDe><![CDATA[2013-10-09 11:02]]></processDe>
                    <processSttus><![CDATA[발송준비]]></processSttus>
                    <nowLc><![CDATA[CNCANA]]></nowLc>
                    <detailDc><![CDATA[]]></detailDc>
                </xsyncData>
            <xsyncData>
                <processDe><![CDATA[2013-10-11 15:48]]></processDe>
                <processSttus><![CDATA[교환국 도착]]></processSttus>
                <nowLc><![CDATA[국제우편물류센터]]></nowLc>
                <detailDc><![CDATA[]]></detailDc>
            </xsyncData>
            <xsyncData>
                <processDe><![CDATA[2013-10-11 22:10]]></processDe>
                <processSttus><![CDATA[발송]]></processSttus>
                <nowLc><![CDATA[국제우편물류센터]]></nowLc>
                <detailDc><![CDATA[]]></detailDc>
            </xsyncData>
            <xsyncData>
                <processDe><![CDATA[2013-10-11 23:41]]></processDe>
                <processSttus><![CDATA[도착]]></processSttus>
                <nowLc><![CDATA[동서울우편집중국]]></nowLc>
                <detailDc><![CDATA[]]></detailDc>
            </xsyncData>
            <xsyncData>
                <processDe><![CDATA[2013-10-12 03:31]]></processDe>
                <processSttus><![CDATA[발송]]></processSttus>
                <nowLc><![CDATA[동서울우편집중국]]></nowLc>
                <detailDc><![CDATA[]]></detailDc>
            </xsyncData>
            <xsyncData>
                <processDe><![CDATA[2013-10-12 04:27]]></processDe>
                <processSttus><![CDATA[도착]]></processSttus>
                <nowLc><![CDATA[서울강남]]></nowLc>
                <detailDc><![CDATA[]]></detailDc>
            </xsyncData>
            <xsyncData>
                <processDe><![CDATA[2013-10-14 09:28]]></processDe>
                <processSttus><![CDATA[배달준비]]></processSttus>
                <nowLc><![CDATA[서울강남]]></nowLc>
                <detailDc><![CDATA[]]></detailDc>
            </xsyncData>
            <xsyncData>
                <processDe><![CDATA[2013-10-15 16:05]]></processDe>
                <processSttus><![CDATA[배달완료]]></processSttus>
                <nowLc><![CDATA[서울강남]]></nowLc>
                <detailDc><![CDATA[]]></detailDc>
            </xsyncData>
            </xsync>

            // error
            <?xml version='1.0' encoding="utf-8"?>
            <xsync>
            <xsyncData>
            <error_code><![CDATA[ERR-001]]></error_code>
            <message><![CDATA[조회결과가 없습니다.]]></message>
            </xsyncData>
            </xsync>
         */


        String expr;
        int count;

        expr = "count(//xsyncData)";
        count = Integer.parseInt(mXpath.evaluate(expr, mDocument, XPathConstants.STRING).toString());

        expr = "//rgist/text()";
        mEmsNum = mXpath.evaluate(expr, mDocument, XPathConstants.STRING).toString();

        if (mEmsNum == null || mEmsNum.length() == 0) {
            expr = "//message/text()";
            mErrMsg = mXpath.evaluate(expr, mDocument, XPathConstants.STRING).toString();
            mErrMsg += " - [";

            expr = "//error_code/text()";
            mErrMsg += mXpath.evaluate(expr, mDocument, XPathConstants.STRING).toString();
            mErrMsg += "]";

            mEmsNum = mTmpNum;
            mTrackingList.add(new EmsTrackingData());
        } else {
            for (int i=2; i<=count; ++i) {
                mTrackingList.add(new EmsTrackingData(i));
            }
        }
    }

    public int getDataCount() {
        if (mTrackingList == null) {
            return 0;
        }

        return mTrackingList.size();
    }

    public EmsTrackingData getEmsDataFromXml(int pos) {
        if (mTrackingList == null) {
            return null;
        }

        return mTrackingList.get(pos);
    }

    public EmsTrackingData getLastTrackingData() {
        if (mTrackingList == null) {
            return null;
        }

        int pos = mTrackingList.size();
        if (pos == 0) {
            return null;
        }

        return mTrackingList.get(pos - 1);
    }

    public void trace() {
        String log = "===================================================================\n" +
                "EMS DATA INFO\n" +
                "===================================================================\n" +
                "ems number : " + mEmsNum;

        for (EmsTrackingData data : mTrackingList) {
            data.trace();
        }

        mLog.debug(log);
    }

    public class EmsTrackingData {
        public String date;
        public String status;
        public String office;
        public String detail;

        public EmsTrackingData() {
            if (EmsDbHelper.isDone(mEmsNum)) {
                status = Cfg.DONE;
            } else {
                status = Cfg.UNREGIST; //"미등록";
            }

            date   = "";
            office = "-";
            detail = "-";
        }

        public EmsTrackingData(int pos) throws Exception {
            String expr, prefix = "//xsyncData[" + pos + "]";

            expr = prefix + "/processDe/text()";
            date = mXpath.evaluate(expr, mDocument, XPathConstants.STRING).toString();

            expr = prefix + "/processSttus/text()";
            status = mXpath.evaluate(expr, mDocument, XPathConstants.STRING).toString();

            expr = prefix + "/nowLc/text()";
            office = mXpath.evaluate(expr, mDocument, XPathConstants.STRING).toString();

            expr = prefix + "/detailDc/text()";
            detail = mXpath.evaluate(expr, mDocument, XPathConstants.STRING).toString();
        }

        public void trace() {
            String log = "date   : " + date + "\n" +
                    "status : " + status + "\n" +
                    "office : " + office + "\n" +
                    "detail : " + detail + "\n";
            mLog.debug(log);
        }
    }
}
