package com.bitsim.vrpc.serializer

import io.vertx.core.json.JsonObject


/**
 *@author  BitSim
 *@version  v1.0.0

 **/
object JsonSerializer : Serializer {
    override fun <T> serialize(data: T): ByteArray {
        return JsonObject.mapFrom(data).encode().toByteArray()
    }

    override fun <T> deserialize(data: ByteArray, clazz: Class<T>): T {
        if (data.isEmpty()) {
            throw IllegalArgumentException("Cannot deserialize empty byte array")
        }
        return JsonObject(String(data)).mapTo(clazz)
    }
}