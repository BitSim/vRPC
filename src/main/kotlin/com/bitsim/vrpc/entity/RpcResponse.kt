package com.bitsim.vrpc.entity

import java.lang.Exception

/**
 *@author  BitSim
 *@version  v1.0.0

 **/
data class RpcResponse (
    val rid: String = "",
    val success: Boolean = false,
    val data: Any? = null,
    val dataType: Class<*>? = null,
    val error:Exception? = null
)