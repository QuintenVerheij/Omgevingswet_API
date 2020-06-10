package com.projdgroep3.omgevingswet.controller

import com.projdgroep3.omgevingswet.models.auth.*
import com.projdgroep3.omgevingswet.models.misc.Message
import com.projdgroep3.omgevingswet.models.misc.MessageType
import com.projdgroep3.omgevingswet.models.misc.MessageWithItem
import com.projdgroep3.omgevingswet.service.auth.AuthenticationService
import com.projdgroep3.omgevingswet.service.auth.AuthorizationTokenService
import com.projdgroep3.omgevingswet.utils.toResponseEntity
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/auth")
@Api(tags= ["Auth"], description = "login, logout and whoami functions for authenticating users")
class AuthController {
    @RequestMapping("/login", method = [RequestMethod.POST])
    @ApiOperation("Login with mail and password")
    fun login(
            @RequestBody request: AuthorizationTokenRequest):
            ResponseEntity<AuthorizationTokenReturn> = AuthenticationService.login(request).toResponseEntity()

    @RequestMapping("/logout", method = [RequestMethod.POST])
    @ApiOperation("Logout with token")
    fun logout(
            @RequestBody request: AuthorizationToken):
            ResponseEntity<Message> = AuthenticationService.logout(request).toResponseEntity()

    @RequestMapping("/whoami", method = [RequestMethod.POST])
    @ApiOperation("Check if provided token is still valid")
    fun whoAmI(
            @RequestBody request: AuthorizationToken):
            ResponseEntity<MessageWithItem<AuthorizationWhoAmIResult>> = AuthorizationTokenService.whoAmI(request).toResponseEntity()
}