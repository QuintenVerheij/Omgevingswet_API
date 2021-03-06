package com.projdgroep3.omgevingswet.controller


import UserAddAddressInput
import UserCreateInput
import UserOutput
import UserOutputPublic
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
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.ServletContextAware
import org.springframework.web.multipart.MultipartFile
import javax.servlet.ServletContext

@CrossOrigin
@RestController
@RequestMapping("/user")
@Api(tags = ["User"], description = "CRUD for users")
class UserController {
    /*
    Create
     */
    @RequestMapping("/create", method = [RequestMethod.POST])
    @ApiOperation("Create new user")
    fun createUser(
            @RequestBody user: UserCreateInput): ResponseEntity<Message> = UserService.createUser(
            AuthorizedAction<UserCreateInput>(
                    AuthorizationTokenService.getInternalToken(AuthorizationRole.USER_CREATE),
                    user
            )).toResponseEntity()

    @RequestMapping("/create/address", method = [RequestMethod.PUT])
    @ApiOperation("Associate address with user")
    fun addAddress(
            @RequestBody input: AuthorizedAction<UserAddAddressInput>): ResponseEntity<Message> = UserService.addAddress(input).toResponseEntity()


//    @RequestMapping("/read", method=[RequestMethod.GET])
//    fun readUser(): ResponseEntity<List<UserOutput>> = UserService.readAll().toResponseEntity()

    @RequestMapping("/read", method = [RequestMethod.POST])
    @ApiOperation("Read user by id")
    fun readUser(@RequestBody input: AuthorizedAction<Int>): ResponseEntity<MessageWithItem<UserOutput>> = UserService.readUser(input).toResponseEntity()

    @RequestMapping("/other/read/{id}", method = [RequestMethod.GET])
    @ApiOperation("Read public info of user by id")
    fun readUser(@PathVariable id: String): ResponseEntity<MessageWithItem<UserOutputPublic>> {
        try{
            id.toInt()
        }catch (e: NumberFormatException){
            return MessageWithItem(
                    Message(
                            successful = false,
                            messageType = MessageType.EXCEPTION,
                            authorizationType = AuthorizationType.READ,
                            message = "Unable to parse input",
                            targetId = -1,
                            userId = -1
                    ), null as UserOutputPublic?
            ).toResponseEntity()
        }
        return UserService.readOtherUser(id.toInt()).toResponseEntity()
    }

    @RequestMapping("/img/{id}", method = [RequestMethod.GET], produces = arrayOf(
            MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE))
    @ApiOperation("Read profile image of user by id")
    fun getImage(@PathVariable id: String): ResponseEntity<ByteArray>{
        try{
            id.toInt()
        }catch (e: NumberFormatException){
            return ByteArray(0).toResponseEntity()
        }
        return UserService.serveImage(id.toInt()).toResponseEntity()
    }

    @RequestMapping("/img/upload", method = [RequestMethod.POST])
    @ApiOperation("Upload profile image and associate with user by id")
    fun setImage(@RequestParam auth: AuthorizationToken, @RequestParam input: Int, @RequestParam file: MultipartFile): ResponseEntity<Message>{
        if(file.isEmpty){
            return Message(
                    successful = false,
                    messageType = MessageType.EXCEPTION,
                    authorizationType = AuthorizationType.UPDATE,
                    message = "Please select a file to upload",
                    targetId = -1,
                    userId = -1
            ).toResponseEntity()
        }
        return UserService.storeImage(AuthorizedAction(auth, input), file).toResponseEntity()
    }


}