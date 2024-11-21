package com.bitsim.vrpc.server

import com.bitsim.vrpc.config.Config
import com.bitsim.vrpc.constant.RegistryConstant
import com.bitsim.vrpc.constant.RpcServerConstant
import com.bitsim.vrpc.entity.RemoteServiceInfo
import com.bitsim.vrpc.registry.LocalServiceCache
import com.bitsim.vrpc.registry.Registry

/**
 *@author  BitSim
 *@version  v1.0.0

 **/
object RpcServerBootstrap {
    init {
        Config.loadConfig()
    }

    private val registry = Registry.Factory(Config.getProperty(RegistryConstant.REGISTRY_TYPE)!!)
    private val host = Config.getProperty(RpcServerConstant.SERVICE_HOST)!!
    private val port = Config.getProperty(RpcServerConstant.SERVICE_PORT)!!.toInt()
    fun setup(services: List<Pair<RemoteServiceInfo, Class<*>>>) {
        Config.loadConfig()
        for (service in services) {
            service.first.serviceHost = host
            service.first.servicePort = port
            LocalServiceCache.put(service.first.serviceKey, service.second)
            registry.register(service.first)
        }
    }

    fun start() {
        VertTcpServer.start(port)
    }

    fun shutdown() {
        registry.shutdown()
        VertTcpServer.stop()
    }
}
