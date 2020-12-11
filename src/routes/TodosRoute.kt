package id.ade.routes

import id.ade.auth.MySession
import id.ade.models.User
import id.ade.repository.Repository
import id.ade.util.Constant.Environment.RoutesKey.TODO
import id.ade.util.Constant.Environment.RoutesKey.DONE
import id.ade.util.Constant.Environment.RoutesKey.ID
import io.ktor.application.*
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.locations.*
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.util.pipeline.*

private const val API_VERSION = "/v1"
const val TODOS = "$API_VERSION/todos"

@KtorExperimentalLocationsAPI
@Location(TODOS)
class TodoRoute

@KtorExperimentalLocationsAPI
fun Route.todos(db: Repository) {

//    Uses the authenticate extension function to tell the system you want to authenticate these routes.
    authenticate("jwt") {

//        Defines the new TODO route
        post<TodoRoute> {
            val todosParameters = call.receive<Parameters>()

            val todo = todosParameters[TODO]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing Todo")

            val done = todosParameters[DONE] ?: "false"

            // Checks if the user has a session. If not, it returns an error.
            val user = getUser(db)
            if (user == null) {
                call.respond(
                    HttpStatusCode.BadRequest, "Problems retrieving User"
                )
                return@post
            }

            try {
                // Adds the TODO to the database
                val currentTodo = db.addTodo(
                    user.userId, todo, done.toBoolean()
                )
                currentTodo?.id?.let {
                    call.respond(HttpStatusCode.OK, currentTodo)
                }
            } catch (e: Throwable) {
                application.log.error("Failed to add todo", e)
                call.respond(HttpStatusCode.BadRequest, "Problems Saving Todo")
            }
        }

        get<TodoRoute> {
            val user = call.sessions.get<MySession>()?.let { db.findUser(it.userId) }
            if (user == null) {
                call.respond(HttpStatusCode.BadRequest, "Problems retrieving User")
                return@get
            }
            getCurrentList(db, user)
        }

        put<TodoRoute> {
            val todosParameters = call.receive<Parameters>()

            val id = todosParameters[ID]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing Todo ID")

            val done = todosParameters[DONE] ?: "false"

            val user = getUser(db)
            if (user == null) {
                call.respond(
                    HttpStatusCode.BadRequest, "Problems retrieving User"
                )
                return@put
            }

            try {
                val currentTodo = db.changeTodo(
                    id.toInt(), done.toBoolean()
                )
                currentTodo?.id?.let {
                    getCurrentList(db, user)
                }
            } catch (e: Throwable) {
                application.log.error("Failed to update todo", e)
                call.respond(HttpStatusCode.BadRequest, "Problems Update Todo")
            }
        }

        delete<TodoRoute> {
            val todosParameters = call.receive<Parameters>()

            val id = todosParameters[ID]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing Todo ID")

            val user = getUser(db)
            if (user == null) {
                call.respond(
                    HttpStatusCode.BadRequest, "Problems retrieving User"
                )
                return@delete
            }

            try {
                val isDeleted = db.deleteTodo(id.toInt())
                if (isDeleted) {
                    getCurrentList(db, user)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Id not found")
                }
            } catch (e: Throwable) {
                application.log.error("Failed to update todo", e)
                call.respond(HttpStatusCode.BadRequest, "Problems Update Todo")
            }
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.getCurrentList(
    db: Repository,
    user: User
) {
    try {
        val todos = db.getTodos(user.userId)
        call.respond(todos)
    } catch (e: Throwable) {
        application.log.error("Failed to get Todos", e)
        call.respond(HttpStatusCode.BadRequest, "Problems getting Todos")
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.getUser(db: Repository): User? {
    call.sessions.get<MySession>()?.let {
        return db.findUser(it.userId)
    }
    return null
}
