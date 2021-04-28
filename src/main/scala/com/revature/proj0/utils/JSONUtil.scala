package com.revature.proj0.utils

object JSONUtil {
    def readJSON() = {
        val jsonString = os.read(os.pwd/"data"/"games.json")
        val data = ujson.read(jsonString)

        data
    }
}