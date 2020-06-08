package com.projdgroep3.omgevingswet.service.auth

import com.projdgroep3.omgevingswet.models.auth.AuthorizationActionType
import com.projdgroep3.omgevingswet.models.auth.AuthorizationToken
import com.projdgroep3.omgevingswet.models.auth.AuthorizationType
import com.projdgroep3.omgevingswet.models.auth.AuthorizedUser
import com.projdgroep3.omgevingswet.models.misc.Message
import com.projdgroep3.omgevingswet.models.misc.MessageType
import com.projdgroep3.omgevingswet.models.misc.MessageWithItem
import com.projdgroep3.omgevingswet.service.Identifiable

object AuthorizationService {
    private fun userCheck(
            user: AuthorizedUser,
            id: Int
    ): Boolean = user.role.skipUserCheck || user.userId == id

    private fun execute(
            targetId: Int,
            token: AuthorizationToken,
            authorizationType: AuthorizationType,
            action: (AuthorizedUser) -> Message,
            verifyUser: (AuthorizedUser) -> Boolean
    ): Message {
        val authorizedUser = AuthorizationTokenService.verifyToken(token, logout = false).user
        return authorizedUser?.let { user ->
            if (verifyUser(user)) {
                action(user)
            } else {
                Message(
                        successful = false,
                        messageType = MessageType.NOT_AUTHORIZED,
                        authorizationType = authorizationType,
                        message = "Not authorized to perform this action",
                        targetId = targetId,
                        userId = user.userId
                )
            }
        } ?: Message(
                successful = false,
                messageType = MessageType.INVALID_TOKEN,
                authorizationType = authorizationType,
                message = "Invalid token",
                targetId = targetId,
                userId = -1
        )
    }

    private fun executeIdentifiable(
            targetId: Int?,
            token: AuthorizationToken,
            authorizationType: AuthorizationType,
            action: () -> Identifiable,
            verifyUser: (AuthorizedUser) -> Boolean
    ): MessageWithItem<Identifiable> = executeGeneric(
            targetId,
            token,
            authorizationType,
            action,
            verifyUser
    ) { it.getIdentifier() }

    private fun <T> executeGeneric(
            targetId: Int?,
            token: AuthorizationToken,
            authorizationType: AuthorizationType,
            action: () -> T,
            verifyUser: (AuthorizedUser) -> Boolean,
            identifier: (T) -> Int
    ): MessageWithItem<T> {
        val authorizedUser = AuthorizationTokenService.verifyToken(token, logout = false).user
        return authorizedUser?.let { user ->
            if (verifyUser(user)) {
                val item = action()
                MessageWithItem(
                        Message(
                                successful = true,
                                messageType = MessageType.NONE,
                                authorizationType = authorizationType,
                                message = null,
                                targetId = targetId ?: item?.let(identifier) ?: -1,
                                userId = user.userId
                        ), item
                )
            } else {
                MessageWithItem<T>(
                        Message(
                                successful = false,
                                messageType = MessageType.NOT_AUTHORIZED,
                                authorizationType = authorizationType,
                                message = "Not authorized to perform this action",
                                targetId = targetId ?: -1,
                                userId = user.userId
                        ), null)
            }
        } ?: MessageWithItem<T>(
                Message(
                        successful = false,
                        messageType = MessageType.INVALID_TOKEN,
                        authorizationType = authorizationType,
                        message = "Invalid token",
                        targetId = targetId ?: -1,
                        userId = -1
                ), null)
    }

