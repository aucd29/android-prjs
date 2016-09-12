/*
 * MainFrgmt.java
 * Copyright 2014 Burke Choi All right reserverd.
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
package net.sarangnamu.scrum_poker.page.sub;

import java.util.ArrayList;

import net.sarangnamu.common.FrgmtBase;
import net.sarangnamu.scrum_poker.MainActivity;
import net.sarangnamu.scrum_poker.R;
import net.sarangnamu.scrum_poker.cfg.Cfg;
import net.sarangnamu.scrum_poker.page.PageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

public class MainFrgmt extends FrgmtBase {
    private static final Logger mLog = LoggerFactory.getLogger(MainFrgmt.class);

    @BindView(R.id.grid) GridView mGrid;
    private ArrayList<String> mDefaultValue;

    @Override
    protected int getLayoutId() {
        return R.layout.page_main;
    }

    @Override
    protected void initLayout() {
        int measureWidth = mGrid.getMeasuredWidth();

        initDefaultValue();
        initAdapter();
    }

    private void initDefaultValue() {
        if (mDefaultValue == null) {
            mDefaultValue = new ArrayList<String>();
        }

        if (mDefaultValue.size() > 0) {
            return;
        }

        mDefaultValue.add("0");
        mDefaultValue.add("1/2");
        mDefaultValue.add("2");

        mDefaultValue.add("3");
        mDefaultValue.add("5");
        mDefaultValue.add("8");

        mDefaultValue.add("13");
        mDefaultValue.add("20");
        mDefaultValue.add("30");

        mDefaultValue.add("60");
        mDefaultValue.add("?");
        mDefaultValue.add("∞");
    }

    private void initAdapter() {
        mGrid.setAdapter(new ScrumAdapter());
        mGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bd = new Bundle();
                bd.putString(Cfg.SCRUM_DATA, mDefaultValue.get(position));

                ((MainActivity) getActivity()).setAddButtonAlpha(0);
                PageManager.getInstance(getActivity()).replace(R.id.content_frame, CardFrgmt.class, bd);
            }
        });
    }

    public void reloadPage() {
        int primaryKey = Integer.parseInt(Cfg.get(getActivity(), Cfg.DB_ID, ""));
        //DBHelper.select(
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // SCRUM ADAPTER
    //
    ////////////////////////////////////////////////////////////////////////////////////

    static class ViewHolder {
        @BindView(R.id.number) TextView number;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    class ScrumAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (mDefaultValue == null) {
                return 0;
            }

            return mDefaultValue.size();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.page_main_scrum_item, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.number.setText(mDefaultValue.get(position));

            return convertView;
        }
    }
}

