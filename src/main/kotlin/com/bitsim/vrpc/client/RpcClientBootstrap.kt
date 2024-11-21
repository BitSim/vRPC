package com.bitsim.vrpc.client

import com.bitsim.vrpc.config.Config
import com.bitsim.vrpc.constant.RegistryConstant
import com.bitsim.vrpc.entity.RemoteServiceInfo
import com.bitsim.vrpc.registry.Registry
import kotlinx.coroutines.runBlocking

/**
 * RPC客户端引导类
 * 负责初始化RPC客户端环境和提供服务代理获取方法
 *
 * @author BitSim
 * @version v1.0.0
 */


// 同步版本的引导类
object RpcClientBootstrap {
    private val susPendRpcClientBootstrap = SuspendRpcClientBootstrap

    fun <T> getService(serviceClass: Class<T>, version: String): T {
        return runBlocking { susPendRpcClientBootstrap.getService(serviceClass, version) }
    }

    inline fun <reified T> getService(version: String): T {
        return getService(T::class.java, version)
    }

    fun shutdown() {
        susPendRpcClientBootstrap.shutdown()
    }
}

