package com.rakuten.tech.mobile.remoteconfig

import android.util.Base64
import java.io.InputStream
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec

internal class SignatureVerifier(private val cache: PublicKeyCache) {

    fun verifyCached(keyId: String, message: InputStream, signature: String) : Boolean {
        val publicKey = cache[keyId] ?: return false

        return verify(publicKey, message, signature)
    }

    fun verifyFetched(keyId: String, message: InputStream, signature: String) : Boolean {
        val publicKey = cache[keyId] ?: cache.fetch(keyId)

        return verify(publicKey, message, signature)
    }
}

private fun verify(publicKey: String, message: InputStream, signature: String): Boolean {
    return Signature.getInstance("SHA256withECDSA")
        .apply {
            initVerify(rawToEncodedECPublicKey(publicKey))

            val buffer = ByteArray(16 * 1024)
            var read = message.read(buffer)
            while (read != -1) {
                update(buffer, 0, read)

                read = message.read(buffer)
            }
        }
        .verify(Base64.decode(signature, Base64.DEFAULT))
}

private fun rawToEncodedECPublicKey(key: String) : ECPublicKey {
    val parameters = ecParameterSpecForCurve("secp256r1")
    val keySizeBytes = parameters.order.bitLength() / java.lang.Byte.SIZE
    val pubKey = Base64.decode(key, Base64.DEFAULT)

    var offset = 1 // First Byte represents compressed/uncompressed status - we're expecting it to always be uncompressed (04)
    val x = BigInteger(1, pubKey.copyOfRange(offset, offset + keySizeBytes))

    offset += keySizeBytes
    val y = BigInteger(1, pubKey.copyOfRange(offset, offset + keySizeBytes))

    val keySpec = ECPublicKeySpec(ECPoint(x, y), parameters)
    val keyFactory = KeyFactory.getInstance("EC")
    return keyFactory
        .generatePublic(keySpec) as ECPublicKey
}

private fun ecParameterSpecForCurve(curveName: String) : ECParameterSpec {
    val kpg = KeyPairGenerator.getInstance("EC")
    kpg.initialize(ECGenParameterSpec(curveName))

    return (kpg.generateKeyPair().public as ECPublicKey).params
}
