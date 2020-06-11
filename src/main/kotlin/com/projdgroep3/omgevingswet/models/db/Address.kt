package com.projdgroep3.omgevingswet.models.db

import UserOutputNoAddress
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object addresses : IntIdTable() {
    val city = varchar("city", 50)
    val street = varchar("street", 50)
    val housenumber = integer("housenumber")
    val housenumberaddition = varchar("housenumberaddition", 10)
    val postalcode = varchar("postalcode", 6)
}

class Address(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Address>(addresses)
    var city by addresses.city
    var street by addresses.street
    var housenumber by addresses.housenumber
    var housenumberaddition by addresses.housenumberaddition
    var postalcode by addresses.postalcode
}

data class AddressOutput(
        val id: Int,
        val city: String,
        val street: String,
        val houseNumber: Int,
        val houseNumberAddition: String?,
        val postalCode: String,
        val users: ArrayList<UserOutputNoAddress>
)

data class AddressCreateInput(
        val city: String,
        val street: String,
        val houseNumber: Int,
        val houseNumberAddition: String?,
        val postalCode: String
)