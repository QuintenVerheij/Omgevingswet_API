package com.projdgroep3.omgevingswet.service.db

import UserOutputNoAddress
import com.projdgroep3.omgevingswet.logic.Database.getDatabase
import com.projdgroep3.omgevingswet.models.db.Address
import com.projdgroep3.omgevingswet.models.db.AddressCreateInput
import com.projdgroep3.omgevingswet.models.db.AddressOutput
import com.projdgroep3.omgevingswet.models.db.addresses
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object AddressService : DatabaseService<AddressOutput>() {
    //override fun readAll(): List<Address> = transaction(getDatabase()) { Address.all().toList() }

    override fun readAll(): List<AddressOutput> {
        var a = ArrayList<AddressOutput>()
        transaction(getDatabase()) {
            addresses.selectAll().forEach {
                var u = ArrayList<UserOutputNoAddress>()
                useraddresses.select { useraddresses.addressID eq it[addresses.id].value }.forEach {
                    users.select { users.id eq it[useraddresses.userID].value }.forEach {
                        u.add(UserOutputNoAddress(
                                it[users.username],
                                it[users.email],
                                it[users.passwordhash]
                        ))
                    }
                }
                a.add(AddressOutput(
                        Address[it[addresses.id]].id.value,
                        it[addresses.city],
                        it[addresses.street],
                        it[addresses.housenumber],
                        it[addresses.housenumberaddition],
                        it[addresses.postalcode],
                        u
                ))
            }
        }
        return a.toList()
    }

    fun createAddress(address: AddressCreateInput): Address {

        var AddressAlreadyExists: Address? = null
        transaction(getDatabase()) {
            for (a in Address.all()) {
                if (address.postalCode == a.postalcode &&
                        address.houseNumber == a.housenumber &&
                        address.houseNumberAddition == a.housenumberaddition) {
                    AddressAlreadyExists = a
                    break
                }
            }
        }

        return if (AddressAlreadyExists != null) {
            AddressAlreadyExists as Address
        } else {
            createAddress(
                    address.city,
                    address.street,
                    address.houseNumber,
                    address.houseNumberAddition,
                    address.postalCode
            )
        }
    }

    private fun createAddress(
            City: String,
            Street: String,
            HouseNumber: Int,
            HouseNumberAddition: String?,
            PostalCode: String
    ): Address {
        return transaction(getDatabase()) {
            Address.new {
                city = City
                street = Street
                housenumber = HouseNumber
                postalcode = PostalCode
                if (HouseNumberAddition != null) {
                    housenumberaddition = HouseNumberAddition
                }
            }
        }
    }
}