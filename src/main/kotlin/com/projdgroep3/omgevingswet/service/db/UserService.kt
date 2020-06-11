package com.projdgroep3.omgevingswet.service.db

import User
import UserAddAddressInput
import UserCreateInput
import UserOutput
import UserOutputPublic
import com.projdgroep3.omgevingswet.config.config
import com.projdgroep3.omgevingswet.config.server
import com.projdgroep3.omgevingswet.logic.Database.getDatabase
import com.projdgroep3.omgevingswet.models.auth.*
import com.projdgroep3.omgevingswet.models.db.Address
import com.projdgroep3.omgevingswet.models.db.AddressCreateInput
import com.projdgroep3.omgevingswet.models.db.addresses
import com.projdgroep3.omgevingswet.models.misc.Message
import com.projdgroep3.omgevingswet.models.misc.MessageType
import com.projdgroep3.omgevingswet.models.misc.MessageWithItem
import com.projdgroep3.omgevingswet.service.auth.AuthorizationService
import com.projdgroep3.omgevingswet.service.auth.AuthorizationTokenService
import com.projdgroep3.omgevingswet.utils.EncryptionUtils
import com.projdgroep3.omgevingswet.utils.FileUtils
import com.projdgroep3.omgevingswet.utils.MessageUtils
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.StreamUtils
import org.springframework.web.multipart.MultipartFile
import useraddresses
import useraddresses.addressID
import useraddresses.userID
import users
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.servlet.ServletContext


object UserService : DatabaseService<UserOutput>() {
    @Autowired
    lateinit var context: ServletContext
    //CREATE
    fun createUser(input: AuthorizedAction<UserCreateInput>): Message = createUser(
            input.auth,
            input.input
    )

    private fun createUser(token: AuthorizationToken, input: UserCreateInput): Message {
        return AuthorizationService.executeCreate(-1, token, AuthorizationActionType.Create.USER) {
            createUser(input)
        }
    }

