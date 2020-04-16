package com.projdgroep3.omgevingswet.service.db

import org.jetbrains.exposed.sql.SizedIterable

abstract class DatabaseService<T> {
    abstract fun readAll(): List<T>
}