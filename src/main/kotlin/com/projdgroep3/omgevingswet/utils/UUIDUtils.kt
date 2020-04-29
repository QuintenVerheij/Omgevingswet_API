package com.projdgroep3.omgevingswet.utils

import java.util.*

object UUIDUtils {

    /**
     * Gets a random [UUID]
     */
    fun get(): String = UUID.randomUUID().toString()
}