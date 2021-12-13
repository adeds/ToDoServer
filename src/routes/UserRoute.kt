package id.ade.routes

import id.ade.auth.JwtService
import id.ade.auth.MySession
import id.ade.models.Login
import id.ade.models.Message
import id.ade.repository.Repository
import id.ade.util.Constant.Environment.RoutesKey.DISPLAY_NAME
import id.ade.util.Constant.Environment.RoutesKey.EMAIL
import id.ade.util.Constant.Environment.RoutesKey.PASSWORD
import id.ade.util.toJson
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

private const val API_VERSION = "/v1"
const val USERS = "$API_VERSION/users"
const val USER_LOGIN = "$USERS/login"
const val USER_CREATE = "$USERS/create"

@KtorExperimentalLocationsAPI
@Location(USER_LOGIN)
class UserLoginRoute

@KtorExperimentalLocationsAPI
@Location(USER_CREATE)
class UserCreateRoute


//Defines an extension function to Route named users that takes in a Repository, a JWTService and a hash function.
@KtorExperimentalLocationsAPI
fun Route.users(
    db: Repository,
    jwtService: JwtService,
    hashFunction: (String) -> String
) {
    //Generates a route for creating a new user.
    post<UserCreateRoute> {

        //Uses the call parameter to get the parameters passed in with the request
        val signupParameters = call.receive<Parameters>()

        //Looks for the password parameter and returns an error if it doesn’t exist.
        val password = signupParameters[PASSWORD]
            ?: return@post call.respond(HttpStatusCode.Unauthorized, Message("Missing Fields").toJson())

        val displayName = signupParameters[DISPLAY_NAME]
            ?: return@post call.respond(HttpStatusCode.Unauthorized, Message("Missing Fields").toJson())

        val email = signupParameters[EMAIL]
            ?: return@post call.respond(HttpStatusCode.Unauthorized, Message("Missing Fields").toJson())

        //Produces a hash string from the password.
        val hash = hashFunction(password) // 5
        try {

            // check if email has been registered
            if (db.isUserExist(email))
                call.respond(HttpStatusCode.BadRequest, Message("Email has been registered").toJson())
            else {
                //Adds a new user to the database.
                val newUser = db.addUser(email, displayName, hash)
                newUser?.userId?.let {
                    call.sessions.set(MySession(it))
                    call.respondText(
                        Login(jwtService.generateToken(newUser)).toJson(),
                        status = HttpStatusCode.Created
                    )
                }
            }
        } catch (e: Throwable) {
            application.log.error("Failed to register user", e)
            call.respond(HttpStatusCode.BadRequest, Message("Problems creating User").toJson())
        }
    }

    post<UserLoginRoute> { // 1
        val signinParameters = call.receive<Parameters>()
        val password = signinParameters[PASSWORD]
            ?: return@post call.respond(HttpStatusCode.Unauthorized, Message("Missing Fields").toJson())

        val email = signinParameters[EMAIL]
            ?: return@post call.respond(HttpStatusCode.Unauthorized, Message("Missing Fields").toJson())

        val hash = hashFunction(password)

        try {
            if (db.isUserExist(email)) {
                val currentUser = db.findUserByEmail(email)
                currentUser?.userId?.let {
                    if (currentUser.passwordHash == hash) {
                        call.sessions.set(MySession(it))
                        call.respondText(Login(token = jwtService.generateToken(currentUser)).toJson())
                    } else {
                        call.respond(HttpStatusCode.NotAcceptable, Message("Wrong Password").toJson())
                    }
                }
            } else {
                call.respond(HttpStatusCode.NotFound, Message("User not found").toJson())
            }
        } catch (e: Throwable) {
            application.log.error("Failed to register user", e)
            call.respond(HttpStatusCode.BadRequest, Message("Problems retrieving User").toJson())
        }
    }

}