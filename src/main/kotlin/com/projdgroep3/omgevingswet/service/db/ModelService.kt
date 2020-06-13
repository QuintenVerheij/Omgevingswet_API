package com.projdgroep3.omgevingswet.service.db

import com.google.common.io.Files.getFileExtension
import com.projdgroep3.omgevingswet.config.config
import com.projdgroep3.omgevingswet.config.server
import com.projdgroep3.omgevingswet.logic.Database.getDatabase
import com.projdgroep3.omgevingswet.models.auth.AuthorizationActionType
import com.projdgroep3.omgevingswet.models.auth.AuthorizationType
import com.projdgroep3.omgevingswet.models.auth.AuthorizedAction
import com.projdgroep3.omgevingswet.models.db.ModelCreateInput
import com.projdgroep3.omgevingswet.models.db.ModelOutputPreview
import com.projdgroep3.omgevingswet.models.db.addresses
import com.projdgroep3.omgevingswet.models.db.models
import com.projdgroep3.omgevingswet.models.db.models.createdAt
import com.projdgroep3.omgevingswet.models.db.models.visibleRange
import com.projdgroep3.omgevingswet.models.misc.Message
import com.projdgroep3.omgevingswet.models.misc.MessageType
import com.projdgroep3.omgevingswet.models.misc.MessageWithItem
import com.projdgroep3.omgevingswet.service.auth.AuthorizationService
import com.projdgroep3.omgevingswet.service.auth.AuthorizationTokenService
import com.projdgroep3.omgevingswet.utils.FileUtils
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.util.StreamUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate

object ModelService : DatabaseService<ModelOutputPreview>() {
    fun createData(data: ModelCreateInput) = AuthorizationService.executeUpdateUser(
            userId = data.userId,
            token = data.token,
            actionType = AuthorizationActionType.Update.USER
    ) {
        var id = -1
        transaction(getDatabase()) {
            id = models.insertAndGetId {
                it[userId] = users.select { users.id eq data.userId }.first()[users.id]
                it[public] = data.public
                it[visibleRange] = data.visibleRange
                it[longitude] = data.longitude
                it[latitude] = data.latitude
                it[createdAt] = LocalDate.now().toString()
            }.value
        }
        return@executeUpdateUser if (id == -1)
            Message(
                    successful = false,
                    authorizationType = AuthorizationType.CREATE,
                    messageType = MessageType.EXCEPTION,
                    message = "Could not add metadata of model into database",
                    targetId = id,
                    userId = data.userId
            )
        else Message(
                successful = true,
                authorizationType = AuthorizationType.CREATE,
                messageType = MessageType.INFO,
                message = "Please follow up by uploading files with targetId as argument",
                targetId = id,
                userId = data.userId)

    }

    override fun readAll(): List<ModelOutputPreview> {
        val out = ArrayList<ModelOutputPreview>()
        transaction(getDatabase()) {
            models.select { models.public eq true }.forEach {
                out.add(ModelOutputPreview(
                        it[models.id].value,
                        it[models.userId].value,
                        it[models.public],
                        it[models.visibleRange],
                        it[models.longitude],
                        it[models.latitude],
                        it[createdAt],
                        servePreview(it[models.userId].value, it[models.id].value)
                ))
            }
        }
        return out
    }

    fun readAll(auth: AuthorizedAction<Int>): MessageWithItem<List<ModelOutputPreview>> = AuthorizationService.executeReadUsers(
            userId = auth.input,
            token = auth.auth,
            actionType = AuthorizationActionType.Read.MODEL,
            action = {
                val out = readAll() as java.util.ArrayList<ModelOutputPreview>
                var addressIds = ArrayList<Int>()
                transaction(getDatabase()) {
                    useraddresses.select { useraddresses.userID eq auth.input }.forEach {
                        addressIds.add(it[useraddresses.addressID].value)
                    }
                }

                var coordAddresses: ArrayList<ArrayList<BigDecimal>> = ArrayList()
                addressIds.forEach {
                    coordAddresses.add(AddressService.getCoords(it) as java.util.ArrayList<BigDecimal>)
                }
                transaction(getDatabase()) {

                    models.select { models.public eq false }.forEach {
                        val coords: ArrayList<BigDecimal> = ArrayList()
                        coords.add(it[models.latitude])
                        coords.add(it[models.longitude])

                        for (item in coordAddresses) {
                            if (AddressService.getDistance(item, coords) <= it[visibleRange]) {
                                out.add(ModelOutputPreview(
                                        it[models.id].value,
                                        it[models.userId].value,
                                        it[models.public],
                                        it[models.visibleRange],
                                        it[models.longitude],
                                        it[models.latitude],
                                        it[createdAt],
                                        servePreview(it[models.userId].value, it[models.id].value)
                                ))
                            }
                        }
                    }
                }
                return@executeReadUsers out
            }) { it as ModelOutputPreview }

