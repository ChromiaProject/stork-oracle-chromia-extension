package net.postchain.stork

import mu.KLogging
import net.postchain.common.data.Hash
import net.postchain.common.types.WrappedByteArray
import net.postchain.common.wrap
import net.postchain.stork.util.calculatePublisherMessageHash
import net.postchain.stork.util.calculateStorkMessageHash
import net.postchain.stork.util.computeMerkleRoot
import net.postchain.stork.util.verifyEVMSignature

class StorkPriceValidator(private val storkPubKey: WrappedByteArray, private val publisherPubKeys: Set<WrappedByteArray>) {

    companion object : KLogging()

    fun validateStorkOraclePrices(storkOraclePrices: StorkOraclePrices): Boolean {
        return validateStorkPrice(storkOraclePrices.asset, storkOraclePrices.storkPrice)
                && validatePublisherPrices(storkOraclePrices.asset, storkOraclePrices.publisherPrices, storkOraclePrices.storkPrice.merkleRoot)
    }

    private fun validateStorkPrice(asset: String, storkPrice: StorkPrice): Boolean {
        try {
            val storkMessageHash = calculateStorkMessageHash(
                    storkPrice.signature.signer,
                    asset,
                    storkPrice.timestampNanos,
                    storkPrice.price,
                    storkPrice.merkleRoot,
                    storkPrice.checksum
            )
            if (storkPrice.signature.signer != storkPubKey) {
                logger.warn("Unexpected Stork price public key: ${storkPrice.signature.signer}, expected: $storkPubKey")
                return false
            }
            return if (verifyEVMSignature(
                            storkMessageHash,
                            storkPrice.signature.signer,
                            storkPrice.signature.r,
                            storkPrice.signature.s,
                            storkPrice.signature.v
                    )) {
                true
            } else {
                logger.warn("Invalid stork price signature")
                false
            }

        } catch (e: Exception) {
            logger.warn("Unable to verify stork price: ${e.message}")
            return false
        }
    }

    private fun validatePublisherPrices(asset: String, publisherPrices: List<PublisherPrice>, merkleRoot: WrappedByteArray): Boolean {
        try {
            val messageHashes = mutableListOf<Hash>()
            for (publisherPrice in publisherPrices) {
                val publisherMessageHash = calculatePublisherMessageHash(
                        publisherPrice.signature.signer,
                        asset,
                        publisherPrice.timestampSeconds,
                        publisherPrice.price
                )
                if (!publisherPubKeys.contains(publisherPrice.signature.signer)) {
                    logger.warn("Unknown publisher public key: ${publisherPrice.signature.signer}")
                    return false
                }
                if (!verifyEVMSignature(
                                publisherMessageHash,
                                publisherPrice.signature.signer,
                                publisherPrice.signature.r,
                                publisherPrice.signature.s,
                                publisherPrice.signature.v
                        )) {
                    logger.warn("Invalid publisher price signature for publisher: ${publisherPrice.signature.signer}")
                    return false
                }

                messageHashes.add(publisherMessageHash.data)
            }

            val computedMerkleRoot = computeMerkleRoot(messageHashes).wrap()
            return if (computedMerkleRoot == merkleRoot) {
                true
            } else {
                logger.warn("Stork price merkle root mismatch, expected $merkleRoot was $computedMerkleRoot")
                false
            }
        } catch (e: Exception) {
            logger.warn("Unable to verify publisher prices, reason: ${e.message}")
            return false
        }
    }
}
