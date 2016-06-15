/*
 * DlgOrderBy.java
 * Copyright 2015 Burke Choi All right reserverd.
 *             http://www.sarangnamu.net
 */
package net.sarangnamu.apk_extractor.dlg;

import net.sarangnamu.apk_extractor.R;
import net.sarangnamu.apk_extractor.cfg.Cfg;
import net.sarangnamu.common.admob.AdMobDecorator;
import net.sarangnamu.common.fonts.FontLoader;
import net.sarangnamu.common.ui.dlg.DlgBase;
import android.content.Context;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DlgSortBy extends DlgBase {
    @Bind(R.id.sortGroup) RadioGroup mGroup;
    @Bind(R.id.alphabetAsc) RadioButton mAlphabetAsc;
    @Bind(R.id.alphabetDesc) RadioButton mAlphabetDesc;
    @Bind(R.id.firstInstallTime) RadioButton mFirstInstallTime;
    @Bind(R.id.lastInstallTime) RadioButton mLastInstallTime;

    public DlgSortBy(Context context) {
        super(context);
    }

    @Override
    protected int getBaseLayoutId() {
        return R.layout.dlg_sortby;
    }

    @Override
    protected void initLayout() {
        ButterKnife.bind(this);

        AdMobDecorator.getInstance().load(this, R.id.adView);
        String sortBy = Cfg.getSortBy(getContext());

        if (sortBy.equals(Cfg.SORT_ALPHABET_ASC)) {
            mAlphabetAsc.setChecked(true);
        } else if (sortBy.equals(Cfg.SORT_ALPHABET_DESC)) {
            mAlphabetDesc.setChecked(true);
        } else if (sortBy.equals(Cfg.SORT_FIRST_INSTALL_TIME)) {
            mFirstInstallTime.setChecked(true);
        } else if (sortBy.equals(Cfg.SORT_LAST_INSTALL_TIME)) {
            mLastInstallTime.setChecked(true);
        }

        mGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
            case R.id.alphabetAsc:
                Cfg.setSortBy(getContext(), Cfg.SORT_ALPHABET_ASC);
                break;
            case R.id.alphabetDesc:
                Cfg.setSortBy(getContext(), Cfg.SORT_ALPHABET_DESC);
                break;
            case R.id.firstInstallTime:
                Cfg.setSortBy(getContext(), Cfg.SORT_FIRST_INSTALL_TIME);
                break;
            case R.id.lastInstallTime:
                Cfg.setSortBy(getContext(), Cfg.SORT_LAST_INSTALL_TIME);
                break;
            }

            dismiss();
        });

        FontLoader.getInstance(getContext()).applyChild("Roboto-Light", mGroup);
    }
}
