package com.projdgroep3.omgevingswet.service.db

import User
import UserAddAddressInput
import UserCreateInput
import UserOutput
import com.projdgroep3.omgevingswet.logic.Database.getDatabase
import com.projdgroep3.omgevingswet.models.db.Address
import com.projdgroep3.omgevingswet.models.db.AddressCreateInput
import com.projdgroep3.omgevingswet.models.db.addresses
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import users
import java.lang.Exception

object UserService : DatabaseService<UserOutput>() {
    override fun readAll(): List<UserOutput> {
        var u = ArrayList<UserOutput>()
        transaction(getDatabase()) {
            users.selectAll().forEach {
                var a = ArrayList<AddressCreateInput>()
                useraddresses.select { useraddresses.userID eq it[users.id].value }.forEach {
                    addresses.select { addresses.id eq it[useraddresses.addressID].value }.forEach {
                        a.add(AddressCreateInput(
                                it[addresses.city],
                                it[addresses.street],
                                it[addresses.housenumber],
                                it[addresses.housenumberaddition],
                                it[addresses.postalcode]
                        ))
                    }
                }
                u.add(UserOutput(
                        User[it[users.id]].id.value,
                        it[users.username],
                        it[users.email],
                        it[users.passwordhash],
                        a
                ))
            }
        }

        return u.toList()
    }

    fun createUser(user: UserCreateInput): String {
        var UserAlreadyExists: User? = null
        transaction(getDatabase()) {
            for (u in User.all()) {
                if (u.email == user.email) {
                    UserAlreadyExists = u
                    break
                }
            }
        }

        if (UserAlreadyExists != null) {
            return "User already exists"
        } else {
            var address: Address = AddressService.createAddress(user.address)
            var newUser = createUser(
                    user.username,
                    user.email,
                    user.passwordHash,
                    address,
                    "user"
            )
            return newUser.username + " succesfully created"
        }
    }

    fun createUser(
            Username: String,
            Email: String,
            PasswordHash: String,
            Address: Address,
            GlobalPermission: String
    ): User {
        var User = transaction(getDatabase()) {
            User.new {
                username = Username
                email = Email
                passwordhash = PasswordHash
                globalpermission = GlobalPermission
            }
        }
        transaction(getDatabase()) { User._address = SizedCollection(listOf(Address)) }
        return User
    }

    fun addAddress(input: UserAddAddressInput):String {
        transaction(getDatabase()) {
            useraddresses.insert {
                var user:User? = User.findById(input.userID)
                var address:Address? = Address.findById(input.addressID)

                if (user != null && address != null) {
                    it[userID] = user.id
                    it[addressID] = address.id
                }
            }
        }
        return "Succesfully added address"
    }
}