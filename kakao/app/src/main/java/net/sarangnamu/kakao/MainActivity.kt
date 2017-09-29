package net.sarangnamu.kakao

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.lang.Math.pow

// https://github.com/kakao/kakao.github.io/blob/5656b06546a63c6b33d9b5204d919b5e90b5a7bb/_posts/2017-09-27-kakao-blind-recruitment-round-1.md

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        q2()
//        q1()
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //
    ////////////////////////////////////////////////////////////////////////////////////

    fun q2() {
        Q2().apply {
            point = "1S2D*3T"
        }
    }

    class Q2 {
        var point: String = ""
        var pos: Int = 0
        var temp: Int = 0
        var result: Int = 0

        fun process() {
            var res = 0
            (0..2).forEach {
                res += cal()
            }

            result = res

            trace()
        }

        fun cal(): Int {
            var k = point.get(pos + 1)
            var value: Int
            var start = pos

            ++pos
            if (k.isDigit()) {
                value = point.substring(start, ++pos).toInt()
            } else {
                value = point.substring(start, pos).toInt()
            }

            if (value < 0) {
                value = 0
            }

            if (value > 10) {
                value = 10
            }

            val op = point.get(pos++)
            when (op) {
                'S' -> value = (pow(value.toDouble(), 1.0) * 2).toInt()
                'D' -> value = (pow(value.toDouble(), 2.0)).toInt()
                'T' -> value = (pow(value.toDouble(), 3.0)).toInt()
            }

            if (point.length >= pos) {
                // special
                val speical = point.get(pos++)
                when (op) {
                    '*' -> value *= 2
                    '#' -> value *= -1
                }
            }

            return value
        }

        fun trace() {
            Log.e("q2", "RES $result")
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //
    ////////////////////////////////////////////////////////////////////////////////////

    fun q1() {
        Q1().apply {
            n = 5
            arr1 = listOf(9, 20, 28, 18, 11)
            arr2 = listOf(30, 1, 21, 17, 28)
        }.process()

        Q1().apply {
            n = 6
            arr1 = listOf(46, 33, 33 ,22, 31, 50)
            arr2 = listOf(27 ,56, 19, 14, 14, 10)
        }.process()
    }

    class Q1 {
        var n: Int = 5
        var arr1: List<Int> = ArrayList()
        var arr2: List<Int> = ArrayList()
        val result: ArrayList<String> = ArrayList()

        fun process() {
            arr1 = filter(arr1)
            arr2 = filter(arr2)

            (0..n - 1).forEach { i ->
                val x   = arr1.get(i) or arr2.get(i)
                var op  = 1
                var str = ""

                (1..n).forEach {
                    if ((x and op) == op) {
                        str = "#" + str
                    } else {
                        str = " " + str
                    }

                    op = op shl 1
                }

                result.add(str)
            }

            trace()
        }

        private fun filter(data: List<Int>) : List<Int> {
            // https://www.quora.com/What-is-the-next-term-of-this-sequence-1-3-7-15-31-63-_
            val filter  = (pow(2.0, n.toDouble()) - 1).toInt()
            val newData = ArrayList<Int>()

            (0..n - 1).forEach { i -> newData += data.get(i) and filter }

            return newData
        }

        private fun trace() {
            result.forEach { res -> Log.e("Q1", res) }
            Log.e("Q1", "--")
        }
    }
}
