package net.sarangnamu.apk_extractor.control.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.sarangnamu.apk_extractor.MainActivity
import net.sarangnamu.apk_extractor.model.Preference
import net.sarangnamu.apk_extractor.model.pref
import net.sarangnamu.common.isForegroundApp
import org.slf4j.LoggerFactory

/**
 * Created by <a href="mailto:aucd29@gmail.com">Burke Choi</a> on 2017. 11. 30.. <p/>
 */
class Receiver: BroadcastReceiver() {
    companion object {
        private val log = LoggerFactory.getLogger(Receiver::class.java)

        val APP_EVENT   = "appEventType"
        val PKGNAME     = "appEventPkgName"
        val APP_ADDED   = "added"
        val APP_REMOVED = "removed"
        val DENY_APK    = "net.sarangnamu.apk_extractor"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_PACKAGE_ADDED == intent.getAction()) {
            val pkgName = intent.data.schemeSpecificPart

            if (DENY_APK.equals(pkgName)) {
                if (log.isDebugEnabled) {
                    log.debug("ignore pkg name : $pkgName")
                }
                return
            }

            if (!context.isForegroundApp()) {
                preference(context, APP_ADDED, pkgName)
            } else {
                showActivity(context, APP_ADDED, pkgName)
            }
        } else if (Intent.ACTION_PACKAGE_REMOVED == intent.action) {
            val pkgName = intent.data.schemeSpecificPart

            if (!context.isForegroundApp()) {
                preference(context, APP_REMOVED, pkgName)
            } else {
                showActivity(context, APP_REMOVED, pkgName)
            }
        }
    }

    private fun showActivity(context: Context, type: String, pkgName: String) {
        context.startActivity(Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(APP_EVENT, type)
            putExtra(PKGNAME, pkgName)
        })
    }

    private fun preference(context: Context, type: String, pkgName: String) {
        context.pref(Preference().apply { write(APP_EVENT, type) })
        context.pref(Preference().apply { write(PKGNAME, pkgName) })
    }
}