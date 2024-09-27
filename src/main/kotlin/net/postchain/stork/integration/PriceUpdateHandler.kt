package net.postchain.stork.integration

import net.postchain.stork.StorkOraclePrices

fun interface PriceUpdateHandler {
    fun onPriceUpdate(storkOraclePrices: StorkOraclePrices)
}
