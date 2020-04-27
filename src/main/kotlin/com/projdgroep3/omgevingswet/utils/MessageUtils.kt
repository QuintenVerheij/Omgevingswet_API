package com.projdgroep3.omgevingswet.utils

import com.projdgroep3.omgevingswet.models.auth.AuthorizationType
import com.projdgroep3.omgevingswet.models.misc.Message
import com.projdgroep3.omgevingswet.models.misc.MessageType

object MessageUtils {

    fun execute(
            targetId: Int,
            userId: Int,
            authType: AuthorizationType,
            execute: () -> Unit
    ): Message = try {
        execute()
        Message(true, MessageType.NONE, authType, null, targetId, userId)
    } catch (e: Exception) {
        e.printStackTrace()
        //TODO(Implement error logging)
        Message(false, MessageType.EXCEPTION, authType, e.message, targetId, userId)
    }

    fun chainValidate(
            before: List<(Message) -> Message>
    ): Message = chain(-1, AuthorizationType.INFO, null, before)

    fun chain(
            userId: Int,
            authType: AuthorizationType,
            before: List<(Message) -> Message>?,
            after: List<(Message) -> Message>
    ): Message {
        if(after.isEmpty()) {
            throw Exception("Cannot chain empty list of messages")
        }

        //Default message, should not be returned or used by the first after in the chain.
        //Executes the required before for the chain.
        var message = before?.let {
            val msg = chain(userId, authType, null, before)
            if (!msg.successful) return msg
            msg
        } ?: Message.successful(userId, authType)

        //Execute the rest of the chain
        for(func in after) {
            //Execute the function and if it wasn't successful return the message with the error.
            val msg = func(message)
            if(!msg.successful) return msg

            //If successful, overwrite message and continue chain
            message = msg
        }

        return message
    }

}