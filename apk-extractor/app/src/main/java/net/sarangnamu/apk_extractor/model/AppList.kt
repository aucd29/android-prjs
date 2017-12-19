package net.sarangnamu.apk_extractor.model

import android.app.Activity
import android.content.pm.ApplicationInfo
import com.squareup.picasso.Picasso
import net.sarangnamu.common.toFileSizeString
import java.io.File
import android.graphics.Bitmap
import android.content.pm.PackageManager
import android.content.Context
import android.graphics.BitmapFactory
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import net.sarangnamu.apk_extractor.*
import org.slf4j.LoggerFactory
import java.util.*


/**
 * Created by <a href="mailto:aucd29@gmail.com">Burke Choi</a> on 2017. 11. 30.. <p/>
 */

class AppList private constructor() {
    private object Holder { val INSTANCE = AppList() }

    companion object {
        private val log = LoggerFactory.getLogger(AppList::class.java)
        val get: AppList by lazy { Holder.INSTANCE }
    }

    fun load(hideSystemApp: Boolean = false): ArrayList<AppInfo> {
        val context  = MainApp.context()
        val packages = context.packageManager.getInstalledPackages(0)
        val apps: ArrayList<AppInfo> = ArrayList()

        for (info in packages) {
            if (hideSystemApp) {
                if ((info.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
                    continue
                }
            }

            val length = File(info.applicationInfo.sourceDir).length()
            val app = AppInfo(
                    info.applicationInfo.loadLabel(context.packageManager).toString(),
                    info.packageName,
                    info.versionName,
                    info.versionCode,
                    info.applicationInfo.sourceDir,
                    length,
                    length.toFileSizeString(),
                    info.firstInstallTime
            )

            apps.add(app)
        }

        return sorting(apps)
    }

    fun sorting(apps: ArrayList<AppInfo>): ArrayList<AppInfo> {
        val sortby  = Cfg.get(Cfg.SORT_BY, Cfg.SORT_LAST_INSTALL_TIME)
        when (sortby) {
            Cfg.SORT_ALPHABET_ASC       -> Collections.sort(apps, SortByAlphabet(true))
            Cfg.SORT_ALPHABET_DESC      -> Collections.sort(apps, SortByAlphabet(false))
            Cfg.SORT_FIRST_INSTALL_TIME -> Collections.sort(apps, SortByInstallTime(true))
            else -> Collections.sort(apps, SortByInstallTime(false))
        }

        return apps
    }
}

data class AppInfo (
        val appName: String,
        val packageName: String,
        val versionName: String?,
        val versionCode: Int = 0,
        val path: String,
        val size: Long,
        val appSize: String,
        val firstInstallTime:Long = 0
)

// https://github.com/mohamad-amin/Android-FastSearch/blob/master/app/src/main/java/com/mohamadamin/fastsearch/free/utils/PicassoUtils.java
// https://medium.com/@jpardogo/requesthandler-api-for-picasso-library-c3ee7c4bec25

class ApkRequestHandler (val context: Context) : RequestHandler() {
    companion object {
        private val log = LoggerFactory.getLogger(ApkRequestHandler::class.java)
        val ICON_SCHEME = "icon"
    }

    override fun canHandleRequest(data: Request): Boolean {
        return ICON_SCHEME == data.uri.scheme
    }

    override fun load(request: Request, networkPolicy: Int): Result {
        if (log.isDebugEnabled) {
            log.debug("request uri : ${request.uri.path}")
        }

        val bmp: Bitmap
        val info = context.packageManager.getPackageArchiveInfo(request.uri.path, 0)
        if (info != null) {
            bmp = info.applicationInfo.apply {
                sourceDir = request.uri.path
                publicSourceDir = request.uri.path
            }.loadIcon(context.packageManager).bitmap()
        } else {
            bmp = BitmapFactory.decodeResource(context.resources, R.drawable.ic_photo_library_white_24dp)
        }

        return Result(bmp.ratioResize(R.dimen.main_icon_size), Picasso.LoadedFrom.DISK)
    }
}

////////////////////////////////////////////////////////////////////////////////////
//
// SORT
//
////////////////////////////////////////////////////////////////////////////////////

class SortByInstallTime(val firstTime: Boolean) : Comparator<AppInfo> {
    override fun compare(info1: AppInfo, info2: AppInfo): Int {
        val l: AppInfo
        val r: AppInfo

        if (firstTime) {
            l = info1
            r = info2
        } else {
            l = info2
            r = info1
        }

        return if (l.firstInstallTime > r.firstInstallTime) 1
          else if (l.firstInstallTime < r.firstInstallTime) -1
          else 0
    }
}

class SortByAlphabet(val asc: Boolean) : Comparator<AppInfo> {
    override fun compare(info1: AppInfo, info2: AppInfo): Int{
        val l: AppInfo
        val r: AppInfo

        if (asc) {
            l = info1
            r = info2
        } else {
            l = info2
            r = info1
        }

        return l.appName.compareTo(r.appName)
    }
}
