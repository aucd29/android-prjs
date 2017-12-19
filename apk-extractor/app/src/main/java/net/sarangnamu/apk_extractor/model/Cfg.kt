package net.sarangnamu.apk_extractor.model

import android.content.Context
import android.os.Environment
import net.sarangnamu.apk_extractor.MainApp
import net.sarangnamu.common.Preference
import net.sarangnamu.common.addLastSlash
import net.sarangnamu.common.config

/**
 * Created by <a href="mailto:aucd29@gmail.com">Burke Choi</a> on 2017. 11. 30.. <p/>
 */

class Cfg {
    companion object {
        val PATH = "/apks/";

        val EMAIL    = "email"
        val USERPATH = "usrPath"
        val SHOW_OPT = "showOpt"
        val SORT_BY  = "sortBy"

        val SORT_LAST_INSTALL_TIME  = "0"
        val SORT_FIRST_INSTALL_TIME = "3"
        val SORT_ALPHABET_DESC      = "2"
        val SORT_ALPHABET_ASC       = "1"

        fun downloadPath(): String {
            val path = get(USERPATH)

            return path?.addLastSlash() ?: "/sdcard" + PATH
        }

        fun get(key: String, dval: String? = null): String? {
            return MainApp.context().pref(Preference().apply { read(key, dval) })
        }

        fun set(key: String, value: String) {
            MainApp.context().pref(Preference().apply { write(key, value) })
        }
    }
}

inline fun Context.pref(param: Preference): String? {
    return config(param)
}

inline fun Context.sdPath() =
        Environment.getExternalStorageDirectory().getAbsolutePath()