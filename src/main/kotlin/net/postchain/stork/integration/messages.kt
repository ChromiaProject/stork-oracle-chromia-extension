package net.postchain.stork.integration

import net.postchain.common.types.WrappedByteArray
import java.math.BigInteger

enum class MessageType {
    subscribe,
    unsubscribe,
    oracle_prices
}

data class SubscriptionRequestMessage(
        val type: MessageType = MessageType.subscribe,
        val data: List<String>
)

data class SubscriptionResponseMessage (
        val type: MessageType = MessageType.subscribe,
        val traceId: String,
        val data: SubscriptionResponseData
)

data class SubscriptionResponseData(
        val subscriptions: List<String>
)

data class OraclePriceMessage(
        val type: MessageType = MessageType.oracle_prices,
        val traceId: String,
        val data: Map<String, OraclePriceMessageData>
)

enum class SignatureType {
    evm // Deserializing this as an enum should prevent us from accepting other unknown signature types
}

data class OraclePriceMessageData(
        val timestamp: Long,
        val assetId: String,
        val signatureType: SignatureType,
        val trigger: String, // Could probably be an enum if we knew possible values
        val price: BigInteger, // Unclear what this top-level price is but is most likely duplicating value in stork price object
        val storkSignedPrice: StorkSignedPrice,
        val signedPrices: List<SignedPrice>
)

data class StorkSignedPrice(
        val publicKey: WrappedByteArray,
        val encodedAssetId: WrappedByteArray,
        val price: BigInteger,
        val timestampedSignature: TimestampedSignature,
        val publisherMerkleRoot: WrappedByteArray,
        val calculationAlg: CalculationAlgorithm
)

data class CalculationAlgorithm(
        val type: String,
        val version: String,
        val checksum: WrappedByteArray
)

data class SignedPrice(
        val publisherKey: WrappedByteArray,
        val externalAssetId: String,
        val signatureType: SignatureType,
        val price: BigInteger,
        val timestampedSignature: TimestampedSignature
)

data class TimestampedSignature(
        val signature: SignatureData,
        val timestamp: Long,
        val msgHash: WrappedByteArray // Publisher key + Asset ID (hex) + timestamp 32 bytes + price 32 bytes
)

data class SignatureData(
        val r: WrappedByteArray,
        val s: WrappedByteArray,
        val v: WrappedByteArray
)
