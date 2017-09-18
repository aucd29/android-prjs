package net.sarangnamu.eternium.apis

import io.reactivex.Observable
import net.sarangnamu.eternium.domains.Bithumb
import retrofit2.http.GET

/**
 * Created by <a href="mailto:aucd29@hanwha.com">Burke Choi</a> on 2017. 9. 18.. <p/>
 */

interface BithumbOrderbook {
    @GET("/public/orderbook")
    fun orderbook(): Observable<Bithumb>
}