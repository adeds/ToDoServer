package id.ade.repository

import id.ade.databse.DatabaseFactory.dbQuery
import id.ade.models.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
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
