package com.bitsim.vrpc.util

import com.bitsim.vrpc.entity.RemoteServiceInfo
import com.bitsim.vrpc.serializer.Serializer
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap


/**
 *@author  BitSim
 *@version  v1.0.0

 **/
class SPIUtil {
    companion object {
        private const val SPI_PREFIX = "META-INF/vrpc/"
        private const val DEFAULT_SERVICES = SPI_PREFIX + "default/"
        private const val USER_SERVICES = SPI_PREFIX + "custom/"
        private val SERVICES: List<String> = listOf(DEFAULT_SERVICES, USER_SERVICES)
        private val log = KotlinLogging.logger {}
        // 区分默认和用户自定义
        private val loaderMap: MutableMap<String, MutableMap<String, Class<*>>> = ConcurrentHashMap()
        fun <T> load(service: Class<T>) {
            val serviceName = service.name
            val loader = loaderMap.computeIfAbsent(serviceName) { mutableMapOf() }
            for (resourcePath in SERVICES) {
                // 读取配置文件
                val resource = ClassLoader.getSystemClassLoader().getResource(resourcePath + serviceName)
                resource?.openStream()?.bufferedReader()?.use {
                    it.lines().forEach { line ->
                        val key = line.split("=")[0]
                        val clazz = Class.forName(line.split("=")[1])
                        if (service.isAssignableFrom(clazz)) {
                            loader[key] = clazz
                        } else {
                            log.info { "class $clazz is not a subclass of $service" }
                        }
                    }
                }


            }
        }

        fun <T> getInstance(service: Class<T>, key: String): T {

            val serviceName = service.name
            val loader = loaderMap[serviceName]
            if (loader == null) {
                log.error { "service $serviceName not found" }
                throw RuntimeException("service $serviceName not found")
            }
            val clazz = loader[key]
            if (clazz == null) {
                log.error { "service $serviceName with key $key not found" }
                throw RuntimeException("service $serviceName with key $key not found")
            }
            @Suppress("UNCHECKED_CAST")
            return if (clazz.kotlin.objectInstance != null) {
                clazz.kotlin.objectInstance as T
            } else {
                clazz.getDeclaredConstructor().newInstance() as T
            }
        }
    }
}
