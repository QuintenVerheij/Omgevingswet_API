package com.projdgroep3.omgevingswet.utils

import com.projdgroep3.omgevingswet.config.config
import com.projdgroep3.omgevingswet.config.server
import com.projdgroep3.omgevingswet.models.auth.AuthorizationType
import com.projdgroep3.omgevingswet.models.misc.Message
import com.projdgroep3.omgevingswet.models.misc.MessageType
import com.projdgroep3.omgevingswet.service.auth.AuthorizationTokenService
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object FileUtils {
    fun getFileExtension(filename: String): String {
        var extension = ""
        val i = filename.lastIndexOf('.')
        val p = filename.lastIndexOf('/').coerceAtLeast(filename.lastIndexOf('\\'))
        if(i > p){
            extension = filename.substring(i+1)
        }
        return extension;
    }

    fun ensureFileFormat(filename: String, allowedFileTypes: Array<String>): Boolean {
        val ext = getFileExtension(filename)
        if(ext !in allowedFileTypes){
            return false
        }
        return true
    }

    fun createUserDirectory(userId: Int) : Path {
        val directoryName = Paths.get(config[server.files.filedir] + "\\" + userId.toString())
        //context.getRealPath("/") + config[server.files.imgdir] + "\\" + it.userId.toString())
        if (!Files.exists(directoryName)) {
            Files.createDirectories(directoryName)
        }
        return directoryName
    }
}