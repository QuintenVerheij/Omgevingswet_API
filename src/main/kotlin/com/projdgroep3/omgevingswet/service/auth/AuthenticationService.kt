package com.projdgroep3.omgevingswet.service.auth

import com.projdgroep3.omgevingswet.logic.Database.getDatabase
import com.projdgroep3.omgevingswet.models.auth.*
import com.projdgroep3.omgevingswet.models.misc.Message
import com.projdgroep3.omgevingswet.service.db.DatabaseService
import com.projdgroep3.omgevingswet.service.db.UserService
import org.jetbrains.exposed.sql.transactions.transaction

object AuthenticationService : DatabaseService<Auth>() {
    override fun readAll(): List<Auth> {
        TODO("Not yet implemented")
    }

    fun login(request: AuthorizationTokenRequest): AuthorizationTokenReturn =
            AuthorizationTokenService.requestToken(request)

    fun logout(request: AuthorizationToken): Message =
            AuthorizationTokenService.revokeToken(request)

    fun readRoleWithToken(
            token: AuthorizationToken,
            userId: Int
            //TODO("Implement AuthorizationService actually enforcing authorization")
    ): AuthorizationRole {
        val user = transaction(getDatabase()) {
            val users = User.find { users.id eq userId }
            if (users.empty()) null else users.first()
        } ?: return AuthorizationRole.USER
        return Auth(user.id.value, user.globalpermission).convert()
    }

}