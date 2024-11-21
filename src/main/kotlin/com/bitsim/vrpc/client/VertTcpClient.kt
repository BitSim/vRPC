package com.bitsim.vrpc.client

/**
 *@author  BitSim
 *@version  v1.0.0

 **/
import com.bitsim.vrpc.config.Config
import com.bitsim.vrpc.constant.SerializerConstant
import com.bitsim.vrpc.constant.TcpMessageConstant
import com.bitsim.vrpc.entity.RpcRequest
import com.bitsim.vrpc.entity.RpcResponse
import com.bitsim.vrpc.protocal.*
import com.bitsim.vrpc.wrapper.VertBufferWrapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.net.NetClient
import io.vertx.core.net.NetSocket
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * RPC客户端
 * 负责与服务端建立连接并发送RPC请求
 *
 * @property host 服务器机地址
 * @property port 服务器端口
 */
class VertTcpClient(
    private val host: String,
    private val port: Int
) {
    private val vertx = Vertx.vertx()
    private val netClient: NetClient = vertx.createNetClient()
    private lateinit var socket: NetSocket
    private val pendingRequests = mutableMapOf<Long, (RpcResponse) -> Unit>()

    suspend fun connect() = suspendCancellableCoroutine { continuation ->
        netClient.connect(port, host) { result ->
            if (result.succeeded()) {
                socket = result.result()
                log.info { "Successfully connected to $host:$port" }

                setupMessageHandler()

                continuation.resume(Unit)
            } else {
                val error = result.cause()
                continuation.resumeWithException(error)
            }
        }

    }

    suspend fun send(request: RpcRequest): RpcResponse = suspendCancellableCoroutine { continuation ->
        try {
            val requestId = System.currentTimeMillis()
            log.info { "Request details: serviceName=${request.serviceName}, methodName=${request.methodName}, params=${request.parameters}" }

            val tcpMessage = TcpMessage(
                header = Header(
                    magic = TcpMessageConstant.MAGIC_NUMBER,
                    messageType = TcpMessageTypeEnum.REQUEST.key,
                    status = TcpMessageStatusEnum.OK.key,
                    version = 1,
                    requestId = requestId,
                    serializer = Config.getProperty(SerializerConstant.SERIALIZER_TYPE)!!
                ),
                body = request
            )

            val encodedData = TcpMessageCodec.encode(tcpMessage)

            pendingRequests[requestId] = { response ->
                continuation.resume(response)
            }

            socket.write(Buffer.buffer(encodedData)) { ar ->
                if (ar.succeeded()) {
                } else {
                    val error = ar.cause()
                    pendingRequests.remove(requestId)
                    continuation.resumeWithException(error)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            continuation.resumeWithException(e)
        }
    }

    private fun setupMessageHandler() {
        socket.handler(
            VertBufferWrapper { buffer ->
                try {
                    val decode: TcpMessage<RpcResponse> = TcpMessageCodec.decode(buffer.bytes)

                    val requestId = decode.header.requestId
                    val rpcResponse = decode.body

                    pendingRequests.remove(requestId)?.let { callback ->
                        callback(rpcResponse)
                    } ?: println("Warning: No callback found for request $requestId")

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )

        socket.exceptionHandler { e ->
            e.printStackTrace()
        }

        socket.closeHandler {
            pendingRequests.clear()
        }

        socket.endHandler {
        }
    }

    fun close() {
        pendingRequests.clear()
        socket.close()
        netClient.close()
        vertx.close()
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}