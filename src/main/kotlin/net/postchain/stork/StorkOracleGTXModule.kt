package net.postchain.stork

import net.postchain.core.EContext
import net.postchain.gtx.SimpleGTXModule
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class StorkOracleGTXModule : SimpleGTXModule<Unit>(
        Unit, mapOf(), mapOf()
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
