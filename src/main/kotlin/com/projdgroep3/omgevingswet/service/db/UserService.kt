package com.projdgroep3.omgevingswet.service.db

import User
import UserCreateInput
import com.projdgroep3.omgevingswet.logic.Database.getDatabase
import com.projdgroep3.omgevingswet.models.db.Address
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Exception

object UserService : DatabaseService<User>() {

    override fun readAll(): SizedIterable<User> = transaction(getDatabase()) { User.all() }

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
}