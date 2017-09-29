package net.sarangnamu.kakao

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.lang.Math.pow
import java.util.*

// https://github.com/kakao/kakao.github.io/blob/5656b06546a63c6b33d9b5204d919b5e90b5a7bb/_posts/2017-09-27-kakao-blind-recruitment-round-1.md

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        q4()
//        q3()
//        q2()
//        q1()
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // 오전 9시부터 총 n회(0 ~ 10) t분(0 ~ 60) 간격으로 역에 도착
    //
    // 최대로 탑승할 수 있는 승객 수 m (0 ~ 45)
    // timetable 의 최소 길이는 1 최대는 2000
    // 도착 시각은 00:01 에서 23:59 사이
    //
    // 사무실에 갈 수 있는 제일 늦은 도착 시작 출력
    //
    ////////////////////////////////////////////////////////////////////////////////////

    fun q4() {
//        Q4().process()
        Q4().apply {
            n = 2
            t = 10
            m = 2
            timetable = arrayListOf("09:10", "09:09", "08:00")
        }.process()
    }

    data class Q4Time (
        val date: String,
        val timestamp: Long
    )

    class Q4 {
        val YEAR = 2017
        val MONTH = 9
        val DAY = 29
        val HOUR = 9
        val MIN = 0
        val SEC = 0

        var n = 1   // 회수
        var t = 1   // 간격 (분)
        var m = 5   // 탑승 인원
        var timetable = arrayListOf("08:00", "08:01", "08:02", "08:03")

        fun parseCrewTime(): ArrayList<ArrayList<Q4Time>> {
            val crewTimeList = ArrayList<ArrayList<Q4Time>>()

            timetable.sort()
            val it = timetable.listIterator()
            while (it.hasNext()) {
                var i = 0
                val list = ArrayList<Q4Time>()

                while (i++ < m) {
                    if (!it.hasNext()) {
                        break
                    }

                    val time      = it.next()
                    val converted = convertCrewTime(time)

                    Log.i("q4", "CREW [$time] = $converted")
                    list.add(Q4Time(time, converted))
                }

                crewTimeList.add(list)
            }

            return crewTimeList
        }

        fun convertCrewTime(time: String): Long {
            val tm   = time.split(":")
            val hour = tm.get(0).toInt()
            val min  = tm.get(1).toInt()

            return Date(YEAR, MONTH, DAY, hour, min, SEC).time
        }

        fun process() {
            var busStartTime = Date(YEAR, MONTH, DAY, HOUR, MIN, SEC).time
            Log.i("q4", "BUS START = $busStartTime")

            val crewTimeList = parseCrewTime()
            // arrayListOf("09:10", "09:09", "08:00")
            var i = 0
            var lastTime:Long = 0
            while (i < n) {
                var it = crewTimeList.get(i).listIterator()
                while (it.hasNext()) {
                    val q4time = it.next()
                    if (busStartTime <= q4time.timestamp) {
                        Log.i("q4", "TAKE ${q4time.date}")
                        lastTime = q4time.timestamp - 60
                        break;
                    }
                }

                if (n > 1) {
                    // next bus time
                    busStartTime += 60 * t
                }

                ++i
            }

            if (lastTime > busStartTime) {
                lastTime = busStartTime
            }

            trace(busStartTime)
        }

        fun trace(time: Long) {
            val date = Date(time)
            Log.e("q4", "RESULT: " + String.format("%02d:%02d", date.hours, date.minutes))
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    // https://ko.wikipedia.org/wiki/LRU
    // LRU(Least Recently Used)는 교체 전략 중의 하나로 사용한지 가장 오래된 항목부터 버리는 방식이다.
    // - cache (0 ~ 30)
    // - city name
    //   최대 100,000, eng only, maxlength 20,
    //   // 특수 문자가 들어온 경우 예외처리를 어떻게 할지에 대한 내용이 없네?
    //
    ////////////////////////////////////////////////////////////////////////////////////

    fun q3() {
        Q3().apply { cities = arrayListOf("Jeju", "Pangyo", "Seoul", "NewYork", "LA", "Jeju", "Pangyo", "Seoul", "NewYork", "LA") }.process()
        Q3().apply { cities = arrayListOf("Jeju", "Pangyo", "Seoul", "Jeju", "Pangyo", "Seoul", "Jeju", "Pangyo", "Seoul") }.process()
        Q3().apply {
            cacheSize(2)
            cities = arrayListOf("Jeju", "Pangyo", "Seoul", "NewYork", "LA", "SanFrancisco", "Seoul", "Rome", "Paris", "Jeju", "NewYork", "Rome")
        }.process()

        Q3().apply {
            cacheSize(5)
            cities = arrayListOf("Jeju", "Pangyo", "Seoul", "NewYork", "LA", "SanFrancisco", "Seoul", "Rome", "Paris", "Jeju", "NewYork", "Rome")
        }.process()

        Q3().apply {
            cacheSize(2)
            cities = arrayListOf("Jeju", "Pangyo", "NewYork", "newyork")
        }.process()

        Q3().apply {
            cacheSize(0)
            cities = arrayListOf("Jeju", "Pangyo", "Seoul", "NewYork", "LA")
        }.process()
    }

    class Q3 {
        private var cacheSize = 3
        var cities: ArrayList<String> = ArrayList()
        var runningTime = 0

        fun cacheSize(cacheSize: Int) {
            if (cacheSize < 0) {
                this.cacheSize = 0
            } else if (cacheSize > 30) {
                this.cacheSize = 30
            } else {
                this.cacheSize = cacheSize
            }
        }

        fun process() {
            replaceCities()
            val cache = ArrayList<String>()

            cities.forEach {
                if (!cache.contains(it)) {
                    runningTime += 5

                    if (cacheSize > 0) {
                        if (cache.size > cacheSize - 1) {
                            cache.removeAt(0)
                        }

                        cache.add(it)
                    }
                } else {
                    ++runningTime
                }
            }

            trace()
        }

        fun replaceCities() {
            val it = cities.listIterator()
            while (it.hasNext()) {
                val city = it.next().toLowerCase()
                it.set(city)
            }
        }

        fun trace() {
            Log.e("q3", "RUNNING TIME: $runningTime")
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //
    ////////////////////////////////////////////////////////////////////////////////////

    fun q2() {
        Q2().apply { dartPoint = "1S2D*3T" }.process()
        Q2().apply { dartPoint = "1D2S#10S" }.process()
        Q2().apply { dartPoint = "1D2S0T" }.process()
        Q2().apply { dartPoint = "1S*2T*3S" }.process()
        Q2().apply { dartPoint = "1D#2S*3S" }.process()
        Q2().apply { dartPoint = "1T2D3D#" }.process()
        Q2().apply { dartPoint = "1D2S3T*" }.process()
    }

    class Q2Result {
        var num: Int = 0
        var operator: Char = ' '
        var option: Char = ' '
        var nextRes: Q2Result? = null

        fun number(value: Int) {
            if (value < 0) {
                num = 0
            } else if (value > 10) {
                num = 10
            } else {
                num = value
            }
        }

        fun option(opt: Char) {
            option = opt
        }

        fun cal(): Int {
            var powVal = 1.0    // S
            when (operator) {
                'D' -> powVal = 2.0
                'T' -> powVal = 3.0
            }

            num = pow(num.toDouble(), powVal).toInt()

            when (option) {
                '*' -> num *= 2
                '#' -> num *= -1
            }

            nextRes?.let {
                if ('*' == it.option) {
                    num *= 2
                }
            }

            return num
        }
    }

    class Q2 {
        var dartPoint: String = ""
        var index: Int = 0
        var totalResult: Int = 0

        fun process() {
            val parseList = ArrayList<Q2Result>()
            (0..2).forEach {
                val result = parse()
                if (parseList.size > 0) {
                    parseList.last().nextRes = result
                }

                parseList.add(result)
            }

            parseList.forEach {
                totalResult += it.cal()
            }

            trace()
        }

        fun parse(): Q2Result {
            var value: Int
            val start = index
            val result = Q2Result()

            ++index
            if (dartPoint.get(index).isDigit()) {
                value = dartPoint.substring(start, ++index).toInt()
            } else {
                value = dartPoint.substring(start, index).toInt()
            }

            result.number(value)
            result.operator = dartPoint.get(index)

            try {
                result.option(dartPoint.get(++index))
                if (result.option == '*' || result.option == '#') {
                    ++index
                }
            } catch (ignore: Exception) {

            }

            return result
        }

        fun trace() {
            Log.e("[q2]", "RES ($dartPoint) = $totalResult")
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
            arr1 = arrayListOf(9, 20, 28, 18, 11)
            arr2 = arrayListOf(30, 1, 21, 17, 28)
        }.process()

        Q1().apply {
            n = 6
            arr1 = arrayListOf(46, 33, 33 ,22, 31, 50)
            arr2 = arrayListOf(27 ,56, 19, 14, 14, 10)
        }.process()
    }

    class Q1 {
        var n: Int = 5
        var arr1: ArrayList<Int> = ArrayList()
        var arr2: ArrayList<Int> = ArrayList()
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

        private fun filter(data: ArrayList<Int>) : ArrayList<Int> {
            // https://www.quora.com/What-is-the-next-term-of-this-sequence-1-3-7-15-31-63-_
            val filter  = (pow(2.0, n.toDouble()) - 1).toInt()
            val it = data.listIterator()
            while (it.hasNext()) {
                it.set(it.next() and filter)
            }

            return data
        }

        private fun trace() {
            result.forEach { res -> Log.e("[q1]", res) }
            Log.e("[q1]", "--")
        }
    }
}
