package com.bitsim.vrpc.serializer

import com.bitsim.vrpc.util.SPIUtil

/**
 *@author  BitSim
 *@version  v1.0.0

 **/
interface Serializer {
    // 方法用泛型
    fun <T> serialize(data: T): ByteArray
    fun <T> deserialize(data: ByteArray, clazz: Class<T>): T


    companion object Factory {
        init {
            SPIUtil.load(Serializer::class.java)
        }
        operator fun invoke(key: String): Serializer {
            return SPIUtil.getInstance(Serializer::class.java, key)
        }
    }
}