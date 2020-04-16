package com.projdgroep3.omgevingswet.logic

import com.projdgroep3.omgevingswet.config.config
import com.projdgroep3.omgevingswet.config.server

import org.jetbrains.exposed.*
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.jdbc.*
import org.jetbrains.exposed.sql.Database

/**
 * Create database connection and handle transactions
 *
 * @Author Quinten
 */
object Database {

    private var db:Database = createDatabase()

    fun getDatabase():Database = db

    private fun createDatabase():Database {
        // Check if JDBC Driver exists
        Class.forName("org.postgresql.Driver")

        val host = config[server.db.host]
        val port = config[server.db.port]
        var databaseName = config[server.db.name]
        val user = config[server.db.user]
        val password = config[server.db.pwd]
        val useSSL = config[server.db.ssl]

        val db by lazy {Database.connect("jdbc:postgresql://$host:$port/$databaseName", driver = "org.postgresql.Driver",
                user = user, password = password)}

        return db
    }
}