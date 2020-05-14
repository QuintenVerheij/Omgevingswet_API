package com.projdgroep3.omgevingswet.models.auth

data class Auth (
        val userId: Int,
        val roleId: String
) {
    fun convert(): AuthorizationRole = requireNotNull(AuthorizationRole.values().find {it.id == roleId})
}

data class AuthRoleSpecification(
        val role: AuthorizationRole,
        val allowedInfo: List<AuthorizationActionType.Info> = role.allowedInfo,
        val allowedCreate: List<AuthorizationActionType.Create> = role.allowedCreate,
        val allowedRead: List<AuthorizationActionType.Read> = role.allowedRead,
        val allowedUpdate: List<AuthorizationActionType.Update> = role.allowedUpdate,
        val allowedDelete: List<AuthorizationActionType.Delete> = role.allowedDelete
)
