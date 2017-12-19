package net.sarangnamu.apk_extractor.control

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item.view.*
import net.sarangnamu.apk_extractor.MainApp
import net.sarangnamu.apk_extractor.R
import net.sarangnamu.apk_extractor.model.ApkRequestHandler
import net.sarangnamu.common.displayDensity
import net.sarangnamu.common.dpToPixel
import net.sarangnamu.common.trycatch

/**
 * Created by <a href="mailto:aucd29@gmail.com">Burke Choi</a> on 2017. 12. 5.. <p/>
 */

class AppAdapter(val activity: Activity, val listener: View.OnClickListener) : BaseAdapter() {
    companion object {
        val MARGIN    = 160

        val ET_SDCARD = 0
        val ET_EMAIL  = 1
        val ET_MENU   = 2
        val ET_DELETE = 3
    }

    var margin: Int = (MARGIN * MainApp.context().displayDensity()).toInt()
    val picasso = Picasso.Builder(activity).addRequestHandler(ApkRequestHandler(activity)).build()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val view: View

        if (convertView == null) {
            view = LayoutInflater.from(activity).inflate(R.layout.item, null)
            holder = with(view) {
                sd.setOnClickListener(listener)
                email.setOnClickListener(listener)
                delete.setOnClickListener(listener)
                row.setOnClickListener(listener)

                ViewHolder(icon, name, size, pkgName, version, sd, email, delete, btnLayout, row)
            }

            view.tag = holder
        } else {
            view   = convertView
            holder = convertView.tag as ViewHolder
        }

        DataManager.get.appInfo(position)?.let {
            with(holder) {
                name.text    = it.appName
                size.text    = it.appSize
                pkgName.text = it.packageName
                version.text = it.versionName?.let { "($it)" } ?: ""

                //memoryPolicy(MemoryPolicy.NO_CACHE).
                picasso.load("icon://" + it.path).into(icon)

                sd.tag     = PosHolder(position, ET_SDCARD, row)
                email.tag  = PosHolder(position, ET_EMAIL, row)
                delete.tag = PosHolder(position, ET_DELETE, row)
                row.tag    = PosHolder(position, ET_MENU, row)
            }
        }

        return view
    }

    override fun getCount(): Int = try { DataManager.get.listAll().size } catch (e: Exception) { 0 }
    override fun getItem(position: Int): Any? = null
    override fun getItemId(position: Int): Long = 0
}

data class ViewHolder (
        val icon: ImageView,
        val name: TextView,
        val size: TextView,
        val pkgName: TextView,
        val version: TextView,
        val sd: TextView,
        val email: TextView,
        val delete: TextView,
        val btnLayout: LinearLayout,
        val row: RelativeLayout
)

data class PosHolder (
        val position: Int,
        val type: Int,
        val row: View
)