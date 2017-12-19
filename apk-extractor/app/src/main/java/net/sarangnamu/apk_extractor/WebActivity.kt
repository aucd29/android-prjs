package net.sarangnamu.apk_extractor

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_web.*
import net.sarangnamu.common.DialogParam
import net.sarangnamu.common.permission.runtimePermission
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Okio
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * Created by <a href="mailto:aucd29@gmail.com">Burke Choi</a> on 2017. 11. 30.. <p/>
 */

class WebActivity : AppCompatActivity() {
    private val log = LoggerFactory.getLogger(WebActivity::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_web)
        init()
    }

    override fun onBackPressed() {
        with (web) {
            if (canGoBack()) {
                goBack()
                return
            }
        }

        super.onBackPressed()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun init() {
        with (web) {
            settings.domStorageEnabled = true
            settings.javaScriptEnabled = true
            setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                runtimePermission(arrayListOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), {
                    if (it) {
                        DownloadTask(WeakReference(this@WebActivity), url, "")
                    }
                })
            }

            loadUrl("http://www.apkmirror.com")

            webViewClient = object: WebViewClient() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    return super.shouldOverrideUrlLoading(view, request)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun download(url: String, dest: String) {

    }

    class DownloadTask(val weak: WeakReference<Activity>, val url: String, val dest: String) : AsyncTask<Void, Int, Boolean>() {
        lateinit var dialog: ProgressDialog

        override fun onPreExecute() {
            super.onPreExecute()

            dialog = ProgressDialog(weak.get())
            with (dialog) {
                progress = 0
                max = 100
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                setMessage("Downloading...")
                show()
            }
        }

        override fun onProgressUpdate(vararg values: Int?) {
            values[0]?.let {
                dialog.progress = it
            }
        }

        override fun doInBackground(vararg params: Void?): Boolean {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body()
            val length = body.contentLength()
            val src = body.source()
            val sink = Okio.buffer(Okio.sink(File(dest)))
            val buffer = sink.buffer()
            val bufferSize: Long = 8 * 1024

            var total: Long = 0
            var read: Long = src.read(buffer, bufferSize)
            var progress = 0

            while (read != -1L) {
                sink.emit()
                total += read

                progress = (total * 100 / length).toInt()
                publishProgress(progress)

                read = src.read(buffer, bufferSize)
            }

            sink.flush()
            sink.close()
            src.close()

            return true
        }

        override fun onPostExecute(result: Boolean?) {
            dialog.dismiss()
        }
    }
}