package net.postchain.stork.config

import net.postchain.common.types.WrappedByteArray
import net.postchain.gtv.Gtv
import net.postchain.gtv.mapper.Name
import net.postchain.gtv.mapper.Nullable
import net.postchain.gtv.mapper.RawGtv

data class StorkOracleBlockchainConfig(
        @param:RawGtv
        val rawGtv: Gtv,
        @param:Name("assets")
        val assets: List<String>,
        @param:Name("publisher_pubkeys")
        @param:Nullable
        val publisherPubKeys: Set<WrappedByteArray>?,
        @param:Name("stork_pubkey")
        @param:Nullable
        val storkPubKey: WrappedByteArray?
) {
        /**
         * @return a hardcoded list of public keys for now since there is no other source for publisher identities.
         * Possible to override return value via blockchain configuration.
         */
        fun getConfiguredPublisherPubKeys() = publisherPubKeys ?: setOf(
                WrappedByteArray.fromHex("5c946686b0302be54d85394015a9f9fa0952984e"),
                WrappedByteArray.fromHex("280a9cf6978d0e0c614f71c0a644584457538632"),
                WrappedByteArray.fromHex("a3c28d4e939ce2927d3b29b7bf53d3aeaab09350"),
                WrappedByteArray.fromHex("b91c675e0c0ecfd4c16f97b110376c3c224061d8"),
                WrappedByteArray.fromHex("f024a9aa110798e5cd0d698fba6523113eaa7fb2"),
                WrappedByteArray.fromHex("51aa9e9c781f85a2c0636a835eb80114c4553098"),
                WrappedByteArray.fromHex("822e6e9a6a5e99fd791538b4ec545e5508b2f5a3"),
                WrappedByteArray.fromHex("bcf95aa3eaf6012136cbad531c8d187ea91f203b"),
                WrappedByteArray.fromHex("16eb47a6bbdf1e1d1e9ac23e6f473f1bcae519c0"),
                WrappedByteArray.fromHex("0cbc88384406c2c2e5e141505d3a3b1c7c5b69cf"),
                WrappedByteArray.fromHex("52ec7c4b45b4498fefe8db8cda6778b97576bae0"),
                WrappedByteArray.fromHex("f2e72022eb19352f488526e835c4b17248aa6c03"),
        )

        /**
         * @return hardcoded Stork public key for now since there is no other source.
         * Possible to override return value via blockchain configuration.
         */
        fun getConfiguredStorkPubKey() = storkPubKey
                ?: WrappedByteArray.fromHex("0a803F9b1CCe32e2773e0d2e98b37E0775cA5d44")
}
