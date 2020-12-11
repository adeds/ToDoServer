package id.ade.repository

import id.ade.databse.DatabaseFactory.dbQuery
import id.ade.models.Todo
import id.ade.models.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement

class TodoRepository : Repository {
    override suspend fun addUser(
        email: String,
        displayName: String,
        passwordHash: String
    ): User? {
        var statement: InsertStatement<Number>? = null // An Exposed class that helps with inserting data.

        dbQuery { // A helper function, defined earlier, that inserts a new User record.

            // Uses the insert method from the Users parent class to insert a new record.
            statement = Users.insert { user ->
                user[Users.email] = email
                user[Users.displayName] = displayName
                user[Users.passwordHash] = passwordHash
            }
        }
        // A private function required to convert the Exposed ResultRow to your User class.
        return rowToUser(statement?.resultedValues?.get(0))
    }

    override suspend fun findUser(userId: Int) = dbQuery {
        Users.select { Users.userId.eq(userId) }
            .map { rowToUser(it) }.singleOrNull()
    }

    override suspend fun findUserByEmail(email: String) = dbQuery {
        Users.select { Users.email.eq(email) }
            .map { rowToUser(it) }.singleOrNull()
    }

    override suspend fun isUserExist(email: String) =
        dbQuery {
            Users.select { Users.email.eq(email) }
                .map { rowToUser(it) }.isNullOrEmpty().not()
        }

    // Defines addTodo, which takes a user ID
    override suspend fun addTodo(userId: Int, todo: String, done: Boolean): Todo? {
        var statement: InsertStatement<Number>? = null
        dbQuery {
            statement = Todos.insert {
                it[Todos.userId] = userId
                it[Todos.todo] = todo
                it[Todos.done] = done
            }
        }
        return rowToTodo(statement?.resultedValues?.get(0))
    }

    override suspend fun changeTodo(todoId: Int, done: Boolean): Todo? {
        return dbQuery {
            Todos.update({ Todos.id.eq(todoId) }) {
                it[this.done] = done
            }
            Todos.select { Todos.id eq todoId }.map { rowToTodo(it) }.singleOrNull()
        }
    }

    // Defines the method to get all TODOs for a given user ID.
    override suspend fun getTodos(userId: Int): List<Todo> {
        return dbQuery {
            Todos.select {

                //Note how getTodos uses eq to find a user that matches the user ID
                Todos.userId.eq((userId))
            }
                .orderBy(Todos.id to SortOrder.ASC)
                .mapNotNull { rowToTodo(it) }
        }
    }

    override suspend fun deleteTodo(todoId: Int): Boolean {
        return dbQuery {
            Todos.deleteWhere(op = { Todos.id.eq(todoId) })
            Todos.select { Todos.id eq todoId }.map { rowToTodo(it) }.isNullOrEmpty()
        }
    }

    // Defines a helper function to convert an Exposed ResultRow to your TODO class.
    private fun rowToTodo(row: ResultRow?): Todo? {
        if (row == null) {
            return null
        }
        return Todo(
            id = row[Todos.id],
            userId = row[Todos.userId],
            todo = row[Todos.todo],
            done = row[Todos.done]
        )
    }

    private fun rowToUser(row: ResultRow?): User? {
        if (row == null) {
            return null
        }
        return User(
            userId = row[Users.userId],
            email = row[Users.email],
            displayName = row[Users.displayName],
            passwordHash = row[Users.passwordHash]
        )
    }

}
