package id.ade

import id.ade.auth.JwtService
import id.ade.auth.MySession
import id.ade.auth.hash
import id.ade.databse.DatabaseFactory
import id.ade.repository.TodoRepository
import id.ade.routes.users
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.locations.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlin.collections.set

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    // 1
    DatabaseFactory.init()
    val db = TodoRepository()
// 2
    val jwtService = JwtService()
    val hashFunction = { s: String -> hash(s) }


    install(Locations) {
    }

    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(Authentication) {
        jwt("jwt") {

            //specifies the verifier you created in the JwtService class.
            verifier(jwtService.verifier)
            realm = "Todo Server"

            //creates a method that runs each time the app needs to authenticate a call.
            validate {
                val payload = it.payload
                val claim = payload.getClaim("id")
                val claimString = claim.asInt()
                val user = db.findUser(claimString)
                /*
                * tries to find the user in the database with the userId from claimString.
                * If the userID exists, it verifies the user.
                * Otherwise, it returns a null user and rejects the route.
                * */
                user
            }
        }

    }

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        users(db, jwtService, hashFunction)
    }
}
