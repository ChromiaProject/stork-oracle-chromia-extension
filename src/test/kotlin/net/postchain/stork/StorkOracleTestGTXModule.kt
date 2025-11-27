package net.postchain.stork

import net.postchain.core.EContext
import net.postchain.core.TxEContext
import net.postchain.gtv.Gtv
import net.postchain.gtv.GtvArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.gtv.GtvNull
import net.postchain.gtv.mapper.GtvObjectMapper
import net.postchain.gtx.GTXOperation
import net.postchain.gtx.SimpleGTXModule
import net.postchain.gtx.data.ExtOpData
import net.postchain.stork.StorkOracleSpecialTxExtension.Companion.OP_STORK_LATEST_UPDATE_TIMESTAMPS_QUERY
import net.postchain.stork.StorkOracleSpecialTxExtension.Companion.OP_STORK_ORACLE_PRICES
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

const val OP_STORK_TEST_LATEST_UPDATE_QUERY = "stork_test_latest_update"

class StorkOracleTestGTXModule : SimpleGTXModule<MutableMap<String, StorkOraclePrices>>(
        mutableMapOf(),
        mapOf(OP_STORK_ORACLE_PRICES to { conf, extOpData -> OraclePriceUpdateOp(conf, extOpData) }),
        mapOf(
                OP_STORK_LATEST_UPDATE_TIMESTAMPS_QUERY to { conf: Map<String, StorkOraclePrices>, _: EContext, _: Gtv ->
                    gtv(conf.mapValues { gtv(it.value.storkPrice.timestampNanos) })
                },
                OP_STORK_TEST_LATEST_UPDATE_QUERY to { conf: Map<String, StorkOraclePrices>, _: EContext, args: Gtv ->
                    conf[args.asString()]?.let { GtvObjectMapper.toGtvArray(it) } ?: GtvNull
                }
        )
) {

    init {
        // We add this provider so that we can get keccak-256 message digest instances
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    override fun initializeDB(ctx: EContext) {}

    override fun getSpecialTxExtensions() = listOf(StorkOracleSpecialTxExtension())
}

class OraclePriceUpdateOp(private val latestUpdateTimestamps: MutableMap<String, StorkOraclePrices>, data: ExtOpData) : GTXOperation(data) {

    override fun checkCorrectness() {}

    override fun apply(ctx: TxEContext): Boolean {
        val priceUpdate = StorkOraclePrices.fromGtvArray(data.args[0] as GtvArray)

        ctx.addAfterAppendHook {
            latestUpdateTimestamps[priceUpdate.asset] = priceUpdate
        }

        return true
    }
}
