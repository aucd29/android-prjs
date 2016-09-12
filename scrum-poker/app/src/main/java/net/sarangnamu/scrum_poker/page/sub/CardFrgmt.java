/*
 * CardFrgmt.java
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

import net.sarangnamu.common.FrgmtBase;
import net.sarangnamu.scrum_poker.R;
import net.sarangnamu.scrum_poker.cfg.Cfg;

import android.graphics.drawable.Drawable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import butterknife.BindView;

public class CardFrgmt extends FrgmtBase {
    private static final Logger mLog = LoggerFactory.getLogger(CardFrgmt.class);

    @BindView(R.id.cardbase) RelativeLayout mLayout;
    @BindView(R.id.value) TextView mValue;
//    @BindView(R.id.blurView) BlurView mBlurView;

    @Override
    protected int getLayoutId() {
        return R.layout.page_card;
    }

    @Override
    protected void initLayout() {
        mValue.setText(getArguments().getString(Cfg.SCRUM_DATA, "ERROR"));
        setupBlurView();
    }

    private void setupBlurView() {
        final float radius = 16f;

//        final View decorView = getActivity().getWindow().getDecorView();
        //Activity's root View. Can also be root View of your layout
//        final View rootView = decorView.findViewById(android.R.id.content);
        //set background, if your root layout doesn't have one
        final Drawable windowBackground = mLayout.getBackground();

//        mBlurView.setupWith(mValue)
//                 .windowBackground(windowBackground)
//                 .blurAlgorithm(new RenderScriptBlur(getActivity(), true)) //Preferable algorithm, needs RenderScript support mode enabled
//                 .blurRadius(radius);
    }

    @Override
    public void onDestroy() {
        if (mLog.isDebugEnabled()) {
            StringBuilder log = new StringBuilder();
            log.append("===================================================================\n");
            log.append("DESTROY\n");
            log.append("===================================================================\n");
            mLog.error(log.toString());
        }

//        mBlurView.removeAllViews();

        super.onDestroy();
    }
}
