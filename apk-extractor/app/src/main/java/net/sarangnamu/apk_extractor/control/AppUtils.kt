package net.sarangnamu.apk_extractor.control

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.annotation.StringRes
import net.sarangnamu.apk_extractor.MainActivity
import net.sarangnamu.apk_extractor.R
import net.sarangnamu.apk_extractor.model.AppInfo
import net.sarangnamu.apk_extractor.model.Cfg
import net.sarangnamu.common.*
import java.io.File

/**
 * Created by <a href="mailto:aucd29@gmail.com">Burke Choi</a> on 2017. 12. 1.. <p/>
 */

fun AppInfo.sharing(activity: Activity, target: String) {
    // https://developer.android.com/training/sharing/send.html#send-binary-content
    activity.startActivityForResult(Intent(Intent.ACTION_SEND).apply {
        type = "application/octet-stream"
        putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(target)))
    }, MainActivity.ACTIVITY_SHARE)
}

fun AppInfo.copy(activity: Activity) {
    val loading   = activity.loading(DialogParam(activity, R.string.plsWait))
    val unitCount = size.unitCount()

    loading.max = size.shr10(unitCount)
    loading.progress = 0
    loading.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)

    loading.show()

    Thread {
        val f = File(path)
        var name: String
        if (appName.matches(Regex("^[A-Za-z0-9. ]+$"))) {
            name = appName
        } else {
            name = packageName
        }

        name += "-$versionName.apk"

        val fullpath = Cfg.downloadPath() + name
        val dest = File(fullpath)

        if (dest.exists()) {
        } else {
            f.copy(dest, object: FileListener() {
                override fun progress() {
                    super.progress()

                    loading.progressFileSize(unitCount, current.toLong(), size)
                }

                override fun finish(code: Int) {
                    loading.hide()
                }
            })
        }
    }
}

inline fun Long.shr10(unit: Int): Int {
    return (this shr (10 * unit)).toInt()
}

inline fun ProgressDialog.progressFileSize(unit: Int, current: Long, total: Long) {
    setProgress(current.shr10(unit))
    setProgressNumberFormat(String.format("%s/%s", current.toFileSizeString(unit), total.toFileSizeString(unit)))
}

inline fun Long.unitCount(): Int {
    var u = 0
    var size = this

    while (size > 1024 * 1024) {
        u++
        size = size shr 10
    }

    if (size > 1024) {
        u++
    }

    return u
}

inline fun Long.toFileSizeString(unit: Int): String {
    val size = this shr (10 * unit)

    return String.format("%.1f %cB", size / 1024f, " KMGTPE"[unit])
}

inline fun Context.string(@StringRes resid: Int): String? = getString(resid)