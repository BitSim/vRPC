package com.bitsim.vrpc.constant

/**
 *@author  BitSim
 *@version  v1.0.0
 **/
object TcpMessageConstant {
    const val HEADER_MESSAGE_LEN = 28
    const val MESSAGE_LEN_INDEX = 24
    const val STRING_LENGTH = 8
    const val MAGIC_NUMBER = 0x1205ABC
    const val MAX_MESSAGE_SIZE = 10 * 1024 * 1024
}