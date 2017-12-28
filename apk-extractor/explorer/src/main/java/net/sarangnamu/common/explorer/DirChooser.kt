package net.sarangnamu.common.explorer

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.content.Loader
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.ipaulpro.afilechooser.FileChooserActivity
import com.ipaulpro.afilechooser.FileListFragment
import com.ipaulpro.afilechooser.FileLoader
import com.ipaulpro.afilechooser.utils.FileUtils
import kotlinx.android.synthetic.main.dir_chooser.*
import kotlinx.android.synthetic.main.dlg_create_dir.view.*
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Created by <a href="mailto:aucd29@hanwha.com">Burke Choi</a> on 2017. 12. 18.. <p/>
 */

class DirChooserActivity : FileChooserActivity() {
    private val log = LoggerFactory.getLogger(DirChooserActivity::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userFont(explorer_layout)

        createDir.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.dlg_create_dir, null)
            userFont(view.create_dir_layout)

            AlertDialog.Builder(this@DirChooserActivity).setView(view)
                .setPositiveButton(android.R.string.yes, { _, _ ->
                    keyboard(view.edit, false)

                    val data = view.edit.text.toString()

                    if (log.isDebugEnabled) {
                        log.debug("CREATE DIR : $data")
                    }

                    if (TextUtils.isEmpty(data)) {
                        return@setPositiveButton
                    }

                    val newDir = File(mPath, data)
                    if (!newDir.exists()) {
                        newDir.mkdirs()
                    }

                    replaceFragment(newDir)
                })
                .setNegativeButton(android.R.string.no, null)
                .show()

            keyboard(view.edit, true)
        }

        setPath.setOnClickListener {
            AlertDialog.Builder(this@DirChooserActivity).setMessage(R.string.setCurrentDir)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, { _, _ ->
                    if (log.isDebugEnabled) {
                        log.debug("CHOOSE DIR : $mPath")
                    }

                    setResult(Activity.RESULT_OK, Intent().apply { putExtra("path", mPath) })
                    finish()
                })
                .show()
        }
    }

    override fun getContentLayoutId(): Int = R.layout.dir_chooser

    override fun instListFragment(): FileListFragment = DirListFrgmt.newInstance(mPath)

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // USER
    //
    ////////////////////////////////////////////////////////////////////////////////////

    private fun dpToPixel(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun keyboard(view: View, show: Boolean) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        view.run {
            if (show) {
                postDelayed({
                    requestFocus()
                    imm.showSoftInput(this, InputMethodManager.SHOW_FORCED)
                }, 400)
            } else {
                imm.hideSoftInputFromWindow(windowToken, 0)
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // FONT
    //
    ////////////////////////////////////////////////////////////////////////////////////

    private fun userFont(vgroup: ViewGroup) {
        val count = vgroup.childCount
        (0..count - 1).map {
            val view = vgroup.getChildAt(it)

            when (view) {
                is TextView -> view.typeface = roboto()
                is ViewGroup -> userFont(view)
            }
        }
    }

    private fun roboto(): Typeface = Typeface.createFromAsset(assets, "fonts/Roboto-Light.ttf")
}

////////////////////////////////////////////////////////////////////////////////////
//
// DirListFrgmt
//
////////////////////////////////////////////////////////////////////////////////////

class DirListFrgmt : FileListFragment() {
    companion object {
        fun newInstance(path: String): DirListFrgmt = DirListFrgmt().apply {
            arguments = Bundle().apply { putString(FileChooserActivity.PATH, path) }
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<MutableList<File>> {
        return super.onCreateLoader(id, args)
    }

    override fun onLoadFinished(loader: Loader<List<File>>?, data: List<File>?) {
        mAdapter.setListItems(data)

        if (isResumed) {
            setListShown(true)
        } else {
            setListShownNoAnimation(true)
        }
    }
}

////////////////////////////////////////////////////////////////////////////////////
//
// DirLoader
//
////////////////////////////////////////////////////////////////////////////////////

class DirLoader(context: Context, path: String) : FileLoader(context, path) {
    override fun loadInBackground(): List<File> = FileUtils.getDirList(mPath)
}