    fun read(id: Int): MessageWithItem<ByteArray> {
        var isPublic = false
        transaction(getDatabase()) {
            if (models.select { models.id eq id }.first()[models.public]) {
                isPublic = true
            }
        }
        if (isPublic) {
            return MessageWithItem(Message.successfulEmpty(), serveModel(id))
        }
        return MessageWithItem(Message(
                successful = false,
                messageType = MessageType.NOT_AUTHORIZED,
                authorizationType = AuthorizationType.READ,
                message = "Not authorized to perform this action",
                targetId = id,
                userId = -1
        ), null)
    }

    fun readBy(userId: Int): MessageWithItem<ArrayList<ModelOutputPreview>> {
        val out = ArrayList<ModelOutputPreview>()
        transaction(getDatabase()) {
            models.select { (models.public eq true) and (models.userId eq userId)}.forEach {
                out.add(ModelOutputPreview(
                        it[models.id].value,
                        it[models.userId].value,
                        it[models.public],
                        it[models.visibleRange],
                        it[models.longitude],
                        it[models.latitude],
                        it[createdAt],
                        servePreview(it[models.userId].value, it[models.id].value)
                ))
            }
        }
        return if(!out.isEmpty()){
            MessageWithItem(Message.successfulEmpty(), out)
        }else{
            MessageWithItem(Message(
                    successful = false,
                    messageType = MessageType.EXCEPTION,
                    authorizationType = AuthorizationType.READ,
                    message = "Could not find any models with specified parameters",
                    targetId = userId,
                    userId = -1),out)
        }
    }

    fun readBy(auth: AuthorizedAction<Int>, targetId: Int) : MessageWithItem<ArrayList<ModelOutputPreview>> = AuthorizationService.executeGenericUser(
            userId = auth.input,
            token = auth.auth,
            actionType = AuthorizationActionType.Read.MODEL,
            action = {
                val out = readBy(targetId).item as java.util.ArrayList<ModelOutputPreview>
                var addressIds = ArrayList<Int>()
                transaction(getDatabase()) {
                    useraddresses.select { useraddresses.userID eq auth.input }.forEach {
                        addressIds.add(it[useraddresses.addressID].value)
                    }
                }

                var coordAddresses: ArrayList<ArrayList<BigDecimal>> = ArrayList()
                addressIds.forEach {
                    coordAddresses.add(AddressService.getCoords(it) as java.util.ArrayList<BigDecimal>)
                }
                transaction(getDatabase()) {

                    models.select { (models.public eq false) and (models.userId eq targetId)}.forEach {
                        val coords: ArrayList<BigDecimal> = ArrayList()
                        coords.add(it[models.latitude])
                        coords.add(it[models.longitude])

                        for (item in coordAddresses) {
                            if (AddressService.getDistance(item, coords) <= it[visibleRange]) {
                                out.add(ModelOutputPreview(
                                        it[models.id].value,
                                        it[models.userId].value,
                                        it[models.public],
                                        it[models.visibleRange],
                                        it[models.longitude],
                                        it[models.latitude],
                                        it[createdAt],
                                        servePreview(it[models.userId].value, it[models.id].value)
                                ))
                            }
                        }
                    }
                }
                return@executeGenericUser out
            },
            identifier = targetId
    )

    fun readBy(auth: AuthorizedAction<Int>) : MessageWithItem<ArrayList<ModelOutputPreview>> = AuthorizationService.executeGenericUser(
            userId = auth.input,
            token = auth.auth,
            actionType = AuthorizationActionType.Read.MODEL,
            action = {
                val out = ArrayList<ModelOutputPreview>()
                transaction(getDatabase()) {

                    models.select { models.userId eq auth.input }.forEach {
                        out.add(ModelOutputPreview(
                                it[models.id].value,
                                it[models.userId].value,
                                it[models.public],
                                it[models.visibleRange],
                                it[models.longitude],
                                it[models.latitude],
                                it[createdAt],
                                servePreview(it[models.userId].value, it[models.id].value)
                        ))
                    }
                }
                return@executeGenericUser out
            },
            identifier = auth.input
    )

    fun read(auth: AuthorizedAction<Int>, id: Int): MessageWithItem<ByteArray> = AuthorizationService.executeGenericUser(
            userId = auth.input,
            token = auth.auth,
            actionType = AuthorizationActionType.Read.MODEL,
            action =
            {
                var isPublic = false
                val addressIds = ArrayList<Int>()

                transaction(getDatabase()) {
                    if (models.select { models.id eq id }.first()[models.public]) {
                        isPublic = true
                    } else {
                        useraddresses.select { useraddresses.userID eq auth.input }.forEach {
                            addressIds.add(it[useraddresses.addressID].value)
                        }
                        val coordAddresses: ArrayList<ArrayList<BigDecimal>> = ArrayList()
                        addressIds.forEach {
                            coordAddresses.add(AddressService.getCoords(it) as java.util.ArrayList<BigDecimal>)
                        }
                        val coords: ArrayList<BigDecimal> = ArrayList()
                        val res = models.select { models.id eq id }.first()
                        coords.add(res[models.latitude])
                        coords.add(res[models.longitude])
                        coordAddresses.forEach {
                            if (AddressService.getDistance(it, coords) < res[visibleRange]) {
                                isPublic = true
                            }
                        }
                    }
                }
                if (isPublic) {
                    return@executeGenericUser serveModel(id)
                }
                return@executeGenericUser ByteArray(0)
            }, identifier = id)