    private fun executeGenericList(
            targetId: Int?,
            token: AuthorizationToken,
            authorizationType: AuthorizationType,
            action: () -> List<Identifiable>,
            verifyUser: (AuthorizedUser) -> Boolean
    ): MessageWithItem<List<Identifiable>> {
        val authorizedUser = AuthorizationTokenService.verifyToken(token, logout = false).user
        return authorizedUser?.let { user ->
            if (verifyUser(user)) {
                val item = action();
                MessageWithItem(
                        Message(
                                successful = true,
                                messageType = MessageType.NONE,
                                authorizationType = authorizationType,
                                message = null,
                                targetId = targetId ?: -1,
                                userId = user.userId
                        ), item)
            } else {
                MessageWithItem<List<Identifiable>>(
                        Message(
                                successful = false,
                                messageType = MessageType.NOT_AUTHORIZED,
                                authorizationType = authorizationType,
                                message = "Not authorized to perform this action",
                                targetId = targetId ?: -1,
                                userId = user.userId
                        ), null
                )
            }
        } ?: MessageWithItem<List<Identifiable>>(
                Message(
                        successful = false,
                        messageType = MessageType.INVALID_TOKEN,
                        authorizationType = authorizationType,
                        message = "Invalid token",
                        targetId = targetId ?: -1,
                        userId = -1
                ), null)
    }

    fun executeCreate(
            id: Int,
            token: AuthorizationToken,
            actionType: AuthorizationActionType.Create,
            action: (AuthorizedUser) -> Message
    ): Message = execute(id, token, actionType.getAuthorizationType(), action) { user -> actionType in user.role.allowedCreate }

    fun <T> executeRead(
            id: Int?,
            token: AuthorizationToken,
            actionType: AuthorizationActionType.Read,
            action: () -> List<Identifiable>,
            cast: (Identifiable) -> T
    ): MessageWithItem<List<T>> = executeGenericList(id, token, actionType.getAuthorizationType(), action) { user -> actionType in user.role.allowedRead }.let {
        MessageWithItem(it.message, it.item?.map(cast))
    }

    fun <T> executeReadOne(
            id: Int?,
            token: AuthorizationToken,
            actionType: AuthorizationActionType.Read,
            action: () -> Identifiable,
            cast: (Identifiable) -> T
    ): MessageWithItem<T> = executeIdentifiable(id, token, actionType.getAuthorizationType(), action) { user -> actionType in user.role.allowedRead }.let {
        MessageWithItem(it.message, it.item?.let(cast))
    }

    fun <T> executeReadUsers(
            userId: Int,
            token: AuthorizationToken,
            actionType: AuthorizationActionType.Read,
            action: () -> List<Identifiable>,
            cast: (Identifiable) -> T
    ): MessageWithItem<List<T>> = executeGenericList(userId, token, actionType.getAuthorizationType(), action) { user -> actionType in user.role.allowedRead && userCheck(user, userId)}.let {
        MessageWithItem(it.message, it.item?.map(cast))
    }

    fun <T> executeReadOneUser(
            userId: Int,
            token: AuthorizationToken,
            actionType: AuthorizationActionType.Read,
            action: () -> Identifiable,
            cast: (Identifiable) -> T
    ): MessageWithItem<T> = executeIdentifiable(userId, token, actionType.getAuthorizationType(), action) { user -> actionType in user.role.allowedRead && userCheck(user, userId) }.let {
        MessageWithItem(it.message, it.item?.let(cast))
    }

    fun executeUpdate(
            id: Int,
            token: AuthorizationToken,
            actionType: AuthorizationActionType.Update,
            action: (AuthorizedUser) -> Message
    ): Message = execute(id, token, actionType.getAuthorizationType(), action) { user -> actionType in user.role.allowedUpdate }

    fun executeUpdateUser(
            userId: Int,
            token: AuthorizationToken,
            actionType: AuthorizationActionType.Update,
            action: (AuthorizedUser) -> Message
    ): Message = execute(userId, token, actionType.getAuthorizationType(), action) { user -> actionType in user.role.allowedUpdate && userCheck(user, userId) }

    fun executeDelete(
            id: Int,
            token: AuthorizationToken,
            actionType: AuthorizationActionType.Delete,
            action: (AuthorizedUser) -> Message
    ): Message = execute(id, token, actionType.getAuthorizationType(), action) { user -> actionType in user.role.allowedDelete }

    fun executeDeleteUser(
            userId: Int,
            token: AuthorizationToken,
            actionType: AuthorizationActionType.Delete,
            action: (AuthorizedUser) -> Message
    ): Message = execute(userId, token, actionType.getAuthorizationType(), action) { user -> actionType in user.role.allowedDelete && userCheck(user, userId)}
}