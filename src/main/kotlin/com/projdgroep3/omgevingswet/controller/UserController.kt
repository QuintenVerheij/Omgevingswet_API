package com.projdgroep3.omgevingswet.controller

import User
import UserCreateInput
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
            @RequestBody user: UserCreateInput):ResponseEntity<String> = UserService.createUser(user).toResponseEntity()
}