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
import com.projdgroep3.omgevingswet.models.db.models
import com.projdgroep3.omgevingswet.models.db.models.createdAt
import com.projdgroep3.omgevingswet.models.misc.Message
import com.projdgroep3.omgevingswet.models.misc.MessageType
import com.projdgroep3.omgevingswet.service.auth.AuthorizationService
import com.projdgroep3.omgevingswet.service.auth.AuthorizationTokenService
import com.projdgroep3.omgevingswet.utils.FileUtils
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.util.StreamUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
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

    fun updateFiles(auth: AuthorizedAction<Int>, modelId: Int, preview: MultipartFile, model: MultipartFile): Message {
        var m = storePreview(auth, preview, modelId)
        if (m.successful) {
            return storeModel(auth, model, modelId)
        }
        return m;
    }


    fun servePreview(modelId: Int): ByteArray {
        return transaction(getDatabase()) {
            if (models.select { models.id eq modelId }.fetchSize == 1) {
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
            if (models.select { models.id eq modelId }.fetchSize == 1) {
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