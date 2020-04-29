package com.projdgroep3.omgevingswet.utils

import com.projdgroep3.omgevingswet.config.config
import com.projdgroep3.omgevingswet.config.server
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.apache.commons.codec.binary.Hex
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


object EncryptionUtils {
    private val serverSalt = config[server.salt]

    fun hashWithServerSalt(
            value: String
    ): String = hash(value, serverSalt)

    fun hashWithCryptoSaltAndServerSalt(
            value: String,
            cryptoSalt: String
    ): String = hash(hash(value, cryptoSalt), serverSalt)

    private fun hash(
            value: String,
            salt: String
    ):String {
        val valueArray: CharArray = value.toCharArray()
        val saltArray: ByteArray = salt.toByteArray()

        val iterations = 10000
        val keyLength = 512

        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        val spec = PBEKeySpec(valueArray, saltArray, iterations, keyLength)
        val key = skf.generateSecret(spec)
        return Hex.encodeHexString(key.encoded)
    }

    fun getSalt(): String = hash(serverSalt, UUIDUtils.get())
}