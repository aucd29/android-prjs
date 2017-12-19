package net.sarangnamu.apk_extractor

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.view.*
import kotlinx.android.synthetic.main.dlg_sortby.*
import kotlinx.android.synthetic.main.dlg_timer.*
import net.sarangnamu.apk_extractor.control.*
import net.sarangnamu.apk_extractor.control.receiver.Receiver
import net.sarangnamu.apk_extractor.model.Cfg
import net.sarangnamu.apk_extractor.model.sdPath
import net.sarangnamu.common.*
import net.sarangnamu.common.permission.mainRuntimePermission
import net.sarangnamu.common.permission.runtimePermission
import org.slf4j.LoggerFactory
import java.lang.ref.WeakReference

/**
 * Created by <a href="mailto:aucd29@gmail.com">Burke Choi</a> on 2017. 12. 1.. <p/>
 */

class MainActivity: AppCompatActivity(), View.OnClickListener {
    companion object {
        private val log = LoggerFactory.getLogger(MainActivity::class.java)

        val ACTIVITY_SHARE      = 100
        val ACTIVITY_EXPLORER   = 200
        val ACTIVITY_DEL        = 300

        val SLIDING_MARGIN      = 160
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initLabel()
        initDownloadPath()
        initMenu()
        initSearch()

        loadList(true)
    }

    fun initLabel() {
        val developer = String.format("<b>%s</b> <a href='http://sarangnamu.net'>@aucd29</a>", string(R.string.dev))

        main_title.text   = string(R.string.appName)!!.html()
        main_dev.text     = developer.html()
        main_progress.max = 100
    }

    fun initDownloadPath() {
        val path = String.format("<b>%s</b> : %s", string(R.string.downloadPath), Cfg.downloadPath().replace(sdPath(), "/sdcard"))
        main_path.text = path.html()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val event   = intent.getStringExtra(Receiver.APP_EVENT)
        val pkgName = intent.getStringExtra(Receiver.PKGNAME)

        if (log.isDebugEnabled) {
            log.debug("event: $event\npkgName: $pkgName")
        }

        if (!TextUtils.isEmpty(event) && !TextUtils.isEmpty(pkgName)) {
            // TODO UPDATE LIST VIEW
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            ACTIVITY_EXPLORER -> {
                val path = intent.getStringExtra("path")
                path?.let {
                    Cfg.set(Cfg.USERPATH, it)
                    initDownloadPath()
                }
            }
            ACTIVITY_SHARE -> {
                when (resultCode) {
                    RESULT_OK or RESULT_CANCELED -> { }
                    else -> timerDialog(R.string.sendMailFail)
                }
            }
            ACTIVITY_DEL -> {

            }
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MENU -> {
                main_menu.performClick()
                return false
            }
        }

        return super.onKeyUp(keyCode, event)
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // MENU
    //
    ////////////////////////////////////////////////////////////////////////////////////

    fun initMenu() {
        main_menu.setOnClickListener { v ->
            val menu = if (Cfg.get(Cfg.SHOW_OPT) == "0") R.menu.main else R.menu.main2

            MenuManager.get.show(this, v, menu, { item ->
                when (item!!.itemId) {
                    R.id.mnu_search -> changeSearchUi()
                    R.id.mnu_search_from_mirror -> searchFromMirror()
                    R.id.mnu_license -> showLicense()
                    R.id.mnu_setSdPath -> fileExplorer()
                    R.id.mnu_showSystemApp -> loadSystemApp()
                    R.id.mnu_showInstalledApp -> loadInstalledApp()
                    R.id.mnu_specialThanks -> showLicense2("file:///android_assets/special_thanks.html")
                    R.id.mnu_sortBy -> sortBy()
                }

                false
            })
        }
    }

    fun searchFromMirror() {
        hideKeyboard(main_search)
        startActivity(Intent(this@MainActivity, WebActivity::class.java))
    }

    fun fileExplorer() {
        mainRuntimePermission(arrayListOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), { res ->
            if (res) {
                startActivityForResult(Intent(this@MainActivity, net.sarangnamu.common.explorer.DirChooserActivity::class.java), ACTIVITY_EXPLORER)
            }
        })
    }

    fun loadSystemApp() {
        Cfg.set(Cfg.SHOW_OPT, "1")
        loadList(false)
    }

    fun loadInstalledApp() {
        Cfg.set(Cfg.SHOW_OPT, "0")
        loadList(false)
    }

    fun sortBy() {
        val dialog = dialog(DialogParam().apply {
            title = string(R.string.mnu_sortBy)
            resid = R.layout.dlg_sortby
        })

        val dlg = dialog.show()
        val sortby = Cfg.get(Cfg.SORT_BY)
        AdMob.get.load(dlg, R.id.adView)

        when (sortby) {
            Cfg.SORT_ALPHABET_ASC       -> { alphabetAsc.isChecked = true }
            Cfg.SORT_ALPHABET_DESC      -> { alphabetDesc.isChecked = true }
            Cfg.SORT_FIRST_INSTALL_TIME -> { firstInstallTime.isChecked = true }
            Cfg.SORT_LAST_INSTALL_TIME  -> { lastInstallTime.isChecked = true }
        }

        sortGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.alphabetAsc      -> Cfg.set(Cfg.SORT_BY, Cfg.SORT_ALPHABET_ASC)
                R.id.alphabetDesc     -> Cfg.set(Cfg.SORT_BY, Cfg.SORT_ALPHABET_DESC)
                R.id.firstInstallTime -> Cfg.set(Cfg.SORT_BY, Cfg.SORT_FIRST_INSTALL_TIME)
                R.id.lastInstallTime  -> Cfg.set(Cfg.SORT_BY, Cfg.SORT_LAST_INSTALL_TIME)
            }

