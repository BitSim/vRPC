package com.bitsim.vrpc.registry

import com.bitsim.vrpc.config.Config
import com.bitsim.vrpc.constant.RegistryConstant
import com.bitsim.vrpc.entity.RemoteServiceInfo
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.x.discovery.ServiceDiscovery
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder
import org.apache.curator.x.discovery.ServiceInstance
import org.apache.curator.x.discovery.details.JsonInstanceSerializer


/**
 *@author  BitSim
 *@version  v1.0.0

 **/
object ZookeeperRegistry : Registry {
    private val client = CuratorFrameworkFactory.builder()
        .connectString(Config.getProperty(RegistryConstant.ZOOKEEPER_CONNECT_STRING))
        .retryPolicy(ExponentialBackoffRetry(1000, 3))
        .build()
    private val serviceDiscovery: ServiceDiscovery<RemoteServiceInfo>
    private val remoteServiceInfoMap = mutableMapOf<String, List<RemoteServiceInfo>>()

    init {
        client.start()
        serviceDiscovery = ServiceDiscoveryBuilder.builder(RemoteServiceInfo::class.java)
            .client(client)
            .basePath("/vrpc")
            .serializer(JsonInstanceSerializer(RemoteServiceInfo::class.java))
            .build()
        serviceDiscovery.start()
    }

    override fun register(remoteServiceInfo: RemoteServiceInfo) {
        val serviceInstance = ServiceInstance.builder<RemoteServiceInfo>()
            .name(remoteServiceInfo.serviceKey)
            .address(remoteServiceInfo.serviceHost)
            .port(remoteServiceInfo.servicePort)
            .payload(remoteServiceInfo)
            .build()
        serviceDiscovery.registerService(serviceInstance)
    }

    override fun unRegister(remoteServiceInfo: RemoteServiceInfo) {
        // 获取已注册的实例
        val instances = serviceDiscovery.queryForInstances(remoteServiceInfo.serviceKey)
        val instanceToUnregister = instances.find {
            it.address == remoteServiceInfo.serviceHost && it.port == remoteServiceInfo.servicePort
        }
        // 如果匹配到，则注销服务
        instanceToUnregister?.let {
            serviceDiscovery.unregisterService(it)
        } ?: println("Service instance not found for ${remoteServiceInfo.serviceKey}")
        // 更新本地缓存
        remoteServiceInfoMap[remoteServiceInfo.serviceKey]?.let { list ->
            remoteServiceInfoMap[remoteServiceInfo.serviceKey] = list.filterNot {
                it.serviceHost == remoteServiceInfo.serviceHost && it.servicePort == remoteServiceInfo.servicePort
            }
        }
    }

    override fun discovery(serviceKey: String): List<RemoteServiceInfo> {
        if(remoteServiceInfoMap.containsKey(serviceKey)){
            return remoteServiceInfoMap[serviceKey]!!
        }
        val instances = serviceDiscovery.queryForInstances(serviceKey)
        return instances.map { it.payload }
    }

    override fun shutdown() {
        for ((_, instances) in remoteServiceInfoMap) {
            for (instance in instances) {
                println("Unregister service: $instance")
                unRegister(instance)
            }
        }
    }
}


