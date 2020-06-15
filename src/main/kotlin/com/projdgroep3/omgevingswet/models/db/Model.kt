package com.projdgroep3.omgevingswet.models.db

import com.projdgroep3.omgevingswet.models.auth.AuthorizationToken
import com.projdgroep3.omgevingswet.service.Identifiable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import users
import java.math.BigDecimal

object models: IntIdTable() {
    val userId = reference("userID", users)
    val public = bool("public")
    val visibleRange = integer("visibleRange")
    val longitude = decimal("longitude", 8, 5)
    val latitude = decimal("latitiude", 8,5)
    val createdAt = varchar("createdAt",10)
}

class Model(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Model>(models)

    val userId by models.userId
    val public by models.public
    val visibleRange by models.visibleRange
    val longitude by models.longitude
    val latitude by models.latitude
    val createdAt by models.createdAt
}

data class ModelOutputPreview(
    val id: Int,
    val userId: Int,
    val public: Boolean,
    val visibleRange: Int,
    val longitude: BigDecimal,
    val latitude: BigDecimal,
    val createdAt: String,

    val preview: ByteArray?
):Identifiable {
    override fun getIdentifier(): Int = id

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModelOutputPreview

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (public != other.public) return false
        if (visibleRange != other.visibleRange) return false
        if (longitude != other.longitude) return false
        if (latitude != other.latitude) return false
        if (createdAt != other.createdAt) return false
        if (preview != null) {
            if (other.preview == null) return false
            if (!preview.contentEquals(other.preview)) return false
        } else if (other.preview != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + userId
        result = 31 * result + public.hashCode()
        result = 31 * result + visibleRange
        result = 31 * result + longitude.hashCode()
        result = 31 * result + latitude.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + (preview?.contentHashCode() ?: 0)
        return result
    }
}

data class ModelOutput(
        val id: Int,
        val userId: Int,
        val public: Boolean,
        val visibleRange: Int,
        val longitude: BigDecimal,
        val latitude: BigDecimal,
        val createdAt: String,

        val model: ByteArray?,
        val json: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModelOutput

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (public != other.public) return false
        if (visibleRange != other.visibleRange) return false
        if (longitude != other.longitude) return false
        if (latitude != other.latitude) return false
        if (createdAt != other.createdAt) return false
        if (model != null) {
            if (other.model == null) return false
            if (!model.contentEquals(other.model)) return false
        } else if (other.model != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + userId
        result = 31 * result + public.hashCode()
        result = 31 * result + visibleRange
        result = 31 * result + longitude.hashCode()
        result = 31 * result + latitude.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + (model?.contentHashCode() ?: 0)
        return result
    }
}

data class ModelCreateInput(
        val token: AuthorizationToken,
        val userId: Int,
        val public: Boolean,
        val visibleRange: Int,
        val longitude: BigDecimal,
        val latitude: BigDecimal
)

data class ModelUpdateFiles(
        val preview: ByteArray,
        val model: ByteArray
)



