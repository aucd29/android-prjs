package net.sarangnamu.apk_extractor

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Build
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.Spanned
import android.util.Base64
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.PopupMenu
import kotlinx.android.synthetic.main.activity_web.*
import net.sarangnamu.apk_extractor.control.string
import net.sarangnamu.common.DialogParam
import net.sarangnamu.common.addEndListener
import net.sarangnamu.common.*

/**
 * Created by <a href="mailto:aucd29@gmail.com">Burke Choi</a> on 2017. 12. 1.. <p/>
 */

fun Drawable.bitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
    if (this is BitmapDrawable) {
        return bitmap
    }

    val bmp = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, config)
    val canvas = Canvas(bmp)

    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)

    return bmp
}

fun Bitmap.resize(w: Int, h: Int): Bitmap {
    val matrix = Matrix()
    val scaleW = w.toFloat() / width.toFloat()
    val scaleH = h.toFloat() / height.toFloat()

    matrix.postScale(scaleW, scaleH)

    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, false)
}

fun Bitmap.ratioResize(@DimenRes resid: Int): Bitmap {
    val context = MainApp.context()
    val size = context.resources.getDimensionPixelSize(resid)

    if (width > height) {
        return if (width > size) {
            val scale = size / width
            resize(width * scale, height * scale)
        } else this
    } else if (height > width) {
        return if (height > size) {
            val scale = size / height
            resize(width * scale, height * scale)
        } else this
    } else {
        return if (width > size) resize(size, size) else this
    }
}

class MenuManager {
    private object Holder { val INSTANCE = MenuManager() }

    companion object {
        val get: MenuManager by lazy { Holder.INSTANCE }
    }

    private lateinit var popup: PopupMenu

    fun show(context: Context, v: View, resid: Int, listener: (MenuItem?) -> Boolean) {
        popup = PopupMenu(context, v)
        popup.menuInflater.inflate(resid, popup.menu)
        popup.setOnMenuItemClickListener(listener)
        popup.show()
    }
}

inline fun View.fadeColor(fcolor: Int, scolor: Int, noinline f: ((Animator?) -> Unit)? = null, duration: Long = 500) {
    ObjectAnimator.ofObject(this, "backgroundColor", ArgbEvaluator(), fcolor, scolor).apply {
        this.duration = duration
        f?.let { this.addEndListener(it) }
    }.start()
}

inline fun View.fadeColorRes(@ColorRes fcolor: Int, @ColorRes scolor: Int, noinline f: ((Animator?) -> Unit)? = null, duration: Long = 500) {
    fadeColor(context.color(fcolor), context.color(scolor), f, duration)
}

inline fun Context.color(@ColorRes resid: Int): Int = ContextCompat.getColor(this, resid)
inline fun Context.colorList(@ColorRes resid: Int): ColorStateList = ContextCompat.getColorStateList(this, resid)

inline fun Window.fadeStatusBar(fcolor:Int, scolor: Int, noinline f: ((Animator?) -> Unit)? = null, duration: Long = 500) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        ValueAnimator.ofArgb(fcolor, scolor).apply {
            this.duration = duration
            this.addUpdateListener { statusBarColor = it.animatedValue as Int }
            f?.let { this.addEndListener(it) }
        }.start()
    }
}

inline fun Window.fadeStatusBarRes(@ColorRes fcolor: Int, @ColorRes scolor: Int, noinline f: ((Animator?) -> Unit)? = null, duration: Long = 500) {
    fadeStatusBar(context.color(fcolor), context.color(scolor), f, duration)
}

inline fun Activity.dialog(params: DialogParam, killTimeMillis: Long): Dialog {
    val dlg = dialog(params).show()
    window.decorView.postDelayed({ dlg.dismiss() }, killTimeMillis)

    return dlg
}

inline fun Activity.loading(params: DialogParam, hor_style: Boolean): ProgressDialog {
    val bd = ProgressDialog(this)

    if (hor_style) {
        bd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
    } else {
        bd.setProgressStyle(ProgressDialog.STYLE_SPINNER)
    }

    with (params) {
        message?.let { bd.setMessage(message) }
        if (resid != 0) {
            bd.setView(layoutInflater.inflate(resid, null))
        }
    }

    return bd
}

inline fun String.html(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}

inline fun Activity.browser(url: String, @StringRes titleId: Int = 0) {
    dialog(DialogParam().apply {
        resid = R.layout.activity_web
        textOnly = true
        if (titleId != 0) {
            title = string(titleId)
        }
    }).show().run {
        web.loadUrl(url)
        web.layoutParams = FrameLayout.LayoutParams(MainApp.screenX(), MainApp.screenY())

        fullscreen()
    }
}

inline fun Dialog.fullscreen() {
    window.run {
        val attr = attributes
        attr.run {
            width  = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
        attributes = attr

        setBackgroundDrawableResource(android.R.color.transparent)
    }
}

fun Activity.async(background: (() -> Boolean)? = null, post: ((result: Boolean) -> Unit)? = null) {
    Async(background, post).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
}

fun Context.async(background: (() -> Boolean)? = null, post: ((result: Boolean) -> Unit)? = null) {
    Async(background, post).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
}

internal class Async(val background: (() -> Boolean)?, val post: ((result: Boolean) -> Unit)?)
    : AsyncTask<Void, Void, Boolean>() {
    override fun doInBackground(vararg params: Void?): Boolean {
        return background?.invoke() ?: true
    }

    override fun onPostExecute(result: Boolean) {
        post?.invoke(result)
    }
}

fun Context.clipboard(key: String): String? {
    val manager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return manager.primaryClip?.getItemAt(0)?.text.toString()
}

fun Context.clipboard(key: String, value: String?) {
    val manager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    manager.primaryClip = ClipData.newPlainText(key, value)
}
