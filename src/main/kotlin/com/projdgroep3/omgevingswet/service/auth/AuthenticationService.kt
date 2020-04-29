package com.projdgroep3.omgevingswet.service.auth

import com.projdgroep3.omgevingswet.models.auth.Auth
import com.projdgroep3.omgevingswet.models.auth.AuthorizationRequest
import com.projdgroep3.omgevingswet.models.misc.Message
import com.projdgroep3.omgevingswet.service.db.DatabaseService
import com.projdgroep3.omgevingswet.service.db.UserService

object AuthenticationService: DatabaseService<Auth>() {
    override fun readAll(): List<Auth> {
        TODO("Not yet implemented")
    }

    fun login(request: AuthorizationRequest): Boolean =
            //TODO("Implement token")
            UserService.readUserLogin(request)
}