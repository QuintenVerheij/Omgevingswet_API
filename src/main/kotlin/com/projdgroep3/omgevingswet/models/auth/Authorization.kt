package com.projdgroep3.omgevingswet.models.auth

data class AuthorizationRequest(
        val mail: String,
        val password: String
)