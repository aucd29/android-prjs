package net.sarangnamu.eternium.domains.bithumb

import java.io.Serializable

/**
 * Created by <a href="mailto:aucd29@hanwha.com">Burke Choi</a> on 2017. 9. 18.. <p/>
 */

data class Bids (
    val quantity: String,
    val price: String
) : Serializable