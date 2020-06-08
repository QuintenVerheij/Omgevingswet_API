package com.projdgroep3.omgevingswet.controller


import UserAddAddressInput
import UserCreateInput
import UserOutput
import com.projdgroep3.omgevingswet.models.auth.AuthorizationRole
import com.projdgroep3.omgevingswet.models.auth.AuthorizationToken
import com.projdgroep3.omgevingswet.models.auth.AuthorizationType
import com.projdgroep3.omgevingswet.models.auth.AuthorizedAction
import com.projdgroep3.omgevingswet.models.misc.Message
import com.projdgroep3.omgevingswet.models.misc.MessageType
import com.projdgroep3.omgevingswet.models.misc.MessageWithItem
import com.projdgroep3.omgevingswet.service.auth.AuthorizationTokenService
import com.projdgroep3.omgevingswet.service.db.UserService
import com.projdgroep3.omgevingswet.utils.toResponseEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/user")
class UserController {
    /*
    Create
     */
    @RequestMapping("/create", method=[RequestMethod.POST])
    fun createUser(
            @RequestBody user: UserCreateInput):ResponseEntity<Message> = UserService.createUser(
            AuthorizedAction<UserCreateInput>(
                    AuthorizationTokenService.getInternalToken(AuthorizationRole.USER_CREATE),
                    user
            )).toResponseEntity()

    @RequestMapping("/create/address", method=[RequestMethod.PUT])
    fun addAddress(
            @RequestBody input: AuthorizedAction<UserAddAddressInput>):ResponseEntity<Message> = UserService.addAddress(input).toResponseEntity()


//    @RequestMapping("/read", method=[RequestMethod.GET])
//    fun readUser(): ResponseEntity<List<UserOutput>> = UserService.readAll().toResponseEntity()

    @RequestMapping("/read", method=[RequestMethod.POST])
    fun readUser(@RequestBody input: AuthorizedAction<Int>): ResponseEntity<MessageWithItem<UserOutput>> = UserService.readUser(input).toResponseEntity()



}