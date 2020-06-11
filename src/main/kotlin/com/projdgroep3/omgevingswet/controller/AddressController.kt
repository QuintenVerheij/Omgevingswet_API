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
import java.math.BigDecimal

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

    @RequestMapping("/read/coords/{id}", method=[RequestMethod.GET])
    @ApiOperation("Get coordinates for address")
    fun getCoords(
            @PathVariable id: Int): ResponseEntity<List<BigDecimal>> = AddressService.getCoords(id).toResponseEntity()

    @RequestMapping("/read/distance/{id1}/{id2}", method=[RequestMethod.GET])
    @ApiOperation("Get coordinates for address")
    fun getDistance(
            @PathVariable id1: Int, @PathVariable id2: Int): ResponseEntity<Double> = AddressService.getDistance(id1, id2).toResponseEntity()
}