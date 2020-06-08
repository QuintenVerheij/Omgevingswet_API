package com.projdgroep3.omgevingswet.models.auth

import com.projdgroep3.omgevingswet.service.Identifiable

enum class AuthorizationRole(
        val identifier: String,
        val id: Int,
        val isPublic: Boolean,
        val skipUserCheck: Boolean,

        val allowedInfo: List<AuthorizationActionType.Info>,
        val allowedCreate: List<AuthorizationActionType.Create>,
        val allowedRead: List<AuthorizationActionType.Read>,
        val allowedUpdate: List<AuthorizationActionType.Update>,
        val allowedDelete: List<AuthorizationActionType.Delete>

) : Identifiable {
    USER(
            "user",
            0,
            true,
            false,
            listOf(
                    //TODO(Implement info permissions)
            ),
            listOf(

            ),
            listOf(
                    AuthorizationActionType.Read.USER,
                    AuthorizationActionType.Read.ADDRESS
            ),
            listOf(
                    AuthorizationActionType.Update.USER,
                    AuthorizationActionType.Update.ADDRESS
            ),
            listOf(
                    AuthorizationActionType.Delete.USER,
                    AuthorizationActionType.Delete.ADDRESS
            )

    ) {
        override fun getIdentifier(): Int = id
    },

    PRIVATE_USER(
            "private_user",
            1,
            false,
            false,
            listOf(
                    //TODO(Implement info permissions)
            ),
            listOf(
                    //TODO(Implement create permissions)
            ),
            listOf(
                    //TODO(Implement read permissions)
            ),
            listOf(
                    //TODO(Implement update permissions)
            ),
            listOf(
                    //TODO(Implement delete permissions)
            )
    ) {
        override fun getIdentifier(): Int = id
    },
    AUTH_READ(
            "auth_read",
            100,
            true,
            true,
            emptyList(),
            emptyList(),
            listOf(
                    AuthorizationActionType.Read.AUTH
            ),
            emptyList(),
            emptyList()
    ) {
        override fun getIdentifier(): Int = id
    },

    USER_CREATE(
            "user_create",
            101,
            true,
            true,
            emptyList(),
            listOf(
                    AuthorizationActionType.Create.ADDRESS,
                    AuthorizationActionType.Create.USER
            ),
            emptyList(),
            emptyList(),
            emptyList()
    ) {
        override fun getIdentifier(): Int = id
    },

    ADMIN(
            "admin",
            -1,
            false,
            false,
            AuthorizationActionType.Info.values().toList(),
            AuthorizationActionType.Create.values().toList(),
            AuthorizationActionType.Read.values().toList(),
            AuthorizationActionType.Update.values().toList(),
            AuthorizationActionType.Delete.values().toList()
    ) {
        override fun getIdentifier(): Int = id

    }


}

enum class AuthorizationType {
    INFO,
    CREATE,
    READ,
    UPDATE,
    DELETE
}


object AuthorizationActionType {
    interface IAuthorizationActionType {
        fun getAuthorizationType(): AuthorizationType
    }

    enum class Info : IAuthorizationActionType {
        AUTH,

        LOG,

        USER_MAIL,

        //TODO(ADD INFO ACTION TYPES)
        ;

        override fun getAuthorizationType() = AuthorizationType.INFO
    }

    enum class Create : IAuthorizationActionType {
        AUTH,

        ADDRESS,
        USER,
        //TODO(ADD CREATE ACTION TYPES)
        ;

        override fun getAuthorizationType() = AuthorizationType.CREATE
    }

    enum class Read : IAuthorizationActionType {
        AUTH,

        ADDRESS,
        USER,
        //TODO(ADD READ ACTION TYPES)
        ;

        override fun getAuthorizationType() = AuthorizationType.READ
    }

    enum class Update : IAuthorizationActionType {
        AUTH,

        ADDRESS,
        USER,
        //TODO(ADD UPDATE ACTION TYPES)
        ;

        override fun getAuthorizationType() = AuthorizationType.UPDATE
    }

    enum class Delete : IAuthorizationActionType {
        AUTH,

        ADDRESS,
        USER,
        //TODO(ADD DELETE ACTION TYPES)
        ;

        override fun getAuthorizationType() = AuthorizationType.DELETE
    }
}