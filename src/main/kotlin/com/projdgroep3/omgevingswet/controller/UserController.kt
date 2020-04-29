package com.projdgroep3.omgevingswet.controller


import UserAddAddressInput
import UserCreateInput
import UserOutput
import com.projdgroep3.omgevingswet.models.misc.Message
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
            @RequestBody user: UserCreateInput):ResponseEntity<Message> = UserService.createUser(user).toResponseEntity()

    @RequestMapping("/create/address", method=[RequestMethod.PUT])
    fun addAddress(
            @RequestBody input: UserAddAddressInput):ResponseEntity<Message> = UserService.addAddress(input).toResponseEntity()


    @RequestMapping("/read", method=[RequestMethod.GET])
    fun readUser(): ResponseEntity<List<UserOutput>> = UserService.readAll().toResponseEntity()


}