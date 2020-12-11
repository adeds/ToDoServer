package id.ade.util

class Constant {

    object General{
        const val PROJECT_NAME = "todoServer"
    }

    object DatabaseKey {
        object User {
            const val KEY_ID = "id"
            const val KEY_EMAIL = "email"
            const val KEY_DISPLAY_NAME = "display_name"
            const val KEY_PASSWORD_HASH = "password_hash"
        }

        object Todo {
            const val KEY_ID = "id"
            const val KEY_USER_ID = "userId"
            const val KEY_TODO = "todo"
            const val KEY_DONE = "done"
        }
    }

    object Environment {
        object DataBase {
            const val JDBC_DRIVER = "JDBC_DRIVER"
            const val JDBC_DATABASE_URL = "JDBC_DATABASE_URL"
            const val TRANSACTION_REPEATABLE_READ = "TRANSACTION_REPEATABLE_READ"

            // doesn't use in local, only when deployed
            const val DB_NAME = "DB_USER"
            const val DB_PASSWORD = "DB_PASSWORD"
        }

        object Auth {
            const val SECRET_KEY = "SECRET_KEY"
            const val HASH_ALGORITH = "HmacSHA1"
            const val JWT_SECRET = "JWT_SECRET"
            const val AUTHENTICATION = "Authentication"
        }
    }
}