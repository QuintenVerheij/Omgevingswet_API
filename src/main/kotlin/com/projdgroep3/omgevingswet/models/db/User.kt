import com.projdgroep3.omgevingswet.models.db.Address
import com.projdgroep3.omgevingswet.models.db.AddressCreateInput
import com.projdgroep3.omgevingswet.models.db.addresses
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

object users : IntIdTable() {
    val username = varchar("username", 50)
    val email = varchar("email", 50)
    val passwordhash = varchar("passwordhash", 512)
    val cryptosalt = varchar("cryptosalt", 128)
    val globalpermission = varchar("globalpermission", 10)
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(users)

    var username by users.username
    var email by users.email
    var passwordhash by users.passwordhash
    var cryptosalt by users.cryptosalt
    var globalpermission by users.globalpermission
    var _address by Address via useraddresses
}

object useraddresses : Table() {
    val userID = reference("userID", users)
    val addressID = reference("addressID", addresses)
    override val primaryKey = PrimaryKey(userID, addressID, name = "PK_useraddresses_U_A")
}

data class UserOutput(
        val id: Int,
        val username: String,
        val email: String,
        val passwordHash: String,
        val address: ArrayList<AddressCreateInput>
)

data class UserOutputNoAddress(
        val username: String,
        val email: String,
        val passwordHash: String
)

data class UserCreateInput(
        val username: String,
        val email: String,
        val passwordHash: String,
        val address: AddressCreateInput
)

data class UserAddAddressInput(
        val userID: Int,
        val addressID: Int
)



