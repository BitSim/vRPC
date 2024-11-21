package com.bitsim.vrpc.registry

/**
 *@author  BitSim
 *@version  v1.0.0

 **/
object LocalServiceCache {
    private val serviceMap: MutableMap<String, Class<*>> = mutableMapOf()
    fun put(serviceName: String, serviceClass: Class<*>) {
        serviceMap[serviceName] = serviceClass
    }
    fun get(serviceName: String): Class<*>? {
        return serviceMap[serviceName]
    }
}