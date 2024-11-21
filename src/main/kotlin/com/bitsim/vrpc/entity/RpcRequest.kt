package com.bitsim.vrpc.entity

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *@author  BitSim
 *@version  v1.0.0

 **/
data class RpcRequest  (
    val rid: String = "",
    val version: String = "",
    val serviceName: String = "",
    val methodName: String = "",
    val parameterTypes: List<Class<*>> = listOf(),
    val parameters: List<Any> = listOf()
)
























