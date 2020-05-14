package com.projdgroep3.omgevingswet.controller

import com.projdgroep3.omgevingswet.models.auth.*
import com.projdgroep3.omgevingswet.models.misc.Message
import com.projdgroep3.omgevingswet.models.misc.MessageType
import com.projdgroep3.omgevingswet.service.auth.AuthenticationService
import com.projdgroep3.omgevingswet.service.auth.AuthorizationTokenService
import com.projdgroep3.omgevingswet.utils.toResponseEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/auth")
class AuthController {
    @RequestMapping("/login", method = [RequestMethod.POST])
    fun login(
            @RequestBody request: AuthorizationTokenRequest):
            ResponseEntity<AuthorizationTokenReturn> = AuthenticationService.login(request).toResponseEntity()

    @RequestMapping("/logout", method = [RequestMethod.POST])
    fun logout(
            @RequestBody request: AuthorizationToken):
            ResponseEntity<Message> = AuthenticationService.logout(request).toResponseEntity()

    @RequestMapping("/whoami", method = [RequestMethod.POST])
    fun whoAmI(
            @RequestBody request: AuthorizationToken):
            ResponseEntity<AuthorizationWhoAmIResult> = AuthorizationTokenService.whoAmI(request).toResponseEntity()
}