package com.projdgroep3.omgevingswet.models.auth

import com.projdgroep3.omgevingswet.service.Identifiable

enum class AuthorizationRole(
    val id: String,
    val isPublic: Boolean,

    val allowedInfo: List<AuthorizationActionType.Info>,
    val allowedCreate: List<AuthorizationActionType.Create>,
    val allowedRead: List<AuthorizationActionType.Read>,
    val allowedUpdate: List<AuthorizationActionType.Update>,
    val allowedDelete: List<AuthorizationActionType.Delete>

): Identifiable {
    USER(
            "",
            true,
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
        override fun getIdentifier(): String = id
    },

    PRIVATE_USER(
            "",
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
        override fun getIdentifier(): String = id
    },

    ADMIN(
            "",
            false,
            AuthorizationActionType.Info.values().toList(),
            AuthorizationActionType.Create.values().toList(),
            AuthorizationActionType.Read.values().toList(),
            AuthorizationActionType.Update.values().toList(),
            AuthorizationActionType.Delete.values().toList()
    ) {
        override fun getIdentifier(): String = id

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

    enum class Info: IAuthorizationActionType {
        AUTH,

        LOG,

        USER_MAIL,

        //TODO(ADD INFO ACTION TYPES)
        ;

        override fun getAuthorizationType() = AuthorizationType.INFO
    }

    enum class Create: IAuthorizationActionType {
        AUTH,

        ADDRESS,
        //TODO(ADD CREATE ACTION TYPES)
        ;

        override fun getAuthorizationType() = AuthorizationType.CREATE
    }

    enum class Read: IAuthorizationActionType {
        AUTH,

        ADDRESS,
        //TODO(ADD READ ACTION TYPES)
        ;

        override fun getAuthorizationType() = AuthorizationType.READ
    }

    enum class Update: IAuthorizationActionType {
        AUTH,

        ADDRESS,
        //TODO(ADD UPDATE ACTION TYPES)
        ;

        override fun getAuthorizationType() = AuthorizationType.UPDATE
    }

    enum class Delete: IAuthorizationActionType {
        AUTH,

        ADDRESS,
        //TODO(ADD DELETE ACTION TYPES)
        ;

        override fun getAuthorizationType() = AuthorizationType.DELETE
    }
}