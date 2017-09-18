package net.sarangnamu.eternium

import android.util.Log
import net.sarangnamu.eternium.commons.BITHUMB_URL
import net.sarangnamu.eternium.commons.Net
import net.sarangnamu.eternium.domains.Bithumb
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun test_bithumb() {
//        val obj = Net.instance(BITHUMB_URL).create(Bithumb::class.java)
//
//        Log.d("test", "obj.status " + obj.status)
    }
}
