package com.projdgroep3.omgevingswet.service.db

import User
import UserAddAddressInput
import UserCreateInput
import UserOutput
import com.projdgroep3.omgevingswet.logic.Database.getDatabase
import com.projdgroep3.omgevingswet.models.auth.AuthorizationTokenRequest
import com.projdgroep3.omgevingswet.models.auth.AuthorizationType
import com.projdgroep3.omgevingswet.models.db.Address
import com.projdgroep3.omgevingswet.models.db.AddressCreateInput
import com.projdgroep3.omgevingswet.models.db.addresses
import com.projdgroep3.omgevingswet.models.misc.Message
import com.projdgroep3.omgevingswet.models.misc.MessageType
import com.projdgroep3.omgevingswet.utils.EncryptionUtils
import com.projdgroep3.omgevingswet.utils.MessageUtils
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import useraddresses
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

    fun createUser(user: UserCreateInput): Message {
        //TODO(Wrap with authorization)
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
            return Message(
                    true,
                    MessageType.INFO,
                    AuthorizationType.CREATE,
                    "User already exists",
                    UserAlreadyExists!!.id.value,
                    -1)
        } else {
            var address: Address = AddressService.createAddress(user.address).let {
                transaction(getDatabase()) {
                    Address.findById(it.targetId)
                } ?: throw Exception("Something went wrong creating the address")
            }
            return createUser(
                    user.username,
                    user.email,
                    user.passwordHash,
                    address,
                    "user"
            )
        }
    }

    fun createUser(
            Username: String,
            Email: String,
            PasswordHash: String,
            Address: Address,
            GlobalPermission: String
    ): Message {
        //TODO(Wrap with authorization)
        return MessageUtils.chain(
                userId = -1,
                authType = AuthorizationType.CREATE,
                before = null,
                after = listOf { _ ->
                    MessageUtils.execute(
                            targetId = transaction(getDatabase()) {
                                val users = User.all().sortedByDescending { it.id }
                                if (users.isEmpty()) 1 else users.first().id.value + 1
                            },
                            userId = -1,
                            authType = AuthorizationType.CREATE) {
                        val user = transaction(getDatabase()) {
                            User.new {
                                username = Username
                                email = Email
                                cryptosalt = EncryptionUtils.getSalt()
                                passwordhash = EncryptionUtils.hashWithCryptoSaltAndServerSalt(PasswordHash, cryptosalt)
                                globalpermission = GlobalPermission
                            }
                        }
                        transaction(getDatabase()) { user._address = SizedCollection(listOf(Address)) }
                    }
                }
        )
    }

    fun addAddress(input: UserAddAddressInput): Message {
        var success = true;
        transaction(getDatabase()) {
            useraddresses.insert {
                var user: User? = User.findById(input.userID)
                var address: Address? = Address.findById(input.addressID)

                if (user != null && address != null) {
                    it[userID] = user.id
                    it[addressID] = address.id
                } else {
                    success = false
                }
            }
        }
        return if (!success) {
            Message(
                    false,
                    MessageType.EXCEPTION,
                    AuthorizationType.UPDATE,
                    "Something went wrong adding the address",
                    input.userID,
                    -1
            )
        } else
            Message(
                    true,
                    MessageType.INFO,
                    AuthorizationType.UPDATE,
                    "Succesfully added address",
                    input.userID,
                    -1)
    }

    /*
    Read user login
     */
    fun readUserLogin(
            request: AuthorizationTokenRequest
    ): Pair<Int, Message> = readUserLogin(
            request.mail,
            request.password
    )

    private fun readUserLogin(
            mail: String,
            password: String
    ): Pair<Int, Message> {
        val user = transaction(getDatabase()) {
            val users = User.find { users.email eq mail }
            if (users.empty()) null else users.first()
        }
        //Return failed message if email is not present
                ?: return -1 to Message(
                        false,
                        MessageType.INVALID_CREDENTIALS,
                        AuthorizationType.READ,
                        "Incorrect email address",
                        -1,
                        -1)
        return if (user.passwordhash == EncryptionUtils.hashWithCryptoSaltAndServerSalt(password, user.cryptosalt))
            user.id.value to Message.successful(
                    user.id.value,
                    AuthorizationType.READ)
        else
            user.id.value to Message(
                    false,
                    MessageType.INVALID_LOGIN_ATTEMPT,
                    AuthorizationType.READ,
                    "Incorrect password for email address",
                    user.id.value,
                    user.id.value)
    }
}