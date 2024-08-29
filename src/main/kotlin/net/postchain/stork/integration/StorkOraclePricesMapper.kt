package net.postchain.stork.integration

import net.postchain.stork.PublisherPrice
import net.postchain.stork.Signature
import net.postchain.stork.StorkOraclePrices
import net.postchain.stork.StorkPrice

object StorkOraclePricesMapper {

    const val NANOS_IN_SECOND = 1_000_000_000

    fun mapMessageToAssetOraclePrices(message: OraclePriceMessage): List<StorkOraclePrices> =
            message.data.map { (asset, priceMessageData) ->
                val storkSignedPrice = priceMessageData.storkSignedPrice
                StorkOraclePrices(
                        asset,
                        StorkPrice(
                                storkSignedPrice.price,
                                Signature(
                                        storkSignedPrice.publicKey,
                                        storkSignedPrice.timestampedSignature.signature.r,
                                        storkSignedPrice.timestampedSignature.signature.s,
                                        storkSignedPrice.timestampedSignature.signature.v,
                                ),
                                storkSignedPrice.timestampedSignature.timestamp,
                                storkSignedPrice.publisherMerkleRoot,
                                storkSignedPrice.calculationAlg.type,
                                storkSignedPrice.calculationAlg.version,
                                storkSignedPrice.calculationAlg.checksum,
                        ),
                        priceMessageData.signedPrices.map {
                            PublisherPrice(
                                    it.price,
                                    Signature(
                                            it.publisherKey,
                                            it.timestampedSignature.signature.r,
                                            it.timestampedSignature.signature.s,
                                            it.timestampedSignature.signature.v,
                                    ),
                                    it.timestampedSignature.timestamp / NANOS_IN_SECOND,
                            )
                        }
                )
            }
}
