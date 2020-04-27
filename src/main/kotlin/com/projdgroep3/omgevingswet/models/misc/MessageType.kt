package com.projdgroep3.omgevingswet.models.misc

enum class MessageType {
    NONE,
    INFO,
    EXCEPTION,

    USERNAME_ALREADY_EXISTS,
    MAIL_ALREADY_USED,

    LOGIN,
    WHOAMI,
    LOGOUT,

    INVALID_LOGIN_ATTEMPT,
    INVALID_CREDENTIALS,
    NOT_AUTHORIZED,
    INVALID_TOKEN,
}

