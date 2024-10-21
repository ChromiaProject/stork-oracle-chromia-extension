package net.postchain.stork

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import net.postchain.common.types.WrappedByteArray
import net.postchain.stork.integration.OraclePriceMessage
import net.postchain.stork.integration.StorkOraclePricesMapper
import net.postchain.stork.integration.gson.StorkGsonConfig
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Test
import java.security.Security

class StorkPriceValidatorTest {
    private val sampleJson = javaClass.getResource("/net/postchain/stork/sample_response.json").readText()
    private val priceUpdate = StorkOraclePricesMapper.mapMessageToAssetOraclePrices(
            StorkGsonConfig.mapper.fromJson(sampleJson, OraclePriceMessage::class.java)
    )

    init {
        // We add this provider so that we can get keccak-256 message digest instances
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    @Test
    fun `Valid price update`() {
        val validator = StorkPriceValidator(
                WrappedByteArray.fromHex("0a803F9b1CCe32e2773e0d2e98b37E0775cA5d44"),
                setOf(
                        WrappedByteArray.fromHex("51aa9e9c781f85a2c0636a835eb80114c4553098"),
                        WrappedByteArray.fromHex("a3c28d4e939ce2927d3b29b7bf53d3aeaab09350"),
                        WrappedByteArray.fromHex("b91c675e0c0ecfd4c16f97b110376c3c224061d8"),
                        WrappedByteArray.fromHex("f024a9aa110798e5cd0d698fba6523113eaa7fb2"),
                )
        )

        assertThat(validator.validateStorkOraclePrices(priceUpdate.first())).isTrue()
    }

    @Test
    fun `Invalid Stork public key`() {
        val validator = StorkPriceValidator(
                WrappedByteArray.fromHex("51aa9e9c781f85a2c0636a835eb80114c4553098"),
                setOf(
                        WrappedByteArray.fromHex("51aa9e9c781f85a2c0636a835eb80114c4553098"),
                        WrappedByteArray.fromHex("a3c28d4e939ce2927d3b29b7bf53d3aeaab09350"),
                        WrappedByteArray.fromHex("b91c675e0c0ecfd4c16f97b110376c3c224061d8"),
                        WrappedByteArray.fromHex("f024a9aa110798e5cd0d698fba6523113eaa7fb2"),
                )
        )

        assertThat(validator.validateStorkOraclePrices(priceUpdate.first())).isFalse()
    }

    @Test
    fun `Unknown publisher key`() {
        val validator = StorkPriceValidator(
                WrappedByteArray.fromHex("0a803F9b1CCe32e2773e0d2e98b37E0775cA5d44"),
                setOf(
                        WrappedByteArray.fromHex("51aa9e9c781f85a2c0636a835eb80114c4553098"),
                        WrappedByteArray.fromHex("a3c28d4e939ce2927d3b29b7bf53d3aeaab09350"),
                        WrappedByteArray.fromHex("b91c675e0c0ecfd4c16f97b110376c3c224061d8"),
                )
        )

        assertThat(validator.validateStorkOraclePrices(priceUpdate.first())).isFalse()
    }
}
