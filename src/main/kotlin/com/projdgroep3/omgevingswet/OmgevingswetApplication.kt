package com.projdgroep3.omgevingswet

import com.projdgroep3.omgevingswet.logic.Database.getDatabase
import com.projdgroep3.omgevingswet.models.db.addresses
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import useraddresses
import users

@SpringBootApplication
class OmgevingswetApplication

fun main(args: Array<String>) {
	runApplication<OmgevingswetApplication>(*args)

	transaction(getDatabase()) {SchemaUtils.create(users, addresses, useraddresses)}

}
