package com.projdgroep3.omgevingswet.models.misc

import com.projdgroep3.omgevingswet.models.auth.AuthorizationType

data class Message(
        val successful: Boolean,
        val messageType: MessageType,
        val authorizationType: AuthorizationType,
        val message: String?,
        val targetId: Int,
        val userId: Int
){
    companion object {
        fun successful(userId: Int, authorizationType: AuthorizationType): Message =
                Message(true, MessageType.NONE, authorizationType, null, -1, userId)

        fun successfulEmpty(): Message = 
                successful(-1, AuthorizationType.INFO)
    }
}