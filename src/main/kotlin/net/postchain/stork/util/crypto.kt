package net.postchain.stork.util

import net.postchain.base.snapshot.SimpleDigestSystem
import net.postchain.common.data.EMPTY_HASH
import net.postchain.common.data.Hash
import net.postchain.common.data.KECCAK256
import net.postchain.common.exception.ProgrammerMistake
import net.postchain.common.hexStringToByteArray
import net.postchain.common.types.WrappedByteArray
import net.postchain.common.wrap
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import java.math.BigInteger
import java.security.MessageDigest

fun getKeccak() = SimpleDigestSystem(MessageDigest.getInstance(KECCAK256))

// See: https://github.com/Stork-Oracle/stork-external/blob/main/contracts/evm/contracts/StorkVerify.sol#L36
fun calculatePublisherMessageHash(
        publisherKey: WrappedByteArray,
        asset: String,
        timestampEpochSeconds: Long,
        price: BigInteger
): WrappedByteArray =
        getKeccak().digest(
                publisherKey.data +
                        asset.toByteArray() +
                        padTo32ByteArray(timestampEpochSeconds.toBigInteger().toByteArray()) +
                        padTo32ByteArray(price.toByteArray())
        ).wrap()

// See: https://github.com/Stork-Oracle/stork-external/blob/main/contracts/evm/contracts/StorkVerify.sol#L15
fun calculateStorkMessageHash(
        storkPublicKey: WrappedByteArray,
        asset: String,
        timestampEpochNanos: Long,
        price: BigInteger,
        merkleRoot: WrappedByteArray,
        algorithmChecksum: WrappedByteArray
): WrappedByteArray =
        getKeccak().digest(
                storkPublicKey.data +
                        getKeccak().digest(asset.toByteArray()) +
                        padTo32ByteArray(timestampEpochNanos.toBigInteger().toByteArray()) +
                        padTo32ByteArray(price.toByteArray()) +
                        merkleRoot.data +
                        algorithmChecksum.data
        ).wrap()

fun verifyEVMSignature(
        digest: WrappedByteArray,
        signer: WrappedByteArray,
        r: WrappedByteArray,
        s: WrappedByteArray,
        v: WrappedByteArray
): Boolean {
    val pubKey = Sign.signedPrefixedMessageToKey(digest.data, Sign.SignatureData(v.data, r.data, s.data))
    val address = Keys.getAddress(pubKey)

    return signer == address.hexStringToByteArray().wrap()
}

fun computeMerkleRoot(hashes: List<Hash>): Hash {
    if (hashes.isEmpty()) throw ProgrammerMistake("Can't compute merkle root without hashes")

    var leaves = hashes
    while (leaves.size > 1) {
        leaves = if (hashes.size % 2 != 0) leaves + EMPTY_HASH else leaves

        val nextLevel = mutableListOf<Hash>()
        for (i in 0 until leaves.size step 2) {
            nextLevel.add(getKeccak().hash(leaves[i], leaves[i + 1]))
        }
        leaves = nextLevel
    }

    return leaves[0]
}

private fun padTo32ByteArray(value: ByteArray): ByteArray {
    val result = ByteArray(32)
    value.copyInto(result, 32 - value.size)
    return result
}
