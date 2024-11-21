package com.bitsim.vrpc.serializer

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 *@author  BitSim
 *@version  v1.0.0
 **/
object JdkSerializer: Serializer {
    override fun <T> serialize(data: T): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(data)
        objectOutputStream.flush()
        return byteArrayOutputStream.toByteArray()
    }

    override fun <T> deserialize(data: ByteArray, clazz: Class<T>): T {
        val byteArrayInputStream = ByteArrayInputStream(data)
        val objectInputStream = ObjectInputStream(byteArrayInputStream)
        return clazz.cast(objectInputStream.readObject())
    }
}