package com.projdgroep3.omgevingswet.service.db

import com.projdgroep3.omgevingswet.logic.Database.getDatabase
import com.projdgroep3.omgevingswet.models.db.Address
import com.projdgroep3.omgevingswet.models.db.AddressCreateInput
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction

object AddressService : DatabaseService<Address>() {
    override fun readAll(): SizedIterable<Address> = transaction(getDatabase()) { Address.all() }

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

        if (AddressAlreadyExists != null) {
            return AddressAlreadyExists as Address
        } else {
            return createAddress(
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