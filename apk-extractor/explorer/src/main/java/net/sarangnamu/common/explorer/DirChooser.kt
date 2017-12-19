package net.sarangnamu.common.explorer

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.content.Loader
import android.text.TextUtils
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.ipaulpro.afilechooser.FileChooserActivity
import com.ipaulpro.afilechooser.FileListFragment
import com.ipaulpro.afilechooser.FileLoader
import com.ipaulpro.afilechooser.utils.FileUtils
import kotlinx.android.synthetic.main.dir_chooser.*
import kotlinx.android.synthetic.main.dlg_create_dir.view.*
import java.io.File

/**
 * Created by <a href="mailto:aucd29@hanwha.com">Burke Choi</a> on 2017. 12. 18.. <p/>
 */

class DirChooserActivity : FileChooserActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userFont(layout)

        createDir.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.dlg_create_dir, null)

            AlertDialog.Builder(this@DirChooserActivity).setView(view)
                .setPositiveButton(android.R.string.yes, { _, _ ->
                    val data = view.edit.toString()
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
        }

        setPath.setOnClickListener {
            val view = TextView(this@DirChooserActivity)
            with (view) {
                setText(R.string.setCurrentDir)
                gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
                setPadding(0, dpToPixel(15), 0, 0)
            }

            AlertDialog.Builder(this@DirChooserActivity).setView(view)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, { _, _ ->
                    setResult(Activity.RESULT_OK, Intent().apply { putExtra("path", mPath) })
                    finish()
                })
                .show()
        }
    }

    override fun instListFragment(): FileListFragment = DirListFrgmt.newInstance(mPath)

    private fun dpToPixel(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // FONT
    //
    ////////////////////////////////////////////////////////////////////////////////////

    private fun userFont(vgroup: ViewGroup) {
        val count = vgroup.childCount
        (0..count - 1).map {
            val view = layout.getChildAt(it)

            if (view is TextView) {
                view.typeface = roboto()
            } else if (view is ViewGroup) {
                userFont(view)
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