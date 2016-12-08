/*
 * Detail.java
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

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.sarangnamu.ems_tracking.ad.AdmobDecorator;
import net.sarangnamu.ems_tracking.api.xml.Ems;
import net.sarangnamu.ems_tracking.api.xml.Ems.EmsTrackingData;
import net.sarangnamu.ems_tracking.cfg.Cfg;
import net.sarangnamu.ems_tracking.db.EmsDbHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Detail extends AppCompatActivity {
    private static final Logger mLog = LoggerFactory.getLogger(Detail.class);

    @BindView(R.id.emsNum)
    TextView mEmsNum;
    @BindView(R.id.detail)
    TextView mDetail;
    @BindView(R.id.shipped_to_apply)
    TextView mShippedToApply;
    @BindView(R.id.list)
    ListView mList;

    private Ems mEms;
    private int mEmsPrimaryKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String emsNumber = getIntent().getStringExtra(EmsDataManager.EMS_NUM);
        mEmsPrimaryKey   = getIntent().getIntExtra(EmsDataManager.EMS_PRIMARY_KEY, -1);

        mEms = EmsDataManager.getInstance().getEmsData(emsNumber);
        if (mEms == null) {
            finish();
        }

        setContentView(R.layout.detail);
        ButterKnife.bind(this);

        initLabel();
        initAdView();
        initListView();
    }

    @Override
    protected void onDestroy() {
        AdmobDecorator.getInstance().destroy();

        super.onDestroy();
    }

    private void initLabel() {
        if (mEms == null) {
            return ;
        }

        String anotherName = Cfg.getAnotherName(getApplicationContext(), mEms.mEmsNum);
        if (anotherName == null) {
            mEmsNum.setText(mEms.mEmsNum);
        } else {
            mEmsNum.setText(anotherName);
            mDetail.setText(mEms.mEmsNum);
        }

        mShippedToApply.setOnClickListener(v -> {
            new AlertDialog.Builder(Detail.this)
                    .setMessage(R.string.shipped_to_apply)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        if (mEmsPrimaryKey == -1) {
                            return ;
                        }

                        EmsTrackingData lastData = mEms.getLastTrackingData();
                        if (lastData == null) {
                            return ;
                        }

                        lastData.status = Cfg.DONE;
                        EmsDataManager.getInstance().setEmsData(mEms.mEmsNum, mEms);
                        EmsDbHelper.update(mEmsPrimaryKey, mEms, null);

                        ((BaseAdapter) mList.getAdapter()).notifyDataSetChanged();
                    }).setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void initAdView() {
        AdmobDecorator.getInstance().load(this, R.id.adView);
    }

    private void initListView() {
        EmsHistory mAdapter = new EmsHistory();
        mList.setAdapter(mAdapter);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // EmsHistory
    //
    ////////////////////////////////////////////////////////////////////////////////////

    class ViewHolder {
        TextView office;
        TextView status;
        TextView date;
    }

    class EmsHistory extends BaseAdapter {
        @Override
        public int getCount() {
            if (mEms == null) {
                return 0;
            }

            return mEms.mTrackingList.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int pos, View view, ViewGroup vg) {
            ViewHolder vh;

            if (view == null) {
                vh = new ViewHolder();
                view = LayoutInflater.from(Detail.this).inflate(R.layout.detail_item, null);

                vh.office = (TextView) view.findViewById(R.id.office);
                vh.status = (TextView) view.findViewById(R.id.status);
                vh.date   = (TextView) view.findViewById(R.id.date);

                view.setTag(vh);
            } else {
                vh = (ViewHolder) view.getTag();
            }

            EmsTrackingData data = mEms.getEmsDataFromXml(pos);

            vh.office.setText(data.office);
            vh.status.setText(data.status);
            vh.date.setText(data.date);

            return view;
        }
    }
}