    private fun createUser(user: UserCreateInput): Message {
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
                    UserAlreadyExists!!.id.value)
        } else {
            var address: Address = AddressService.createAddress(user.address).let {
                transaction(getDatabase()) {
                    Address.findById(it.targetId)
                } ?: throw Exception("Something went wrong creating the address")
            }
            return createUser(
                    user.username,
                    user.email,
                    user.password,
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

    fun addAddress(input: AuthorizedAction<UserAddAddressInput>) = AuthorizationService.executeUpdateUser(
            input.input.userID,
            input.auth,
            AuthorizationActionType.Update.ADDRESS
    ) { user -> addAddress(user, input.input) }

    private fun addAddress(authUser: AuthorizedUser, input: UserAddAddressInput): Message {
        var success = true;
        var present = false;
        transaction(getDatabase()) {
            var user: User? = User.findById(input.userID)
            var address: Address? = Address.findById(input.addressID)
            if (user != null && address != null) {
                if (!useraddresses.select { (userID eq user.id) and (addressID eq address.id) }.empty()) {
                    present = true;
                } else {
                    useraddresses.insert {
                        it[userID] = user.id
                        it[addressID] = address.id
                    }
                }
            } else {
                success = false
            }
        }
        return if (present) {
            Message(
                    successful = true,
                    messageType = MessageType.INFO,
                    authorizationType = AuthorizationType.UPDATE,
                    message = "Address already coupled to user",
                    targetId = input.userID,
                    userId = authUser.userId
            )
        } else {
            return if (!success) {
                Message(
                        false,
                        MessageType.EXCEPTION,
                        AuthorizationType.UPDATE,
                        "Something went wrong adding the address",
                        input.userID,
                        authUser.userId
                )
            } else
                Message(
                        true,
                        MessageType.INFO,
                        AuthorizationType.UPDATE,
                        "Succesfully added address",
                        input.userID,
                        authUser.userId)
        }
    }

    //READ
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
                        a
                ))
            }
        }

        return u.toList()
    }

    fun readUser(
            input: AuthorizedAction<Int>
    ): MessageWithItem<UserOutput> = readUser(
            input.auth,
            input.input
    )

    fun readUser(
            token: AuthorizationToken,
            id: Int
    ): MessageWithItem<UserOutput> = AuthorizationService.executeReadOneUser(id, token, AuthorizationActionType.Read.USER, {
        var u = ArrayList<UserOutput>()
        var a = ArrayList<AddressCreateInput>()
        transaction(getDatabase()) {
            users.select { users.id eq id }.forEach {
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
                        a
                ))
            }
        }
        u.first()
    }) { it as UserOutput }

    fun readOtherUser(id: Int): MessageWithItem<UserOutputPublic> {
        var u = ArrayList<UserOutputPublic>()
        transaction(getDatabase()) {
            users.select { users.id eq id }.forEach {
                u.add(UserOutputPublic(
                        User[it[users.id]].id.value,
                        it[users.username]
                ))
            }
        }
        return MessageWithItem(Message(
                successful = true,
                messageType = MessageType.INFO,
                authorizationType = AuthorizationType.READ,
                message = null,
                targetId = id,
                userId = -1
        ), u.first())
    }

    fun storeImage(input: AuthorizedAction<Int>, file: MultipartFile): Message = AuthorizationService.executeUpdateUser(
            userId = input.input,
            token = input.auth,
            actionType = AuthorizationActionType.Update.USER
    ) {
        try {
            if(!FileUtils.ensureFileFormat(file.originalFilename ?: "", arrayOf(".jpg", ".png"))){
                return@executeUpdateUser Message(
                        successful = false,
                        messageType = MessageType.EXCEPTION,
                        authorizationType = AuthorizationType.UPDATE,
                        message = "Image must be .jpg or .png",
                        targetId = input.input,
                        userId =  AuthorizationTokenService.verifyToken(input.auth, logout = false).user?.userId ?: -1
                )
            }
            val directoryName = FileUtils.createUserDirectory(it.userId)
            val bytes = file.bytes
            val path: Path = Paths.get(directoryName.toString() + "\\" + it.userId.toString() + "_Profile.jpg")
            Files.write(path, bytes)

        } catch (e: IOException){
            println(e.stackTrace)
            return@executeUpdateUser Message(
                    successful = false,
                    messageType = MessageType.EXCEPTION,
                    authorizationType = AuthorizationType.UPDATE,
                    message = "Internal server error occurred storing file",
                    targetId = input.input,
                    userId =  AuthorizationTokenService.verifyToken(input.auth, logout = false).user?.userId ?: -1
            )
        }
        Message.successful(
                AuthorizationTokenService.verifyToken(input.auth, logout = false).user?.userId ?: -1,
                AuthorizationType.UPDATE
        )
    }

    fun serveImage(userId: Int) : ByteArray {

        val path = Paths.get(config[server.files.filedir] + "\\" + userId.toString() + "\\" + userId.toString() + "_Profile.jpg")
        //context.getRealPath("") + config[server.files.imgdir] + "\\" + userId.toString() + "\\" + userId.toString() + ".jpg")
        return if(!Files.exists(path)){
            ByteArray(0)
        }else {
            val stream = Files.newInputStream(path)
            StreamUtils.copyToByteArray(stream)
        }
    }


    fun readUserRole(id: Int): AuthorizationRole? {
        var role: String? = null
        transaction(getDatabase()) {
            users.select { users.id eq id }.forEach {
                role = it[users.globalpermission]
            }
        }
        AuthorizationRole.values().forEach {
            if (role.equals(it.identifier)) {
                return it;
            }
        }
        return null;
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
                        "Incorrect email and password combination",
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
                    "Incorrect email and password combination",
                    user.id.value,
                    user.id.value)
    }
}
