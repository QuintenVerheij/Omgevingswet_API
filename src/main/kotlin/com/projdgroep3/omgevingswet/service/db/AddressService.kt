package com.projdgroep3.omgevingswet.service.db

import UserOutputNoAddress
import com.projdgroep3.omgevingswet.config.config
import com.projdgroep3.omgevingswet.config.server
import com.projdgroep3.omgevingswet.logic.Database.getDatabase
import com.projdgroep3.omgevingswet.models.auth.AuthorizationType
import com.projdgroep3.omgevingswet.models.db.Address
import com.projdgroep3.omgevingswet.models.db.AddressCreateInput
import com.projdgroep3.omgevingswet.models.db.AddressOutput
import com.projdgroep3.omgevingswet.models.db.addresses
import com.projdgroep3.omgevingswet.models.misc.Message
import com.projdgroep3.omgevingswet.models.misc.MessageType
import com.projdgroep3.omgevingswet.utils.MessageUtils
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.client.getForObject
import java.math.BigDecimal

object AddressService : DatabaseService<AddressOutput>() {
    //override fun readAll(): List<Address> = transaction(getDatabase()) { Address.all().toList() }

    fun createAddress(address: AddressCreateInput): Message {

        var addressAlreadyExists: Address? = null
        transaction(getDatabase()) {
            for (a in Address.all()) {
                if (address.postalCode == a.postalcode &&
                        address.houseNumber == a.housenumber &&
                        address.houseNumberAddition == a.housenumberaddition) {
                    addressAlreadyExists = a
                    break
                }
            }
        }

        return if (addressAlreadyExists != null) {
            Message(
                    true,
                    MessageType.INFO,
                    AuthorizationType.CREATE,
                    "Address already exists",
                    addressAlreadyExists!!.id.value,
                    -1)
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
    ): Message {
        //TODO(Wrap with Authorization)
        return MessageUtils.chain(
                userId = -1,
                authType = AuthorizationType.CREATE,
                before = null,
                after = listOf { _ ->
                    MessageUtils.execute(
                            targetId = transaction(getDatabase()) {
                                val addresses = Address.all().sortedByDescending { it.id }
                                if (addresses.isEmpty()) 1 else addresses.first().id.value + 1
                            },
                            userId = -1,
                            authType = AuthorizationType.CREATE) {
                        transaction(getDatabase()) {
                            Address.new {
                                city = City
                                street = Street
                                housenumber = HouseNumber
                                postalcode = PostalCode
                                housenumberaddition = HouseNumberAddition ?: ""
                            }
                        }
                    }
                }
        )


    }

    override fun readAll(): List<AddressOutput> {
        var a = ArrayList<AddressOutput>()
        transaction(getDatabase()) {
            addresses.selectAll().forEach {
                var u = ArrayList<UserOutputNoAddress>()
                useraddresses.select { useraddresses.addressID eq it[addresses.id].value }.forEach {
                    users.select { users.id eq it[useraddresses.userID].value }.forEach {
                        u.add(UserOutputNoAddress(
                                it[users.username],
                                it[users.email]
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

    fun getCoords(addressId: Int): List<BigDecimal> {
        var a: Address? = null
        transaction(getDatabase()) {
            a = Address.findById(addressId)
        }
        var url = ""
        if (a != null) {
            url = "https://api.mapbox.com/geocoding/v5/mapbox.places/" +
                    a?.street + " " +
                    a?.housenumber.toString() + " " + a?.housenumberaddition + " " +
                    a?.city + ".json"

            url += "?access_token=" + config[server.mapbox.accesstoken] + "&autocomplete=false"
            println(url)
        }
        var res: ResponseEntity<String> = RestTemplateBuilder().build().getForEntity(url, String::class)
        return res.body?.let { parseCoords(it) } ?: ArrayList()
    }

    private fun parseCoords(res: String):List<BigDecimal> {
        val start = res.indexOf("coordinates")
        val end = res.indexOf(']', startIndex = start)
        return res.subSequence(start + 14, end).toString().split(',').map { item -> item.toBigDecimal() }
    }

    fun getDistance(addressId1: Int, addressId2: Int):Double {
        val coords1 = getCoords(addressId1)
        val coords2 = getCoords(addressId2)
        return getDistance(coords1[1],coords1[0],coords2[1],coords2[0])
    }

    fun getDistance(coords1: ArrayList<BigDecimal>, coords2: ArrayList<BigDecimal>): Double{
        return getDistance(coords1[1],coords1[0],coords2[1],coords2[0])
    }

    fun getDistance(lat1: BigDecimal,lon1: BigDecimal,lat2: BigDecimal,lon2: BigDecimal):Double {
        val R = 6371e3; // Radius of the earth in km
        val dLat = deg2rad(lat2-lat1);  // deg2rad below
        val dLon = deg2rad(lon2-lon1);
        val a =
                Math.sin((dLat/2.toBigDecimal()).toDouble()) * Math.sin((dLat/2.toBigDecimal()).toDouble()) +
                        Math.cos(deg2rad(lat1).toDouble()) * Math.cos(deg2rad(lat2).toDouble()) *
                        Math.sin((dLon/2.toBigDecimal()).toDouble()) * Math.sin((dLon/2.toBigDecimal()).toDouble())
        ;
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        val d = R * c; // Distance in m
        return d;
    }

    private fun deg2rad(deg: BigDecimal):BigDecimal {
        return deg * (Math.PI/180).toBigDecimal()
    }
}