            dlg.dismiss()
        }

        sortGroup.font("Roboto-Light")
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // SEARCH
    //
    ////////////////////////////////////////////////////////////////////////////////////

    fun initSearch() {
        forceHideKeyboard(window)
        main_search.imeOptions = EditorInfo.IME_ACTION_DONE
        main_search.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                DataManager.get.searching(s.toString())
                updateList()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })
        main_search.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                changeSearchUi()
            }

            false
        }

        main_search.roboto()
        hideKeyboard(main_search)
    }

    fun changeSearchUi() {
        if (main_search.visibility == View.GONE) {
            main_search.visibility    = View.VISIBLE
            main_tv_search.visibility = View.VISIBLE
            main_title.visibility     = View.GONE
            main_title_bar.fadeColorRes(R.color.dBg, R.color.dBgSearch)
            window.fadeStatusBarRes(R.color.colorPrimaryDark, R.color.dBgSearch)
            main_search.text.clear()

            showKeyboard(main_search)

            list.hide()
        } else {
            main_search.visibility    = View.GONE
            main_tv_search.visibility = View.GONE
            main_title.visibility     = View.VISIBLE
            main_title_bar.fadeColorRes(R.color.dBgSearch, R.color.dBg)
            window.fadeStatusBarRes(R.color.dBgSearch, R.color.colorPrimaryDark)

            hideKeyboard(main_search)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // LIST
    //
    ////////////////////////////////////////////////////////////////////////////////////

    fun loadList(reload: Boolean) {
        with (Cfg) {
            if (get(SORT_BY) == "0") {
                set(SORT_BY, SORT_LAST_INSTALL_TIME)
            }
        }

        val loading = loading(DialogParam(this@MainActivity, R.string.plsWait), false)
        loading.show()

        LoadTask(WeakReference(this@MainActivity), {
            if (reload) {
                initList()
            } else {
                list.hide()
                updateList()
            }

            loading.hide()
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    class LoadTask(val weak: WeakReference<Activity>, val endListener: (result: Boolean) -> Unit)
        : AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg params: Void?): Boolean {
            DataManager.get.load(weak.get()!!)
            return true
        }

        override fun onPostExecute(result: Boolean) {
            endListener.invoke(result)
        }
    }

    fun initList() {
        with (list) {
            adapter        = AppAdapter(this@MainActivity, this@MainActivity)
            slidingMargin  = SLIDING_MARGIN
            buttonLayoutId = R.id.btnLayout
            rowId          = R.id.row

            // android.R 로 등록되어 있는 id 는 kotlin extension 에서 인식이 안되는 모양
            emptyView      = findViewById(android.R.id.empty)
        }
    }

    fun updateList() {
        (list.adapter as BaseAdapter)?.run { notifyDataSetChanged() }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // OnClickListener
    //
    ////////////////////////////////////////////////////////////////////////////////////

    override fun onClick(v: View) {
        val ph  = v.tag as PosHolder
        val pos = ph.position

        when (ph.type) {
            AppAdapter.ET_MENU -> {
                if (main_search.visibility != View.GONE) {
                    main_search.visibility = View.GONE
                    main_tv_search.visibility = View.GONE
                    main_title.visibility = View.GONE
                    main_title_bar.fadeColorRes(R.color.dBgSearch, R.color.dBg)
                    window.fadeStatusBarRes(R.color.dBgSearch, R.color.dBg)

                    hideKeyboard(main_search)
                }

                list.toggle(v)
            }
            AppAdapter.ET_DELETE -> {
                if (DataManager.get.MODE == DataManager.SEARCHED) {
                    DataManager.get.removeSearchedList()
                    updateList()

                    if (main_search.visibility != View.GONE) {
                        changeSearchUi()
                    }
                }

                DataManager.get.appInfo(pos)?.let {
                    startActivityForResult(Intent(Intent.ACTION_DELETE).apply {
                        data = Uri.parse("package:" + it.packageName)
                    }, ACTIVITY_DEL)
                }
            }
            AppAdapter.ET_SDCARD -> {
                runtimePermission(arrayListOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), { res ->
                    if (res) {
                        val info = DataManager.get.appInfo(pos)
                        info?.copy(this@MainActivity)
                    }
                })
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // dialog
    //
    ////////////////////////////////////////////////////////////////////////////////////

    fun timerDialog(@StringRes msgId: Int) {
        val dlg = dialog(DialogParam().apply {
            resid = R.layout.dlg_timer
        }, 1000)

        dlg.msg.text = string(msgId)
    }
}