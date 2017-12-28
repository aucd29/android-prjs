package net.sarangnamu.apk_extractor.model

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.util.Base64
import net.sarangnamu.apk_extractor.MainApp
import net.sarangnamu.common.addLastSlash

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

inline fun Context.sdPath() =
        Environment.getExternalStorageDirectory().getAbsolutePath()

@SuppressLint("CommitPrefEdits")
inline fun Context.pref(param: Preference): String? {
    val pref = getSharedPreferences("shared.pref", Context.MODE_PRIVATE)

    if (param.write) {
        // write mode
        val editor = pref.edit().putString(param.key, param.value)
        if (param.async) {
            editor.apply()
        } else {
            editor.commit()
        }
    } else {
        // read mode
        val value = pref.getString(param.key, param.value)
        return value?.let { String(Base64.decode(it, Base64.DEFAULT)) } ?: value
    }

    return null
}

class Preference {
    lateinit var key: String
    var value: String? = null
    var write = false
    var async = false

    /**
     * read shared preference
     */
    fun read(key: String, value: String?) {
        data(key, value)
        write = false
    }

    /**
     * write shared preference
     */
    fun write(key: String, value: String?) {
        data(key, value)
        write = true
    }

    private fun data(key: String, value: String?) {
        this.key = key
        this.value = value?.let { Base64.encodeToString(it.toByteArray(), Base64.DEFAULT) } ?: value
    }
}