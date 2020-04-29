package com.projdgroep3.omgevingswet.controller

import com.projdgroep3.omgevingswet.models.auth.AuthorizationRequest
import com.projdgroep3.omgevingswet.models.auth.AuthorizationType
import com.projdgroep3.omgevingswet.models.misc.Message
import com.projdgroep3.omgevingswet.models.misc.MessageType
import com.projdgroep3.omgevingswet.service.auth.AuthenticationService
import com.projdgroep3.omgevingswet.utils.toResponseEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/auth")
class AuthController {
    @RequestMapping("/login", method = [RequestMethod.POST])
    fun login(
            @RequestBody request: AuthorizationRequest):
            ResponseEntity<Message> = Message(AuthenticationService.login(request), MessageType.LOGIN, AuthorizationType.READ, "bruh", -1, -1).toResponseEntity()
}