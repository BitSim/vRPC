package com.bitsim.vrpc.registry

import com.bitsim.vrpc.entity.RemoteServiceInfo
import com.bitsim.vrpc.util.SPIUtil

/**
 *@author  BitSim
 *@version  v1.0.0

 **/
interface Registry {
    fun register(remoteServiceInfo: RemoteServiceInfo)
    fun unRegister(remoteServiceInfo: RemoteServiceInfo)
    fun discovery(serviceName: String): List<RemoteServiceInfo>
    fun shutdown()
    companion object Factory {
        init {
            SPIUtil.load(Registry::class.java)
        }
        operator fun invoke(key: String): Registry {
            return SPIUtil.getInstance(Registry::class.java, key)
        }
    }
}