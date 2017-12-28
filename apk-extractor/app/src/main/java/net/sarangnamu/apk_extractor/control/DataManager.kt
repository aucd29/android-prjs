package net.sarangnamu.apk_extractor.control

import android.app.Activity
import android.graphics.Typeface.NORMAL
import android.text.TextUtils
import net.sarangnamu.apk_extractor.model.AppInfo
import net.sarangnamu.apk_extractor.model.AppList
import org.slf4j.LoggerFactory

/**
 * Created by <a href="mailto:aucd29@gmail.com">Burke Choi</a> on 2017. 12. 5.. <p/>
 */

class DataManager private constructor() {
    private object Holder { val INSTANCE = DataManager() }
    private val log = LoggerFactory.getLogger(DataManager::class.java)

    companion object {
        val get: DataManager by lazy { Holder.INSTANCE }

        val NORMAL   = 0
        val SEARCHED = 1
    }

    var MODE = NORMAL

    private var appList: ArrayList<ArrayList<AppInfo>> = ArrayList()

    init {
        appList.add(ArrayList())
        appList.add(ArrayList())
    }

    fun load() {
        val data = AppList.get.load()
        appList[MODE] = data

        if (log.isDebugEnabled) {
            log.debug("LOAD APPS COUNT:(${data.size}), MODE($MODE) ")
        }
    }

    fun toggleMode() { MODE = if (MODE == NORMAL) SEARCHED else NORMAL }

    fun count(): Int = appList[MODE].size

    fun appInfo(pos: Int): AppInfo? = appList[MODE].get(pos)

    fun listAll(): ArrayList<AppInfo> = appList[MODE]

    fun removeAt(pos: Int) = appList[MODE].removeAt(pos)

    fun remove(appInfo: AppInfo) = appList[MODE].remove(appInfo)

    fun removeSearchedList() {
        appList[SEARCHED].clear()
        MODE = NORMAL
    }

    fun changeOrderBy() {
        appList[MODE] = AppList.get.sorting(appList.get(MODE))
    }

    fun searching(keyword: String) {
        if (!TextUtils.isEmpty(keyword)) {
            appList[SEARCHED].clear()

            val lowerKeyword = keyword.toLowerCase()
            appList[NORMAL].forEach {
                if (it.appName.toLowerCase().contains(lowerKeyword)) {
                    appList[SEARCHED].add(it)
                }
            }

            MODE = SEARCHED
        } else {
            MODE = NORMAL
        }
    }
}