    fun updateFiles(auth: AuthorizedAction<Int>, modelId: Int, preview: MultipartFile, model: MultipartFile): Message {
        var m = storePreview(auth, preview, modelId)
        if (m.successful) {
            return storeModel(auth, model, modelId)
        }
        return m;
    }


    fun servePreview(modelId: Int): ByteArray {
        return transaction(getDatabase()) {
            if (models.select { models.id eq modelId }.count() == 1.toLong()) {
                return@transaction servePreview(models.select { models.id eq modelId }.first()[models.userId].value, modelId)
            } else {
                return@transaction ByteArray(0)
            }
        }
    }

    private fun servePreview(userId: Int, modelId: Int): ByteArray {
        val path = Paths.get(config[server.files.filedir] + "\\" + userId.toString() + "\\" + modelId.toString() + "_Preview.jpg")
        return if (!Files.exists(path)) {
            ByteArray(0)
        } else {
            val stream = Files.newInputStream(path)
            StreamUtils.copyToByteArray(stream)
        }
    }

    fun serveModel(modelId: Int): ByteArray {
        return transaction(getDatabase()) {
            if (models.select { models.id eq modelId }.count() == 1.toLong()) {
                return@transaction serveModel(models.select { models.id eq modelId }.first()[models.userId].value, modelId)
            } else {
                return@transaction ByteArray(0)
            }
        }
    }

    private fun serveModel(userId: Int, modelId: Int): ByteArray {
        val path = Paths.get(config[server.files.filedir] + "\\" + userId.toString() + "\\" + modelId.toString() + ".jpg")
        return if (!Files.exists(path)) {
            ByteArray(0)
        } else {
            val stream = Files.newInputStream(path)
            StreamUtils.copyToByteArray(stream)
        }
    }

    fun storePreview(input: AuthorizedAction<Int>, file: MultipartFile, modelId: Int): Message = AuthorizationService.executeUpdateUser(
            userId = input.input,
            token = input.auth,
            actionType = AuthorizationActionType.Update.MODEL
    ) {
        try {
            if (!FileUtils.ensureFileFormat(file.originalFilename ?: "", arrayOf("jpg", "png"))) {
                return@executeUpdateUser Message(
                        successful = false,
                        messageType = MessageType.EXCEPTION,
                        authorizationType = AuthorizationType.UPDATE,
                        message = "Image must be .jpg or .png",
                        targetId = modelId,
                        userId = AuthorizationTokenService.verifyToken(input.auth, logout = false).user?.userId ?: -1
                )
            }
            val directoryName = FileUtils.createUserDirectory(it.userId);
            val bytes = file.bytes
            val path: Path = Paths.get(directoryName.toString() + "\\" + modelId.toString() + "_Preview.jpg")
            Files.write(path, bytes)

        } catch (e: IOException) {
            println(e.stackTrace)
            return@executeUpdateUser Message(
                    successful = false,
                    messageType = MessageType.EXCEPTION,
                    authorizationType = AuthorizationType.UPDATE,
                    message = "Internal server error occurred storing file",
                    targetId = modelId,
                    userId = AuthorizationTokenService.verifyToken(input.auth, logout = false).user?.userId ?: -1
            )
        }
        Message.successful(
                AuthorizationTokenService.verifyToken(input.auth, logout = false).user?.userId ?: -1,
                AuthorizationType.UPDATE
        )
    }

    fun storeModel(input: AuthorizedAction<Int>, file: MultipartFile, modelId: Int): Message = AuthorizationService.executeUpdateUser(
            userId = input.input,
            token = input.auth,
            actionType = AuthorizationActionType.Update.MODEL
    ) {
        try {
            if (!FileUtils.ensureFileFormat(file.originalFilename ?: "", arrayOf("fbx"))) {
                return@executeUpdateUser Message(
                        successful = false,
                        messageType = MessageType.EXCEPTION,
                        authorizationType = AuthorizationType.UPDATE,
                        message = "Model must be .fbx",
                        targetId = input.input,
                        userId = AuthorizationTokenService.verifyToken(input.auth, logout = false).user?.userId ?: -1
                )
            }
            val directoryName = FileUtils.createUserDirectory(it.userId);
            val bytes = file.bytes
            val path: Path = Paths.get("$directoryName\\$modelId.fbx")
            Files.write(path, bytes)

        } catch (e: IOException) {
            println(e.stackTrace)
            return@executeUpdateUser Message(
                    successful = false,
                    messageType = MessageType.EXCEPTION,
                    authorizationType = AuthorizationType.UPDATE,
                    message = "Internal server error occurred storing file",
                    targetId = input.input,
                    userId = AuthorizationTokenService.verifyToken(input.auth, logout = false).user?.userId ?: -1
            )
        }
        Message.successful(
                AuthorizationTokenService.verifyToken(input.auth, logout = false).user?.userId ?: -1,
                AuthorizationType.UPDATE
        )
    }
}