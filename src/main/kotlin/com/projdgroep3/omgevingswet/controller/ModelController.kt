package com.projdgroep3.omgevingswet.controller

import com.projdgroep3.omgevingswet.models.auth.*
import com.projdgroep3.omgevingswet.models.db.ModelCreateInput
import com.projdgroep3.omgevingswet.models.db.ModelOutput
import com.projdgroep3.omgevingswet.models.db.ModelOutputPreview
import com.projdgroep3.omgevingswet.models.misc.Message
import com.projdgroep3.omgevingswet.models.misc.MessageType
import com.projdgroep3.omgevingswet.models.misc.MessageWithItem
import com.projdgroep3.omgevingswet.service.auth.AuthenticationService
import com.projdgroep3.omgevingswet.service.auth.AuthorizationTokenService
import com.projdgroep3.omgevingswet.service.db.ModelService
import com.projdgroep3.omgevingswet.service.db.UserService
import com.projdgroep3.omgevingswet.utils.toResponseEntity
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import retrofit2.http.Path

@CrossOrigin
@RestController
@RequestMapping("/model")
@Api(tags = ["Model"], description = "CRUD for models")
class ModelController {
    @RequestMapping("/create", method = [RequestMethod.POST])
    @ApiOperation("Upload model metadata and associate with user by id")
    fun createModel(@RequestBody data: ModelCreateInput): ResponseEntity<MessageWithItem<Int>> {
        val m = ModelService.createData(data)
        return MessageWithItem(m, m.targetId).toResponseEntity()
    }

    @RequestMapping("/create/files", method = [RequestMethod.POST])
    @ApiOperation("Upload files to associate with modelId")
    fun uploadFiles(@RequestParam token: AuthorizationToken,
                    @RequestParam userId: Int,
                    @RequestParam modelId: Int,
                    @RequestParam preview: MultipartFile,
                    @RequestParam modelJson: MultipartFile): ResponseEntity<Message> {
       return ModelService.updateFiles(AuthorizedAction(token, userId),modelId, preview, modelJson).toResponseEntity()
    }

    @RequestMapping("/public/read", method = [RequestMethod.GET])
    @ApiOperation("Get info about all public models and download thumbnails")
    fun readAllPublic(): ResponseEntity<List<ModelOutputPreview>> = ModelService.readAll().toResponseEntity()

    @RequestMapping("public/read/{id}", method = [RequestMethod.GET])
    @ApiOperation("Download model if it is publicly visible")
    fun readPublic(@PathVariable id: Int): ResponseEntity<MessageWithItem<ModelOutput>> = ModelService.read(id).toResponseEntity()

    @RequestMapping("public/read/by/{id}", method = [RequestMethod.GET])
    @ApiOperation("Get info about all models available to this account by a certain user")
    fun readByPublic(@PathVariable id: Int): ResponseEntity<MessageWithItem<ArrayList<ModelOutputPreview>>> = ModelService.readBy(id).toResponseEntity()

    @RequestMapping("/read", method = [RequestMethod.POST])
    @ApiOperation("Get info about all models available to this account and download thumbnails")
    fun readAll(@RequestBody auth : AuthorizedAction<Int>): ResponseEntity<MessageWithItem<List<ModelOutputPreview>>> = ModelService.readAll(auth).toResponseEntity()

    @RequestMapping("/read/by/{id}", method = [RequestMethod.POST])
    @ApiOperation("Get info about all models available to this account by a certain user")
    fun readBy(@RequestBody auth : AuthorizedAction<Int>, @PathVariable id: Int): ResponseEntity<MessageWithItem<ArrayList<ModelOutputPreview>>> = ModelService.readBy(auth, id).toResponseEntity()

    @RequestMapping("/read/{id}", method = [RequestMethod.POST])
    @ApiOperation("Download model if it is visible to current account")
    fun read(@RequestBody auth : AuthorizedAction<Int>, @PathVariable id: Int): ResponseEntity<MessageWithItem<ModelOutput?>> = ModelService.read(auth, id).toResponseEntity()
}