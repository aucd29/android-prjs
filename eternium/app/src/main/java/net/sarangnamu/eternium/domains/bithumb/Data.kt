package net.sarangnamu.eternium.domains.bithumb

import java.io.Serializable

/**
 * Created by <a href="mailto:aucd29@hanwha.com">Burke Choi</a> on 2017. 9. 15.. <p/>
 */

data class Data (
    val timestamp : String,
    val payment_currency : String,
    val order_currency : String,
    val bids: List<Bids>
) : Serializable