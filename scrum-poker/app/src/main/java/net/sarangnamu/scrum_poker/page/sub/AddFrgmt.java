/*
 * AddFrgmt.java
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
import net.sarangnamu.common.ui.dlg.DlgTimer;
import net.sarangnamu.common.ui.grid.edit.EditGridData;
import net.sarangnamu.common.ui.grid.edit.EditGridView;
import net.sarangnamu.scrum_poker.R;
import net.sarangnamu.scrum_poker.cfg.Cfg;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import butterknife.BindView;
import butterknife.OnClick;

public class AddFrgmt extends FrgmtBase {
    private static final Logger mLog = LoggerFactory.getLogger(AddFrgmt.class);

    @BindView(R.id.edtTitle) EditText mEdtTitle;
    @BindView(R.id.submit)   ImageButton mSubmit;
    @BindView(R.id.grid)     EditGridView mGrid;

    @Override
    protected int getLayoutId() {
        return R.layout.page_add;
    }

    @Override
    protected void initLayout() {
        mBaseView.setPadding(0, dpToPixelInt(Cfg.ACTION_BAR_HEIGHT), 0, 0);
    }


    @OnClick(R.id.submit) void submit() {
        if (mLog.isDebugEnabled()) {
            StringBuilder log = new StringBuilder();
            log.append("===================================================================\n");
            log.append("Add Rule\n");
            log.append("===================================================================\n");
            mLog.debug(log.toString());
        }

        if (getString(R.string.license).equals(mEdtTitle.getText()) ||
                getString(R.string.add_rule).equals(mEdtTitle.getText())) {
            String msg = String.format(getActivity().getString(R.string.doNotUseThisWord), mEdtTitle.getText());
            showDlgTimer(msg);
            return;
        }

        ArrayList<EditGridData> datas = mGrid.getGridData();
        if (datas == null) {
            showDlgTimer(R.string.invalidData);
            mLog.error("onClick <ArrayList<EditGridData> is null>");
            return;
        }

        ArrayList<String> contents = new ArrayList<String>();
        for (EditGridData data : datas) {
            contents.add(data.value);
        }

//        UserScrumData scrumData = new UserScrumData();
//        scrumData.setTitle(mEdtTitle.getText().toString());
//        scrumData.setContents(contents);
//
//        if (!DbHelper.insert(scrumData)) {
//            Toast.makeText(getActivity(), R.string.errInsert, Toast.LENGTH_SHORT).show();
//            return;
//        }

        mEdtTitle.setText("");
        mGrid.reset();

        showDlgTimer(R.string.insertComplete);
    }

    private void showDlgTimer(int resid) {
        showDlgTimer(getActivity().getString(resid));
    }

    private void showDlgTimer(String msg) {
        DlgTimer dlg = new DlgTimer(getActivity(), R.layout.dlg_timer);
        dlg.setMessage(msg);
        dlg.setTime(1500);
        dlg.show();
        dlg.setTransparentBaseLayout();
    }
}
