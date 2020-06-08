package com.projdgroep3.omgevingswet.models.misc

data class MessageWithItem<T>(
    val message: Message,
    val item: T?
)