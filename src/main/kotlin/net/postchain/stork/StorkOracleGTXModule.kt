package net.postchain.stork

import net.postchain.PostchainContext
import net.postchain.core.BlockchainConfiguration
import net.postchain.core.EContext
import net.postchain.gtx.PostchainContextAware
import net.postchain.gtx.SimpleGTXModule
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class StorkOracleGTXModule : SimpleGTXModule<Unit>(
        Unit, mapOf(), mapOf()
), PostchainContextAware {

    init {
        // We add this provider so that we can get keccak-256 message digest instances
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    private lateinit var postchainContext: PostchainContext

    override fun initializeDB(ctx: EContext) {}

    override fun initializeContext(configuration: BlockchainConfiguration, postchainContext: PostchainContext, ctx: EContext) {
        this.postchainContext = postchainContext
    }

    override fun getSpecialTxExtensions() = listOf(StorkOracleSpecialTxExtension(postchainContext))
}
