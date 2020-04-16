package com.projdgroep3.omgevingswet.controller

import com.projdgroep3.omgevingswet.models.db.Address
import com.projdgroep3.omgevingswet.models.db.AddressCreateInput
import com.projdgroep3.omgevingswet.models.db.AddressOutput
import com.projdgroep3.omgevingswet.service.db.AddressService
import com.projdgroep3.omgevingswet.utils.toResponseEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/address")
class AddressController {
    /*
    Create
     */
    @RequestMapping("/create", method=[RequestMethod.POST])
    fun createAddress(
            @RequestBody address: AddressCreateInput):ResponseEntity<AddressCreateInput> = AddressService.createAddress(address).toAddressCreateInput().toResponseEntity()

    @RequestMapping("/read", method=[RequestMethod.GET])
    fun readAddress(): ResponseEntity<List<AddressOutput>> = AddressService.readAll().toResponseEntity()
}