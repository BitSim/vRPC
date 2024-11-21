package com.bitsim.vrpc.server

import com.bitsim.vrpc.entity.RpcRequest
import com.bitsim.vrpc.entity.RpcResponse
import com.bitsim.vrpc.protocal.TcpMessage
import com.bitsim.vrpc.protocal.TcpMessageCodec
import com.bitsim.vrpc.protocal.TcpMessageTypeEnum
import com.bitsim.vrpc.registry.LocalServiceCache
import com.bitsim.vrpc.wrapper.VertBufferWrapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.net.NetSocket
import java.net.SocketException

/**
 * TCP 服务器套接字处理器
 * 负责处理客户端连接和请求
 */
object VertxTcpServerSocketHandler : Handler<NetSocket> {
    private val log = KotlinLogging.logger {}

    override fun handle(socket: NetSocket) {
        // 设置消息处理器
        socket.handler(
            VertBufferWrapper { buffer ->
                    // 解码接收到的消息
                    val decode: TcpMessage<RpcRequest> = TcpMessageCodec.decode(buffer.bytes)
                    val header = decode.header
                    val request = decode.body

                    // 从本地服务缓存中获取服务类
                    val service: Class<*>? = LocalServiceCache.get("${request.serviceName}:${request.version}")
                    if (service == null) {
                        log.error { "Service not found: ${request.serviceName}" }
                        return@VertBufferWrapper
                    }

                    // 执行服务方法
                    val method = service.getMethod(request.methodName, *request.parameterTypes.toTypedArray())
                    val result = method.invoke(
                        service.getDeclaredConstructor().newInstance(),
                        *request.parameters.toTypedArray()
                    )

                    // 构建响应消息
                    val response = RpcResponse(
                        success = true,
                        data = result
                    )
                    val message = TcpMessage(
                        header = header.copy(messageType = TcpMessageTypeEnum.RESPONSE.key),
                        body = response
                    )

                    // 编码并发送响应
                    val encodedData = TcpMessageCodec.encode(message)
                    socket.write(Buffer.buffer(encodedData))

            }
        )

        // 设置异常处理器
        socket.exceptionHandler { e ->
            when (e) {
                is SocketException -> return@exceptionHandler
                else -> log.error(e) { "Error handling request" }
            }
        }

        // 设置关闭处理器
        socket.closeHandler {
            log.info { "Socket closed" }
        }

        // 设置结束处理器
        socket.endHandler {
            log.info { "Socket ended" }
        }
    }
}