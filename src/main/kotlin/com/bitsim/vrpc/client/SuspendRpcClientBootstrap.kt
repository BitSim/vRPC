package com.bitsim.vrpc.client

import com.bitsim.vrpc.config.Config
import com.bitsim.vrpc.constant.RegistryConstant
import com.bitsim.vrpc.entity.RemoteServiceInfo
import com.bitsim.vrpc.registry.Registry

/**
 *@author  BitSim
 *@version  v1.0.0

 **/
object SuspendRpcClientBootstrap {
    private val proxyFactory = RpcProxyFactory()
    private val clientMap = mutableMapOf<String, VertTcpClient>()
    private val serviceRegistry: Registry

    /**
     * 初始化RPC客户端
     */
    init {
        Config.loadConfig()
        serviceRegistry = Registry.Factory(Config.getProperty(RegistryConstant.REGISTRY_TYPE)!!)
    }

    /**
     * 获取服务代理实例
     * @param serviceClass 服务接口类
     * @return 服务代理实例
     */
    suspend fun <T> getService(serviceClass: Class<T>, version: String): T {
        val serviceName = serviceClass.name
        // 从注册中心获取服务地址
        val discovery: List<RemoteServiceInfo> = serviceRegistry.discovery("$serviceName:$version")
        if (discovery.isEmpty()) {
            throw RuntimeException("No provider available for service: $serviceName")
        }
        val host = discovery[0].serviceHost
        val port = discovery[0].servicePort

        // 获取或创建对应的客户端连接
        val client = clientMap.getOrPut("$host:$port") {
            VertTcpClient(host, port).apply {
                connect()
            }
        }

        proxyFactory.setRpcClient(client)
        return proxyFactory.createProxy(serviceClass,version)
    }

    /**
     * 使用内联函数方式获取服务代理实例
     * @return 服务代理实例
     */
    suspend inline fun <reified T> getService(version: String): T {
        return getService(T::class.java, version)
    }

    /**
     * 关闭所有客户端连接
     */
    fun shutdown() {
        clientMap.values.forEach { it.close() }
        clientMap.clear()
    }

}