package net.sarangnamu.apk_extractor

import android.app.Application
import android.content.Context
import android.graphics.Point
import android.view.WindowManager
import java.lang.ref.WeakReference

/**
 * Created by <a href="mailto:aucd29@hanwha.com">Burke Choi</a> on 2017. 12. 11.. <p/>
 */

class MainApp : Application() {
    companion object {
        private var weak: WeakReference<Context>? = null
        var screen: Point = Point()

        fun screenX() = screen.x
        fun screenY() = screen.y
        fun context(): Context = weak?.get()!!
    }

    override fun onCreate() {
        super.onCreate()

        weak = WeakReference(applicationContext)

        val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        manager.defaultDisplay.getSize(screen)
    }
}
