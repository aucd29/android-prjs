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

package net.sarangnamu.scroll_capture;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import net.sarangnamu.common.BkCfg;
import net.sarangnamu.common.permission.PermissionListener;
import net.sarangnamu.common.permission.RunTimePermission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final Logger mLog = LoggerFactory.getLogger(MainActivity.class);
    
    @BindView(R.id.web)
    WebView mWeb;

    @BindView(R.id.url)
    EditText mUrl;

    @BindView(R.id.options)
    ImageView mOptions;

//    @BindView(R.id.fab)
//    FloatingActionButton mFab;

    @BindView(R.id.progress)
    ProgressBar mProgress;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw();
        }

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mWeb.loadUrl("http://aucd29.tistory.com/m");
        BkCfg.hideKeyboard(mWeb);

        mWeb.getSettings().setJavaScriptEnabled(true);
        mWeb.getSettings().setDomStorageEnabled(true);
        mWeb.getSettings().setLoadWithOverviewMode(true);
        mWeb.getSettings().setUseWideViewPort(true);
        mWeb.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (mLog.isDebugEnabled()) {
                    String log = "";
                    log += "===================================================================\n";
                    log += "WEBPAGE LOADED\n";
                    log += "===================================================================\n";
                    mLog.debug(log);
                }

                if (url.equals("http://aucd29.tistory.com/")) {
                    mProgress.setVisibility(View.GONE);
//                    mFab.animate().alpha(1);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                if (url.equals("http://aucd29.tistory.com/")) {
                    mProgress.setVisibility(View.VISIBLE);
//                    mFab.animate().alpha(0);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (mLog.isErrorEnabled()) {
                    String log = "";
                    log += "===================================================================\n";
                    log += "ERROR: \n";
                    log += "===================================================================\n";
                    mLog.error(log);
                }
                super.onReceivedError(view, request, error);
            }
        });

    }

//    @OnClick(R.id.fab)
    void fabClick() {
        RunTimePermission.check(this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, result -> {
            if (!result) {
                return ;
            }

            scrollCapture();
        });
    }

    private void scrollCapture() {
        mProgress.setVisibility(View.VISIBLE);
        BkCfg.hideKeyboard(mUrl);

        if (mLog.isDebugEnabled()) {
            String log = "";
            log += "===================================================================\n";
            log += "CAPTURE START\n";
            log += "===================================================================\n";
            mLog.debug(log);
        }

        new AsyncTask<Object, Integer, Boolean>() {
            Bitmap mBmp;

            @Override
            protected void onPreExecute() {
                int w, h;
                try {
                    Picture pic = mWeb.capturePicture();

                    w = pic.getWidth();
                    h = pic.getHeight();
                } catch (Exception e) {
                    w = mWeb.getMeasuredWidth();
                    h = mWeb.getMeasuredHeight();
                }

                mWeb.measure(View.MeasureSpec.makeMeasureSpec(
                        View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                mBmp = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);

                Canvas canvas = new Canvas(mBmp);
                mWeb.draw(canvas);
            }

            @Override
            protected Boolean doInBackground(Object... objects) {
                Context context = (Context) objects[0];

                File dnPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File ssPath = new File(dnPath, "screenshot");
                if (!ssPath.exists()) {
                    ssPath.mkdirs();
                }

                String fileName = "test.png"; // DateFormat.format("yyyyMMdd-HHmmss", new Date()) + ".png";
                File ssFile = new File(ssPath, fileName);

                try {
                    FileOutputStream os = new FileOutputStream(ssFile);
                    mBmp.compress(Bitmap.CompressFormat.PNG, 90, os);
                    os.flush();
                    os.close();

                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + ssFile.getAbsolutePath())));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (mBmp != null) {
                    mBmp.recycle();
                    mBmp = null;
                }

                if (mLog.isDebugEnabled()) {
                    String log = "";
                    log += "===================================================================\n";
                    log += "CAPTURE END\n";
                    log += "===================================================================\n";
                    mLog.debug(log);
                }

                mProgress.setVisibility(View.GONE);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, MainActivity.this);
    }
}

// define name
// error log
// file name indexing
// saved folder