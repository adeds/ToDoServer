package id.ade.routes

import id.ade.auth.MySession
import id.ade.models.Message
import id.ade.models.User
import id.ade.repository.Repository
import id.ade.util.Constant.Environment.RoutesKey.DONE
import id.ade.util.Constant.Environment.RoutesKey.ID
import id.ade.util.Constant.Environment.RoutesKey.TODO
import id.ade.util.toJson
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.*
import io.ktor.util.pipeline.*

private const val API_VERSION = "/v1"
const val TODOS = "$API_VERSION/todos"

@KtorExperimentalLocationsAPI
@Location(TODOS)
class TodoRoute

@OptIn(InternalAPI::class)
@KtorExperimentalLocationsAPI
fun Route.todos(db: Repository) {

//    Uses the authenticate extension function to tell the system you want to authenticate these routes.
    authenticate(configurations = arrayOf("jwt")) {
        post<TodoRoute> {
            val todosParameters = call.receive<Parameters>()

            val todo = todosParameters[TODO]
                ?: return@post call.respond(HttpStatusCode.BadRequest, Message("Missing Todo").toJson())

            val done = todosParameters[DONE] ?: "false"

            // Checks if the user has a session. If not, it returns an error.
            val user = getUser(db)
            if (user == null) {
                call.respond(
                    HttpStatusCode.BadRequest, Message("Problems retrieving User").toJson()
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
                call.respond(HttpStatusCode.BadRequest, Message("Problems Saving Todo").toJson())
            }
        }

        get<TodoRoute> {
            val user = call.sessions.get<MySession>()?.let { db.findUser(it.userId) }
            if (user == null) {
                call.respond(HttpStatusCode.BadRequest, Message("Problems retrieving User").toJson())
                return@get
            }
            getCurrentList(db, user)
        }

        put<TodoRoute> {
            val todosParameters = call.receive<Parameters>()

            val id = todosParameters[ID]
                ?: return@put call.respond(HttpStatusCode.BadRequest, Message("Missing Todo ID").toJson())

            val done = todosParameters[DONE] ?: "false"

            val user = getUser(db)
            if (user == null) {
                call.respond(
                    HttpStatusCode.BadRequest, Message("Problems retrieving User").toJson()
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
                call.respond(HttpStatusCode.BadRequest, Message("Problems Update Todo").toJson())
            }
        }

        delete<TodoRoute> {
            val todosParameters = call.receive<Parameters>()

            val id = todosParameters[ID]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, Message("Missing Todo ID").toJson())

            val user = getUser(db)
            if (user == null) {
                call.respond(
                    HttpStatusCode.BadRequest, Message("Problems retrieving User").toJson()
                )
                return@delete
            }

            try {
                val isDeleted = db.deleteTodo(id.toInt())
                if (isDeleted) {
                    getCurrentList(db, user)
                } else {
                    call.respond(HttpStatusCode.NotFound, Message("Id not found").toJson())
                }
            } catch (e: Throwable) {
                application.log.error("Failed to update todo", e)
                call.respond(HttpStatusCode.BadRequest, Message("Problems Update Todo").toJson())
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
        call.respond(HttpStatusCode.BadRequest, Message("Problems getting Todos").toJson())
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.getUser(db: Repository): User? {
    call.sessions.get<MySession>()?.let {
        return db.findUser(it.userId)
    }
    return null
}
