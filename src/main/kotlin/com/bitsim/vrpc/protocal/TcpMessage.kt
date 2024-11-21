package com.bitsim.vrpc.protocal

data class Header(
    val magic: Int,
    var messageType: Byte,
    val status: Byte,
    val version: Short,
    val requestId: Long,
    val serializer: String,
    val bodyLength: Int = 0
) {

    init {
        require(serializer.length <= 8) { "Serializer must not exceed 8 characters" }
    }
}

data class TcpMessage<T>(
    val header: Header,
    val body: T
)

