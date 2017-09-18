package net.sarangnamu.eternium.domains

import net.sarangnamu.eternium.domains.bithumb.Data
import java.io.Serializable

/**
 * Created by <a href="mailto:aucd29@hanwha.com">Burke Choi</a> on 2017. 9. 15.. <p/>
 */

// https://api.bithumb.com/public/orderbook
/*
{
	"status": "0000",
	"data": {
		"timestamp": "1505464977208",
		"payment_currency": "KRW",
		"order_currency": "BTC",
		"bids": [{
			"quantity": "0.94606761",
			"price": "3511000"
		}]
	}
}
 */

data class Bithumb (
    val status : String,
    val data : Data
) : Serializable