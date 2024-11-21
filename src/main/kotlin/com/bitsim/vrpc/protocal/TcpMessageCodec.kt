package com.bitsim.vrpc.protocal

import com.bitsim.vrpc.constant.TcpMessageConstant
import com.bitsim.vrpc.entity.RpcRequest
import com.bitsim.vrpc.entity.RpcResponse
import com.bitsim.vrpc.serializer.Serializer
import java.nio.ByteBuffer


/**
 *@author  BitSim
 *@version  v1.0.0

 **/
object TcpMessageCodec {
    fun <T> encode(message: TcpMessage<T>): ByteArray {
        val bodyBytes = Serializer.Factory(message.header.serializer).serialize(message.body)
        val header = message.header
        val byteBuffer = ByteBuffer.allocate(
             Int.SIZE_BYTES + Byte.SIZE_BYTES * 2 + Short.SIZE_BYTES + Long.SIZE_BYTES +
                    TcpMessageConstant.STRING_LENGTH + Int.SIZE_BYTES + bodyBytes.size
        ).apply {
            putInt(header.magic)
            put(header.messageType)
            put(header.status)
            putShort(header.version)
            putLong(header.requestId)
            put(header.serializer.padEnd(TcpMessageConstant.STRING_LENGTH, ' ').toByteArray())
            putInt(bodyBytes.size)
            put(bodyBytes)
        }
        return byteBuffer.array()
    }

    fun <T> decode(bytes: ByteArray): TcpMessage<T> {
        val byteBuffer = ByteBuffer.wrap(bytes)
        val header = Header(
            magic = byteBuffer.getInt(),
            messageType = byteBuffer.get(),
            status = byteBuffer.get(),
            version = byteBuffer.short,
            requestId = byteBuffer.long,
            serializer = String(
                bytes,
                byteBuffer.position(),
                TcpMessageConstant.STRING_LENGTH
            ).trim().also { byteBuffer.position(byteBuffer.position() + TcpMessageConstant.STRING_LENGTH) },
            bodyLength = byteBuffer.int
        )
        val bodyBytes = ByteArray(header.bodyLength).apply { byteBuffer.get(this) }
        val body: T = Serializer.Factory(header.serializer).deserialize(
            bodyBytes,
            when (header.messageType) {
                TcpMessageTypeEnum.REQUEST.key -> RpcRequest::class.java
                TcpMessageTypeEnum.RESPONSE.key -> RpcResponse::class.java
                else -> throw IllegalArgumentException("Unknown message type: ${header.messageType}")
            }
        ) as T
        return TcpMessage(header, body)
    }
}
