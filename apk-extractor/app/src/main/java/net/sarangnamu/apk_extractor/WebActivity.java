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

package net.sarangnamu.apk_extractor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.sarangnamu.apk_extractor.model.Cfg;
import net.sarangnamu.common.BkString;
import net.sarangnamu.common.permission.RunTimePermission;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * Created by <a href="mailto:aucd29@gmail.com">Burke Choi</a> on 2016. 7. 26.. <p/>
 */
public class WebActivity extends AppCompatActivity {
    private static final org.slf4j.Logger mLog = org.slf4j.LoggerFactory.getLogger(WebActivity.class);

    @BindView(R.id.web)
    WebView mWeb;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web);
        ButterKnife.bind(this);

        // Uri uri = Uri.parse("http://www.apkmirror.com/?s=apk+extractor&post_type=app_release&searchtype=apk");
        mWeb.getSettings().setDomStorageEnabled(true);
        mWeb.getSettings().setJavaScriptEnabled(true);
        mWeb.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        mWeb.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> RunTimePermission.check(WebActivity.this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, result -> {
            String downloadFile = Cfg.getDownPath(getApplicationContext());
            downloadFile += BkString.getFileName(url);

            if (mLog.isDebugEnabled()) {
                String log = "";
                log += "===================================================================\n";
                log += "url : " + url + "\n";
                log += "downloadFile : " + downloadFile + "\n";
                log += "===================================================================\n";
                mLog.debug(log);
            }

            try {
                download(url, downloadFile);
            } catch (Exception e) {
                e.printStackTrace();
                mLog.error(e.getMessage());
            }
        }));
        mWeb.loadUrl("http://www.apkmirror.com");
    }

    @Override
    public void onBackPressed() {
        if (mWeb.canGoBack()) {
            mWeb.goBack();
            return ;
        }

        super.onBackPressed();
    }

    private void download(String url, String destFile) throws IOException {
        new AsyncTask<Void, Integer, Boolean>() {
            ProgressDialog mDlg;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                mDlg = new ProgressDialog(WebActivity.this);
                mDlg.setProgress(0);
                mDlg.setMax(100);
                mDlg.setMessage("Downloading...");
                mDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mDlg.show();
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                mDlg.setProgress(values[0]);
            }

            @Override
            protected Boolean doInBackground(Void... data) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(url).build();

                    Response response = client.newCall(request).execute();
                    ResponseBody body = response.body();
                    long contentLength = body.contentLength();
                    BufferedSource source = body.source();

                    BufferedSink sink = Okio.buffer(Okio.sink(new File(destFile)));
                    Buffer sinkBuffer = sink.buffer();
                    long totalBytesRead = 0;
                    int bufferSize = 8 * 1024;
                    for (long bytesRead; (bytesRead = source.read(sinkBuffer, bufferSize)) != -1; ) {
                        sink.emit();
                        totalBytesRead += bytesRead;
                        int progress = (int) ((totalBytesRead * 100) / contentLength);
                        publishProgress(progress);
                    }
                    sink.flush();
                    sink.close();
                    source.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                mDlg.dismiss();
            }
        }.execute();
    }
}
