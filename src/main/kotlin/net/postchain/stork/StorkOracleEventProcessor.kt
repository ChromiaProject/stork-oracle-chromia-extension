package net.postchain.stork

import mu.KLogging
import net.postchain.stork.integration.PriceUpdateHandler
import java.util.concurrent.ConcurrentHashMap

class StorkOracleEventProcessor(private val storkPriceValidator: StorkPriceValidator) : PriceUpdateHandler {

    companion object : KLogging()

    val assetPriceUpdates = ConcurrentHashMap<String, StorkOraclePrices>()

    override fun onPriceUpdate(storkOraclePrices: StorkOraclePrices) {
        if (storkPriceValidator.validateStorkOraclePrices(storkOraclePrices)) {
            assetPriceUpdates[storkOraclePrices.asset] = storkOraclePrices
        } else {
            logger.error("Received an invalid stork price update with data: ${storkOraclePrices}. Discarding update.")
        }
    }
}
