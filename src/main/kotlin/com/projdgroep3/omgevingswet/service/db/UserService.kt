package com.projdgroep3.omgevingswet.service.db

import User
import UserAddAddressInput
import UserCreateInput
import UserOutput
import com.projdgroep3.omgevingswet.logic.Database.getDatabase
import com.projdgroep3.omgevingswet.models.db.Address
import com.projdgroep3.omgevingswet.models.db.AddressCreateInput
import com.projdgroep3.omgevingswet.models.db.addresses
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import users

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
        //TODO(Convert return type to Message)
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
            var address: Address = AddressService.createAddress(user.address).let {
                transaction(getDatabase()) {
                    Address.findById(it.targetId)
                } ?: throw Exception("Something went wrong creating the address")
            }
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
            //TODO(Convert return type to Message)
            Username: String,
            Email: String,
            PasswordHash: String,
            Address: Address,
            GlobalPermission: String
    ): User {
        var user = transaction(getDatabase()) {
            User.new {
                username = Username
                email = Email
                passwordhash = PasswordHash
                globalpermission = GlobalPermission
            }
        }
        transaction(getDatabase()) { user._address = SizedCollection(listOf(Address)) }
        return user
    }

    fun addAddress(input: UserAddAddressInput): String {
        transaction(getDatabase()) {
            useraddresses.insert {
                var user: User? = User.findById(input.userID)
                var address: Address? = Address.findById(input.addressID)

                if (user != null && address != null) {
                    it[userID] = user.id
                    it[addressID] = address.id
                }
            }
        }
        return "Succesfully added address"
    }
}