package id.ade.util

import com.google.gson.Gson

fun Any.toJson(): String = Gson().toJson(this)