package net.postchain.stork.config

import net.postchain.gtv.Gtv
import net.postchain.gtv.mapper.Name
import net.postchain.gtv.mapper.RawGtv

data class StorkOracleBlockchainConfig(
        @RawGtv
        val rawGtv: Gtv,
        @Name("assets")
        val assets: List<String>
)
