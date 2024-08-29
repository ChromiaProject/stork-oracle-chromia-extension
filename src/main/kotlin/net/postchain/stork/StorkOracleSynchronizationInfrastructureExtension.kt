package net.postchain.stork

import net.postchain.PostchainContext
import net.postchain.common.exception.UserMistake
import net.postchain.core.BlockchainProcess
import net.postchain.core.SynchronizationInfrastructureExtension
import net.postchain.gtv.mapper.toObject
import net.postchain.gtx.GTXModuleAware
import net.postchain.stork.config.StorkOracleBlockchainConfig
import net.postchain.stork.config.StorkOracleNodeConfig
import net.postchain.stork.integration.StorkOracleEventProcessor
import net.postchain.stork.integration.websocket.StorkWebSocketConnectionFactory

class StorkOracleSynchronizationInfrastructureExtension(
        private val postchainContext: PostchainContext
) : SynchronizationInfrastructureExtension {

    private var storkOracleEventProcessor: StorkOracleEventProcessor? = null

    override fun connectProcess(process: BlockchainProcess) {
        val cfg = process.blockchainEngine.getConfiguration()
        if (cfg is GTXModuleAware) {
            val storkExt = cfg.module.getSpecialTxExtensions().find { it is StorkOracleSpecialTxExtension }
            if (storkExt is StorkOracleSpecialTxExtension) {
                val storkNodeConfig = StorkOracleNodeConfig.fromAppConfig(postchainContext.appConfig)
                val storkBcConfig = cfg.rawConfig["stork"]?.toObject<StorkOracleBlockchainConfig>()
                        ?: throw UserMistake("Mandatory 'stork' configuration key is missing")

                storkExt.storkOracleEventProcessor = StorkOracleEventProcessor(
                        StorkWebSocketConnectionFactory(
                            storkNodeConfig.url,
                            storkNodeConfig.username,
                            storkNodeConfig.password
                        ),
                        storkBcConfig.assets
                )
            }
        }
    }

    override fun disconnectProcess(process: BlockchainProcess) {
        storkOracleEventProcessor?.shutdown()
    }

    override fun shutdown() {
        storkOracleEventProcessor?.shutdown()
    }
}
