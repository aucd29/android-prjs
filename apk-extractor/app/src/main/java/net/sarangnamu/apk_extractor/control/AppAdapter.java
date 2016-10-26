/*
 * Copyright 2016 Burke Choi All rights reserved.
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

package net.sarangnamu.apk_extractor.control;

import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.sarangnamu.apk_extractor.model.AppList;
import net.sarangnamu.apk_extractor.R;
import net.sarangnamu.common.BkApp;
import net.sarangnamu.common.DimTool;

/**
 * Created by <a href="mailto:aucd29@gmail.com">Burke Choi</a> on 2016. 9. 1.. <p/>
 */
public class AppAdapter extends BaseAdapter {
    private static final org.slf4j.Logger mLog = org.slf4j.LoggerFactory.getLogger(AppAdapter.class);

    private static final int SLIDING_MARGIN = 160;

    public static final int ET_SDCARD   = 0;
    public static final int ET_EMAIL    = 1;
    public static final int ET_MENU     = 2;
    public static final int ET_DELETE   = 3;


    public int margin;

    private Activity mActivity;
    private View.OnClickListener mClickListener;

    public AppAdapter(Activity activity, View.OnClickListener clickListener) {
        mActivity       = activity;
        mClickListener  = clickListener;

        margin = DimTool.dpToPixelInt(BkApp.context(), SLIDING_MARGIN) * -1;
    }

    @Override
    public int getCount() {
        int count = AppListManager.getInstance().getCount();

        if (mLog.isDebugEnabled()) {
            mLog.debug("count : " + count);
        }

        return count;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.item, null);

            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.size = (TextView) convertView.findViewById(R.id.size);
            holder.pkgName = (TextView) convertView.findViewById(R.id.pkgName);
            holder.version = (TextView) convertView.findViewById(R.id.version);
            holder.sd = (TextView) convertView.findViewById(R.id.sd);
            holder.email = (TextView) convertView.findViewById(R.id.email);
            holder.delete = (TextView) convertView.findViewById(R.id.delete);
            holder.row = (RelativeLayout) convertView.findViewById(R.id.row);
            holder.btnLayout = (LinearLayout) convertView.findViewById(R.id.btnLayout);

            holder.sd.setOnClickListener(mClickListener);
            holder.email.setOnClickListener(mClickListener);
            holder.delete.setOnClickListener(mClickListener);
            holder.row.setOnClickListener(mClickListener);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AppList.PkgInfo info = AppListManager.getInstance().getPkgInfo(position);
        if (info.icon != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                holder.icon.setBackground(info.icon);
            } else {
                holder.icon.setBackgroundDrawable(info.icon);
            }
        }

        holder.name.setText(info.appName);
        holder.size.setText(info.appSize);
        holder.pkgName.setText(info.pkgName);
        holder.version.setText("(" + info.versionName + ")");

        holder.sd.setTag(new PosHolder(position, ET_SDCARD, holder.row));
        holder.email.setTag(new PosHolder(position, ET_EMAIL, holder.row));
        holder.delete.setTag(new PosHolder(position, ET_DELETE, holder.row));
        holder.row.setTag(new PosHolder(position, ET_MENU, holder.row));

        return convertView;
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // ViewHolder
    //
    ////////////////////////////////////////////////////////////////////////////////////

    public static class ViewHolder {
        ImageView icon;
        TextView name, size, pkgName, version, sd, email, delete;
        LinearLayout btnLayout;
        RelativeLayout row;
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // PosHolder
    //
    ////////////////////////////////////////////////////////////////////////////////////

    public static class PosHolder {
        public int position;
        public int type;
        public View row;

        public PosHolder(int pos, int type, View row) {
            this.position = pos;
            this.type = type;
            this.row = row;
        }
    }

}
