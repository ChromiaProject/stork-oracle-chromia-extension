package net.postchain.stork

import net.postchain.common.types.WrappedByteArray
import net.postchain.common.wrap
import net.postchain.gtv.GtvArray
import java.math.BigInteger

data class StorkOraclePrices(
        val asset: String,
        val storkPrice: StorkPrice,
        val publisherPrices: List<PublisherPrice>
) {
    companion object {
        fun fromGtvArray(array: GtvArray) = StorkOraclePrices(
                array[0].asString(),
                StorkPrice.fromGtvArray(array[1] as GtvArray),
                array[2].asArray().map { PublisherPrice.fromGtvArray(it as GtvArray) }
        )
    }
}

data class StorkPrice(
        val price: BigInteger,
        val signature: Signature,
        val timestampNanos: Long,
        val merkleRoot: WrappedByteArray,
        val type: String,
        val version: String,
        val checksum: WrappedByteArray
) {
    companion object {
        fun fromGtvArray(array: GtvArray) = StorkPrice(
                array[0].asBigInteger(),
                Signature.fromGtvArray(array[1] as GtvArray),
                array[2].asInteger(),
                array[3].asByteArray().wrap(),
                array[4].asString(),
                array[5].asString(),
                array[6].asByteArray().wrap()
        )
    }
}

data class PublisherPrice(
        val price: BigInteger,
        val signature: Signature,
        val timestampSeconds: Long
) {
    companion object {
        fun fromGtvArray(array: GtvArray) = PublisherPrice(
                array[0].asBigInteger(),
                Signature.fromGtvArray(array[1] as GtvArray),
                array[2].asInteger()
        )
    }
}

data class Signature(
        val signer: WrappedByteArray,
        val r: WrappedByteArray,
        val s: WrappedByteArray,
        val v: WrappedByteArray
) {
    companion object {
        fun fromGtvArray(array: GtvArray) = Signature(
                array[0].asByteArray().wrap(),
                array[1].asByteArray().wrap(),
                array[2].asByteArray().wrap(),
                array[3].asByteArray().wrap(),
        )
    }
}
