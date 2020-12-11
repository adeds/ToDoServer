package id.ade.repository

import id.ade.util.Constant.DatabaseKey.Todo.KEY_DONE
import id.ade.util.Constant.DatabaseKey.Todo.KEY_ID
import id.ade.util.Constant.DatabaseKey.Todo.KEY_TODO
import id.ade.util.Constant.DatabaseKey.Todo.KEY_USER_ID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Todos : Table() {
    val id: Column<Int> = integer(KEY_ID).autoIncrement().primaryKey()
    val userId: Column<Int> = integer(KEY_USER_ID).references(Users.userId)
    val todo = varchar(KEY_TODO, 512)
    val done = bool(KEY_DONE)
}
