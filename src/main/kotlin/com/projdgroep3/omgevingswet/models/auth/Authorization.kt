package com.projdgroep3.omgevingswet.models.auth

import com.projdgroep3.omgevingswet.models.misc.MessageType

data class AuthorizationToken(
        //Single attribute data classes need default arguments or jackson wont deserialize them
        val token: String = ""
)

data class AuthorizationTokenRequest(
        val mail: String,
        val password: String
)

data class AuthorizationTokenReturn(
        val successful: Boolean,
        val messageType: MessageType,
        val message: String?,
        val userId: Int?,
        val role: AuthRoleSpecification?,
        val expireTime: Long?,
        val token: String?
)

data class AuthorizationWhoAmIResult(
        val userId: Int,
        val role: AuthRoleSpecification,
        val expireTime: Long
)

data class AuthorizedAction<T>(
        val auth: AuthorizationToken,
        val input: T
)

data class AuthorizedUserLogout(
        val user: AuthorizedUser?,
        val logout: Boolean
)

data class AuthorizedUser(
        val userId: Int,
        val role: AuthorizationRole,
        val expireTime: Long
)