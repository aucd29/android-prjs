/*
 * Copyright (C) Hanwha S&C Ltd., 2016. All rights reserved.
 *
 * This software is covered by the license agreement between
 * the end user and Hanwha S&C Ltd., and may be
 * used and copied only in accordance with the terms of the
 * said agreement.
 *
 * Hanwha S&C Ltd., assumes no responsibility or
 * liability for any errors or inaccuracies in this software,
 * or any consequential, incidental or indirect damage arising
 * out of the use of the software.
 */

package net.sarangnamu.ems_tracking.ad;

/**
 * Created by <a href="mailto:aucd29@gmail.com">Burke Choi</a> on 2016. 10. 6.. <p/>
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

/**
 * <pre>
 * </pre>
 * @see manual :  https://developers.google.com/admob/android/eclipse
 * @see Where is ad unit id : apps.admob.com -> Monetize -> app list -> Ad Unit ID
 *
 * @author <a href="mailto:aucd29@gmail.com">Burke Choi</a>
 */
public class AdmobDecorator {
    private AdView mAdView;
    private static AdmobDecorator mInst;
    private InterstitialAd mInterstitial;

    public static AdmobDecorator getInstance() {
        if (mInst == null) {
            mInst = new AdmobDecorator();
        }

        return mInst;
    }

    private AdmobDecorator() {

    }

    public void load(Activity act, int id) {
        mAdView = (AdView) act.findViewById(id);
        mAdView.loadAd(new AdRequest.Builder().build());
    }

    public void load(Dialog dlg, int id) {
        mAdView = (AdView) dlg.findViewById(id);
        mAdView.loadAd(new AdRequest.Builder().build());
    }

    public void load(View view, int id) {
        mAdView = (AdView) view.findViewById(id);
        mAdView.loadAd(new AdRequest.Builder().build());
    }

    public void destroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
    }

    public void initInterstitial(Context context, String adId, String testDeviceId) {
        mInterstitial = new InterstitialAd(context);
        mInterstitial.setAdUnitId(adId);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice(testDeviceId).build();

        mInterstitial.loadAd(adRequest);
    }

    public InterstitialAd getInterstitialAd() {
        return mInterstitial;
    }
}
