package id.ade.repository

import id.ade.models.Todo
import id.ade.models.User

interface Repository {
    suspend fun addUser(
        email: String,
        displayName: String,
        passwordHash: String
    ): User?

    suspend fun findUser(userId: Int): User?
    suspend fun findUserByEmail(email: String): User?
    suspend fun isUserExist(email: String): Boolean

    suspend fun addTodo(userId: Int, todo: String, done: Boolean): Todo?
    suspend fun changeTodo(todoId: Int, done: Boolean): Todo?
    suspend fun deleteTodo(todoId: Int): Boolean
    suspend fun getTodos(userId: Int): List<Todo>

}
