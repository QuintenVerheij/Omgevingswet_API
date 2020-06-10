package com.projdgroep3.omgevingswet.controller

import com.projdgroep3.omgevingswet.models.db.Address
import com.projdgroep3.omgevingswet.models.db.AddressCreateInput
import com.projdgroep3.omgevingswet.models.db.AddressOutput
import com.projdgroep3.omgevingswet.models.misc.Message
import com.projdgroep3.omgevingswet.service.db.AddressService
import com.projdgroep3.omgevingswet.utils.toResponseEntity
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/address")
@Api(tags = ["Address"], description = "CRUD for addresses")
class AddressController {
    /*
    Create
     */
    @RequestMapping("/create", method=[RequestMethod.POST])
    @ApiOperation("Create address")
    fun createAddress(
            @RequestBody address: AddressCreateInput):ResponseEntity<Message> = AddressService.createAddress(address).toResponseEntity()

    @RequestMapping("/read", method=[RequestMethod.GET])
    @ApiOperation("Fetch a list of all addresses")
    fun readAddress(): ResponseEntity<List<AddressOutput>> = AddressService.readAll().toResponseEntity()
}