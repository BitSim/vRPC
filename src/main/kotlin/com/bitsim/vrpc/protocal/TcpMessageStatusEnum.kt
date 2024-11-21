package com.bitsim.vrpc.protocal

/**
 *@author  BitSim
 *@version  v1.0.0

 **/
enum class TcpMessageStatusEnum(val key: Byte) {
    OK(1),
    BAD_REQUEST(2),
    BAD_RESPONSE(3)
}