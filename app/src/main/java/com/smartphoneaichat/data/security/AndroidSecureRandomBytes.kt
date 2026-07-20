package com.smartphoneaichat.data.security

import java.security.SecureRandom

class AndroidSecureRandomBytes(
    private val secureRandom: SecureRandom = SecureRandom(),
) : RandomBytes {
    override fun next(size: Int): ByteArray = ByteArray(size).also(secureRandom::nextBytes)
}
