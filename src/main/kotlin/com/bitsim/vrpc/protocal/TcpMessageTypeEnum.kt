package com.bitsim.vrpc.protocal

/**
 *@author  BitSim
 *@version  v1.0.0

 **/
enum class TcpMessageTypeEnum(val key:Byte) {
    REQUEST(1),
    RESPONSE(2)
}