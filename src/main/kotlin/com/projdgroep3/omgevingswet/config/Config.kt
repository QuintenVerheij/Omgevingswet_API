package com.projdgroep3.omgevingswet.config

import com.natpryce.konfig.*
import java.io.File
import java.util.*

private class Config {

    fun getConfiguration(): Configuration {
        val properties = Properties()
        properties.load(this::class.java.classLoader.getResourceAsStream("system.properties"))

        return ConfigurationProperties.systemProperties() overriding
                EnvironmentVariables() overriding
                ConfigurationProperties.fromOptionalFile(File("/etc/extra/server.properties")) overriding
                ConfigurationProperties(properties = properties)
    }
}

val config = Config().getConfiguration()

object server: PropertyGroup() {
    val baseUrl by stringType

    val salt by stringType

    object db: PropertyGroup() {
        val host by stringType
        val port by intType
        val name by stringType
        val user by stringType
        val pwd by stringType
        val ssl by booleanType
    }

    object files: PropertyGroup() {
        val filedir by stringType
    }
}