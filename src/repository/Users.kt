package id.ade.repository

import id.ade.util.Constant
import id.ade.util.Constant.DatabaseKey.User.KEY_DISPLAY_NAME
import id.ade.util.Constant.DatabaseKey.User.KEY_EMAIL
import id.ade.util.Constant.DatabaseKey.User.KEY_ID
import id.ade.util.Constant.DatabaseKey.User.KEY_PASSWORD_HASH
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val userId: Column<Int> = integer(KEY_ID).autoIncrement().primaryKey()
    val email = varchar(KEY_EMAIL, 128).uniqueIndex()
    val displayName = varchar(KEY_DISPLAY_NAME, 256)
    val passwordHash = varchar(KEY_PASSWORD_HASH, 64)
}
