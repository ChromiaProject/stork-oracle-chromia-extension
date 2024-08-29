package net.postchain.stork.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import net.postchain.common.wrap
import net.postchain.stork.integration.OraclePriceMessage
import net.postchain.stork.integration.StorkOraclePricesMapper
import net.postchain.stork.integration.gson.StorkGsonConfig
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Test
import java.security.Security

class CryptoUtilTest {

    init {
        // We add this provider so that we can get keccak-256 message digest instances
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    @Test
    fun `Verify hash functions and signatures with sample API data`() {
        val sampleJson = javaClass.getResource("/net/postchain/stork/sample_response.json").readText()
        val apiPriceMessage = StorkGsonConfig.mapper.fromJson(sampleJson, OraclePriceMessage::class.java)

        val btcPrice = apiPriceMessage.data["BTCUSD"]!!

        // Stork price
        val storkSignedPrice = btcPrice.storkSignedPrice
        assertThat(
                calculateStorkMessageHash(
                        storkSignedPrice.publicKey,
                        "BTCUSD",
                        storkSignedPrice.timestampedSignature.timestamp,
                        storkSignedPrice.price,
                        storkSignedPrice.publisherMerkleRoot,
                        storkSignedPrice.calculationAlg.checksum
                )
        ).isEqualTo(storkSignedPrice.timestampedSignature.msgHash)

        assertThat(
                verifyEVMSignature(
                        storkSignedPrice.timestampedSignature.msgHash,
                        storkSignedPrice.publicKey,
                        storkSignedPrice.timestampedSignature.signature.r,
                        storkSignedPrice.timestampedSignature.signature.s,
                        storkSignedPrice.timestampedSignature.signature.v
                )
        ).isTrue()

        // Publisher price
        val publisherPrice = btcPrice.signedPrices.first()
        assertThat(
                calculatePublisherMessageHash(
                        publisherPrice.publisherKey,
                        "BTCUSD",
                        publisherPrice.timestampedSignature.timestamp / StorkOraclePricesMapper.NANOS_IN_SECOND,
                        publisherPrice.price
                )
        ).isEqualTo(publisherPrice.timestampedSignature.msgHash)

        assertThat(
                verifyEVMSignature(
                        publisherPrice.timestampedSignature.msgHash,
                        publisherPrice.publisherKey,
                        publisherPrice.timestampedSignature.signature.r,
                        publisherPrice.timestampedSignature.signature.s,
                        publisherPrice.timestampedSignature.signature.v
                )
        ).isTrue()

        // Merkle root
        assertThat(
                computeMerkleRoot(btcPrice.signedPrices.map { it.timestampedSignature.msgHash.data }).wrap()
        ).isEqualTo(storkSignedPrice.publisherMerkleRoot)
    }
}
