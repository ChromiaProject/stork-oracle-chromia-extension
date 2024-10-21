package net.postchain.stork

import mu.KLogging
import net.postchain.base.SpecialTransactionPosition
import net.postchain.common.BlockchainRid
import net.postchain.core.BlockEContext
import net.postchain.crypto.CryptoSystem
import net.postchain.gtv.GtvArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtx.GTXModule
import net.postchain.gtx.data.OpData
import net.postchain.gtx.special.GTXSpecialTxExtension
import java.time.Duration

class StorkOracleSpecialTxExtension : GTXSpecialTxExtension {

    companion object : KLogging() {
        const val OP_STORK_ORACLE_PRICES = "__stork_oracle_prices"
        const val OP_STORK_LATEST_UPDATE_TIMESTAMPS_QUERY = "stork_latest_update_timestamps"

        val NANOS_IN_MILLIS = 1_000_000
        val MAX_FUTURE_PRICE_TIME = Duration.ofMinutes(1).toNanos()
    }

    lateinit var storkPriceValidator: StorkPriceValidator
    lateinit var storkOracleEventProcessor: StorkOracleEventProcessor
    lateinit var module: GTXModule
    lateinit var latestPriceUpdateTimestamps: MutableMap<String, Long>

    override fun createSpecialOperations(position: SpecialTransactionPosition, bctx: BlockEContext): List<OpData> {
        if (!::latestPriceUpdateTimestamps.isInitialized) initializeLatestPriceUpdateTimestamps(bctx)

        return storkOracleEventProcessor.assetPriceUpdates
                .filter { (asset, priceUpdate) -> validatePriceUpdateTimestamp(asset, priceUpdate.storkPrice.timestampNanos) }
                .map { (_, priceUpdate) ->
                    OpData(OP_STORK_ORACLE_PRICES, arrayOf(GtvObjectMapper.toGtvArray(priceUpdate)))
                }
    }

    override fun getRelevantOps() = setOf(OP_STORK_ORACLE_PRICES)

    override fun init(module: GTXModule, chainID: Long, blockchainRID: BlockchainRid, cs: CryptoSystem) {
        this.module = module
    }

    override fun needsSpecialTransaction(position: SpecialTransactionPosition): Boolean = when (position) {
        SpecialTransactionPosition.Begin -> ::storkOracleEventProcessor.isInitialized
        SpecialTransactionPosition.End -> false
    }

    override fun validateSpecialOperations(position: SpecialTransactionPosition, bctx: BlockEContext, ops: List<OpData>): Boolean {
        if (!::latestPriceUpdateTimestamps.isInitialized) initializeLatestPriceUpdateTimestamps(bctx)

        val updatedAssets = mutableMapOf<String, Long>()

        for (op in ops) {
            val priceUpdate = StorkOraclePrices.fromGtvArray(op.args[0] as GtvArray)

            if (updatedAssets.contains(priceUpdate.asset)) {
                logger.warn("Only one update per asset is allowed per block, found multiple ops for asset: ${priceUpdate.asset}")
                return false
            }
            updatedAssets[priceUpdate.asset] = priceUpdate.storkPrice.timestampNanos

            if (!validatePriceUpdateTimestamp(priceUpdate.asset, priceUpdate.storkPrice.timestampNanos)) {
                logger.warn("Invalid timestamp for oracle price update: ${priceUpdate.storkPrice.timestampNanos}")
                return false
            }

            if (!storkPriceValidator.validateStorkOraclePrices(priceUpdate)) {
                logger.warn("Validation of stork price update failed")
                return false
            }
        }

        bctx.addAfterCommitHook {
            latestPriceUpdateTimestamps.putAll(updatedAssets)
        }

        return true
    }

    private fun validatePriceUpdateTimestamp(asset: String, timestampNanos: Long) =
            timestampNanos > (latestPriceUpdateTimestamps[asset] ?: 0) &&
                    timestampNanos - System.currentTimeMillis() * NANOS_IN_MILLIS <= MAX_FUTURE_PRICE_TIME

    private fun initializeLatestPriceUpdateTimestamps(bctx: BlockEContext) {
        latestPriceUpdateTimestamps = module.query(bctx, OP_STORK_LATEST_UPDATE_TIMESTAMPS_QUERY, gtv(mapOf()))
                .asDict().mapValues { it.value.asInteger() }.toMutableMap()
    }
}
