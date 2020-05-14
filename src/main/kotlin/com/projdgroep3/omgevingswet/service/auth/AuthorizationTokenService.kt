package com.projdgroep3.omgevingswet.service.auth

import com.projdgroep3.omgevingswet.models.auth.*
import com.projdgroep3.omgevingswet.models.misc.Message
import com.projdgroep3.omgevingswet.models.misc.MessageType
import com.projdgroep3.omgevingswet.service.db.UserService
import com.projdgroep3.omgevingswet.utils.EncryptionUtils
import com.projdgroep3.omgevingswet.utils.UUIDUtils
import java.util.concurrent.ConcurrentHashMap

object AuthorizationTokenService {
    private val tokens: ConcurrentHashMap<String, AuthorizedUser> = ConcurrentHashMap()
    private val userLogin: ConcurrentHashMap<Int, String> = ConcurrentHashMap()

    private val internalToken: ConcurrentHashMap<AuthorizationRole, AuthorizationToken> = ConcurrentHashMap()

    fun getInternalToken(role: AuthorizationRole) = requireNotNull(internalToken[role])

    init {
        // Initialize public auth roles
        AuthorizationRole.values().filter { it.isPublic }.forEach { role ->
            val mail = role.id
            val infiniteToken = requestInfiniteToken(
                    AuthorizationTokenRequest(mail, ""),
                    role
            )
            internalToken[role] = AuthorizationToken(requireNotNull(infiniteToken.token))
        }
    }

    private fun requestInfiniteToken(
            request: AuthorizationTokenRequest,
            role: AuthorizationRole
    ): AuthorizationTokenReturn {
        // Fake the member credentials and expire time
        val userId = -1
        val expireTime = Long.MAX_VALUE

        val loginId = EncryptionUtils.hashWithServerSalt(UUIDUtils.get())
        val token = createToken(userId, loginId)

        saveToken(
                token,
                userId,
                role,
                expireTime
        )

        return AuthorizationTokenReturn(true, MessageType.LOGIN, null, userId, AuthRoleSpecification(role), expireTime, token)
    }

    fun requestToken(
            request: AuthorizationTokenRequest
    ): AuthorizationTokenReturn {
        val (userId, message) = UserService.readUserLogin(request)
        return if (message.successful) {
            val loginId = EncryptionUtils.hashWithServerSalt(UUIDUtils.get())
            val expireTime = System.currentTimeMillis() + HOURS_12

            val token = createToken(userId, loginId)

            val role = AuthenticationService.readRoleWithToken(
                    getInternalToken(AuthorizationRole.AUTH_READ),
                    userId
            )

            saveToken(
                    token,
                    userId,
                    role,
                    expireTime
            )

            AuthorizationTokenReturn(true, MessageType.LOGIN, null, userId, AuthRoleSpecification(role), expireTime, token)
        } else {
            AuthorizationTokenReturn(false, message.messageType, message.message, null, null, null, null)
        }
    }

    private fun saveToken(
            token: String,
            userId: Int,
            role: AuthorizationRole,
            expireTime: Long
    ) {
        // Remove previous token
        revokeToken(userId)

        // Set current token
        tokens[token] = AuthorizedUser(userId, role, expireTime)

        // Set reference for user and token
        userLogin[userId] = token
    }

    fun verifyToken(
            request: AuthorizationToken,
            logout: Boolean
    ): AuthorizedUserLogout {
        // Get authorized user for token
        getAuthorizedUserForToken(request)?.let { authorizedUser ->
            // Check if token is still valid, else revoke the token
            return if (verifyIsTokenNotExpired(authorizedUser.expireTime) && !logout) {
                AuthorizedUserLogout(authorizedUser, false)
            } else {
                revokeToken(authorizedUser.userId)
                AuthorizedUserLogout(authorizedUser, true)
            }
        }
        return AuthorizedUserLogout(null, false)
    }

    private fun getAuthorizedUserForToken(request: AuthorizationToken): AuthorizedUser? = tokens[request.token]

    fun whoAmI(request: AuthorizationToken): AuthorizationWhoAmIResult? =
            verifyToken(request, logout = false).user?.let {
                Message(true, MessageType.WHOAMI, AuthorizationType.READ, null, it.userId, it.userId)
                AuthorizationWhoAmIResult(
                        it.userId,
                        AuthRoleSpecification(it.role),
                        it.expireTime
                )
            }

    fun revokeToken(
            request: AuthorizationToken
    ): Message = verifyToken(request, logout = true).let {
        val userId = it.user?.userId ?: -1
        Message(
                it.logout,
                MessageType.LOGOUT,
                AuthorizationType.UPDATE,
                null,
                userId,
                userId
        )
    }

    private fun revokeToken(
            userId: Int
    ) {
        userLogin[userId]?.let {
            tokens.remove(it)
        }
        userLogin.remove(userId)
    }

    private const val HOURS_12: Long = ((1000 * 60) * 60) * 12

    private fun verifyIsTokenNotExpired(
            expireTime: Long
    ): Boolean = expireTime > System.currentTimeMillis()

    private fun createToken(
            userId: Int,
            loginId: String
    ): String = EncryptionUtils.hashWithCryptoSaltAndServerSalt(userId.toString(), loginId)

}