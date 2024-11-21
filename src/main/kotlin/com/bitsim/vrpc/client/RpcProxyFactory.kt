package com.bitsim.vrpc.client

import com.bitsim.vrpc.entity.RpcRequest
import com.bitsim.vrpc.entity.RpcResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Proxy

/**
 * RPC代理工厂类
 * 负责为服务接口创建动态代理实例，将接口方法调用转换为RPC请求
 */
class RpcProxyFactory {
    private lateinit var vertTcpClient: VertTcpClient
    
    /**
     * 设置RPC客户端实例
     */
    fun setRpcClient(client: VertTcpClient) {
        this.vertTcpClient = client
    }
    
    /**
     * 创建服务接口的代理实例
     * @param serviceClass 服务接口类
     * @return 代理实例
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> createProxy(serviceClass: Class<T>,version:String): T {
        return Proxy.newProxyInstance(
            serviceClass.classLoader,
            arrayOf(serviceClass)
        ) { _, method, args ->
            // 使用runBlocking在协程中执行suspend函数
            runBlocking {
                val request = RpcRequest(
                    rid = "req:${System.currentTimeMillis()}",
                    version = version,
                    serviceName = serviceClass.name,
                    methodName = method.name,
                    parameterTypes = method.parameterTypes.toList(),
                    parameters = args?.toList() ?: emptyList()
                )
                // 发送RPC请求并获取响应
                val response = vertTcpClient.send(request)
                log.info { "Response details: data=${response.data}, error=${response.error}" }
                // 处理响应结果
                when {
                    response.success -> response.data
                    response.error != null -> throw response.error
                    else -> throw RuntimeException("RPC调用失败")
                }
            }
        } as T
    }
    companion object {
        private val log = KotlinLogging.logger {}
    }
} 